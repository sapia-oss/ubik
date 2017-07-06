package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sapia.ubik.ioc.BeanLookup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.mina.MinaAddress;
import org.sapia.ubik.rmi.server.transport.mina.MinaTransportProvider;
import org.sapia.ubik.util.Conf;

/**
 * This class is the single-entry point into Ubik RMI's API.
 *
 * @author yduchesne
 */
public class Hub {

  public static final  String           DEFAULT_TRANSPORT_TYPE   = MinaTransportProvider.TRANSPORT_TYPE;

  private static final long             DEFAULT_SHUTDOWN_TIMEOUT = 10000;
  private static final int              MAX_CONNECT_RETRIES      = Conf.getSystemProperties().getIntProperty(
                                                                        Consts.CLIENT_CONNECTION_MAX_RETRY, 
                                                                        Defaults.DEFAULT_CLIENT_CONNECTION_MAX_RETRY
                                                                    );
  private static final AtomicBoolean    SHUTDOWN                 = new AtomicBoolean();
  private static final Category         LOG                      = Log.createCategory(Hub.class);
  private static final Modules          CONTAINER                = new Modules();
  private static final List<BeanLookup> LOOKUPS                  = Collections.synchronizedList(new ArrayList<BeanLookup>());

  /**
   * @param lookup a {@link BeanLookup} to add to this instance.
   * @see BeanLookup
   */
  public static void addBeanLookup(BeanLookup lookup) {
    LOOKUPS.add(lookup);
  }

  /**
   * @param lookup removes the given {@link BeanLookup} from this instance.
   */
  public static void removeBeanLookup(BeanLookup lookup) {
    LOOKUPS.remove(lookup);
  }

  /**
   * @param typeOf the {@link Class} of the bean to look up.
   * @return the bean corresponding to the given class, or <code>null</code> if no
   * such bean is found.
   * @see BeanLookup
   */
  public static <T> T getBean(Class<T> typeOf) {
    synchronized (LOOKUPS) {
      for (BeanLookup b : LOOKUPS) {
        T t = b.getBean(typeOf);
        if (t != null) {
          return t;
        }
      }
    }
    return null;
  }

  /**
   * "Exports" the passed in object as a remote RMI server: this method
   * internally starts an RMI server that listens on a random port and
   * implements the interfaces of the passed in object. The stub is returned and
   * can be bound to the JNDI.
   *
   * @see #exportObject(Object, int)
   * @see #connect(String, int)
   * @return the stub corresponding to the exported object.
   * @throws RemoteException
   *           if a problem occurs performing the connection.
   */
  public static Object exportObject(Object o) throws RemoteException {
    checkStarted();
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, DEFAULT_TRANSPORT_TYPE);
    return CONTAINER.getServerTable().exportObject(o, props);
  }

  /**
   * This method creates a server listening on the specified port.
   *
   * @see #exportObject(Object)
   * @throws RemoteException
   *           if a problem occurs performing the connection.
   * @return the stub for the given exported object.
   */
  public static Object exportObject(Object o, int port) throws RemoteException {
    checkStarted();
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, DEFAULT_TRANSPORT_TYPE);
    props.setProperty(MinaTransportProvider.PORT, Integer.toString(port));
    return CONTAINER.getServerTable().exportObject(o, props);
  }

  /**
   * Exports the given object as a server (and creates a remote reference). The
   * properties passed in must contain the property identifying the desired
   * "transport type" (ubik.rmi.transport.type).
   * <p>
   * The method returns the stub for the given object.
   *
   * @param object
   *          the {@link Object} to export.
   * @param props
   *          the transport provider {@link Properties}.
   * @return the stub of the exported server.
   * @see TransportManager
   * @see TransportManager#getProviderFor(String)
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider
   */
  public static Object exportObject(Object object, Properties props) throws RemoteException {
    checkStarted();
    return CONTAINER.getServerTable().exportObject(object, props);
  }

  /**
   * Exports the given object as a remote object that will receive request
   * through a server that must already have been exported for the given
   * transport type.
   * <p>
   * The method returns the stub for the given object.
   *
   * @param object
   *          an {@link Object} to export.
   * @param transportType
   *          the identifier of the transport layer to which the given object
   *          will be exported.
   * @return a stub.
   * @throws RemoteException
   *           if the object could not be exported.
   */
  public static Object exportObject(Object object, String transportType) throws RemoteException {
    checkStarted();
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, transportType);
    return CONTAINER.getServerTable().exportObject(object, props);
  }

  /**
   * This method "unexports" an object that was exported through one of this
   * class' <code>export()</code> methods. The unexported object will not
   * receive remote method calls anymore.
   * <p>
   * NOTE: this method does not stop the server through which the exported
   * instance is receiving remote method calls. To stop the servers that have
   * been started by this class, call the latter's {@link #shutdown()} method.
   *
   * @see #shutdown(long)
   *
   * @param o
   *          the exported object that is to be unexported.
   */
  public static void unexport(Object o) {
    checkStarted();
    CONTAINER.getObjectTable().remove(o);
  }

  /**
   * This method "unexports" all objects whose class was loaded by the given
   * <code>ClassLoader</code>.
   * <p>
   * This method can be useful in hot-deploy scenarios.
   * <p>
   * NOTE: this method does not stop the server through which the exported
   * instances (that correspond to the given classloader) are receiving remote
   * method calls. To stop the servers that have been started by the
   * <code>Hub</code>, call the latter's {@link #SHUTDOWN} method.
   *
   * @see #shutdown(long)
   *
   * @param loader
   */
  public static void unexport(ClassLoader loader) {
    checkStarted();
    CONTAINER.getObjectTable().remove(loader);
  }

  /**
   * This method allows connecting to a RMI server listening to the given host
   * and port.
   *
   * @throws RemoteException
   *           if a problem occurs performing the connection.
   */
  public static Object connect(String host, int port) throws RemoteException {
    checkStarted();
    return connect(new MinaAddress(host, port));
  }

  /**
   * This method allows connecting to a RMI server listening on the given
   * address.
   *
   * @param address
   *          the {@link ServerAddress} corresponding to the target server's
   *          physical endpoint.
   *
   * @throws RemoteException
   *           if a problem occurs performing the connection.
   */
  public static Object connect(ServerAddress address) throws RemoteException {
    checkStarted();
    return tryConnect(address);
  }
  
  /**
   * Forces the clearing of the connection pool corresponding to the given
   * address.
   *
   * @see Connections#clear()
   * @param address
   *          a {@link ServerAddress}
   */
  public static void refresh(ServerAddress address) {
    checkStarted();
    try {
      Hub.getModules().getTransportManager().getConnectionsFor(address).clear();
    } catch (RemoteException e) {
      // noop
    }
  }

  /**
   * Returns the address of the server for the given transport type.
   *
   * @param transportType
   *          the logical identifier of a "transport type".
   * @return a <code>ServerAddress</code>,
   */
  public static ServerAddress getServerAddressFor(String transportType) {
    checkStarted();
    return CONTAINER.getServerTable().getServerAddress(transportType);
  }

  /**
   * Returns true if the Hub is shut down.
   *
   * @return <code>true</code> if the Hub is shut down.
   */
  public static boolean isShutdown() {
    return SHUTDOWN.get();
  }

  /**
   * Shuts down this instance. The calling thread will wait a builtin maximum of
   * seconds for the shutdown to be completed - after which it will return.
   */
  public static synchronized void shutdown() {
    try {
      shutdown(DEFAULT_SHUTDOWN_TIMEOUT);
    } catch (InterruptedException e) {
      LOG.warning("Thread interrupted during shutdown");
      throw new ThreadInterruptedException();
    }
  }

  /**
   * Shuts down this instance; some part of the shutdown can be asynchronous. A
   * timeout must be given in order not to risk the shutdown to last for too
   * long.
   *
   * @param timeout
   *          a shutdown "timeout", in millis.
   * @throws InterruptedException
   */
  public static synchronized void shutdown(long timeout) throws InterruptedException {
    if (SHUTDOWN.get()) {
      return;
    }
    LOG.info("Shutting down...");
    LOOKUPS.clear();
    CONTAINER.stop();
    LOG.info("Shutdown completed");
    SHUTDOWN.set(true);
  }

  /**
   * @return this class' {@link Modules}.
   */
  public static Modules getModules() {
    checkStarted();
    return CONTAINER;
  }

  /**
   * Explicitely starts the Hub's modules.
   */
  public static void start() {
    checkStarted();
  }
  
  // --------------------------------------------------------------------------
  // Restricted

  // this method is not synchronized, since the container's start() method is
  // itself synchronized
  private static void checkStarted() {
    if (!CONTAINER.isStarted()) {

      LOG.info("Performing initialization (VMID = %s)", VmId.getInstance());

      CONTAINER.init();
      CONTAINER.start();

      SHUTDOWN.set(false);
    }
  }
  
  private static Object tryConnect(ServerAddress address) throws RemoteException {
    
    int             retryCount = 0;
    RemoteException lastExc    = null;
    while (retryCount < MAX_CONNECT_RETRIES) {
      try {
        return doConnect(address);
      } catch (RemoteException e) {
        lastExc = e;
        if (e.getCause() instanceof SocketTimeoutException) {
          retryCount++;
          continue;
        } else {
          throw e;
        }
      }
    }
    if (lastExc != null) {
      throw lastExc;
    }
    throw new RemoteException(String.format("Could not connect to host %s within the specified timeout", address));
  }
  
  private static Object doConnect(ServerAddress address) throws RemoteException {
    RmiConnection conn = null;
    Object toReturn;

    Connections pool = Hub.getModules().getTransportManager().getConnectionsFor(address);
    try {

      conn = pool.acquire();

      try {
        conn.send(new CommandConnect(address.getTransportType()));
      } catch (RemoteException e) {
        pool.invalidate(conn);
        pool.clear();
        conn = pool.acquire();
        conn.send(new CommandConnect(address.getTransportType()));
      }
      toReturn = conn.receive();
      pool.release(conn);
    } catch (ConnectException e) {
      throw new RemoteException("No server at address: " + address, e);
    } catch (RemoteException e) {
      if (conn != null) {
        pool.invalidate(conn);
        pool.clear();
      }
      throw e;
    } catch (IOException e) {
      throw new RemoteException("Error connecting to remote server " + address, e);
    } catch (ClassNotFoundException e) {
      throw new RemoteException("Could not find class", e);
    }

    if (toReturn instanceof Throwable) {
      if (toReturn instanceof RuntimeException) {
        throw (RuntimeException) toReturn;
      } else {
        throw new RemoteException("Problem connecting to remote server", (Throwable) toReturn);
      }
    }

    return toReturn;
  }

}
