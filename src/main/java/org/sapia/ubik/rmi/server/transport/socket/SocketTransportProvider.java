package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.SocketConnectionFactory;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.net.ThreadPool;
import org.sapia.ubik.net.UbikServerSocketFactory;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Props;


/**
 * Implements the {@link TransportProvider} interface over the standard java {@link Socket} class.
 *
 * @see java.net.Socket
 *
 * @author Yanick Duchesne
 */
public class SocketTransportProvider implements TransportProvider {
  
  /**
   * Constant corresponding to this provider class' transport type.
   */
  public static final String TRANSPORT_TYPE   = TCPAddress.TRANSPORT_TYPE;

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.max-threads</code>
   * system property.
   */
  public static final String MAX_THREADS      = "ubik.rmi.transport.socket.max-threads";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.client-factory</code>
   * system property (it identifies the {@link ConnectionFactory} to use to create client-side
   * connections).
   */
  public static final String CLIENT_FACTORY   = "ubik.rmi.transport.socket.client-factory";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.server-factory</code>
   * system property. It identifies the {@link UbikServerSocketFactory} implementation to use.
   *
   */
  public static final String SERVER_FACTORY   = "ubik.rmi.transport.socket.server-factory";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.bind-address</code>
   * system property.
   */
  public static final String BIND_ADDRESS     = "ubik.rmi.transport.socket.bind-address";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.port</code>
   * system property.
   */
  public static final String PORT             = "ubik.rmi.transport.socket.port";
  
  protected static final long DEFAULT_RESET_INTERVAL               = 2000;
  private   static final long DEFAULT_STALE_CLIENT_CONNECTION_TIME = 5000;

  private Category                                       log   = Log.createCategory(getClass());
  private Map<ServerAddress, SocketClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, SocketClientConnectionPool>();
  private boolean                                        poolCleanerStarted;
  
  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address)
    throws RemoteException {
    Props props         = Props.getSystemProperties();
    long  resetInterval = props.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL);
    
    if(!poolCleanerStarted) {
      Hub.getModules().getTaskManager().addTask(
          new TaskContext(getClass().getSimpleName(), DEFAULT_STALE_CLIENT_CONNECTION_TIME), 
          new Task() {
            @Override
            public void exec(TaskContext ctx) {
              doCleanPools();
            }
          }
      );
      poolCleanerStarted = true;
    }
    
    SocketClientConnectionPool pool = pools.get(address);
    if(pool == null) {
      if(!(address instanceof TCPAddress)) {
        throw new IllegalArgumentException("Address not instance of TCPAddress: " + address);
      }
      
      TCPAddress tcpAddr = (TCPAddress) address;

      try {
        Class<?>  factoryClass = props.getClass(CLIENT_FACTORY, SocketRmiConnectionFactory.class);
        ConnectionFactory factory = (ConnectionFactory) factoryClass.newInstance();
        if(factory instanceof SocketRmiConnectionFactory) {
          ((SocketRmiConnectionFactory) factory).setResetInterval(resetInterval);
        }
        pool = new SocketClientConnectionPool(tcpAddr.getHost(), tcpAddr.getPort(), factory);
        pools.put(address, pool);        
      } catch (ClassNotFoundException e) {
        throw new RemoteException("Could load connection factory class (was not found)", e);
      } catch (IllegalAccessException e) {
        throw new RemoteException("Could not instantiate connection factory (illegal access, " +
                                  "make sure class has default, no-args, public constructor", e);
      } catch (InstantiationException e) {
        throw new RemoteException("Could not instantiate connection factory", e);
      }
    }

    return pool;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(Properties)
   */
  public Server newServer(Properties props) throws RemoteException {
    Props pu = new Props().addProperties(props).addProperties(System.getProperties());
    return doNewServer(pu.getIntProperty(PORT, 0), pu);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newDefaultServer()
   */
  public Server newDefaultServer() throws RemoteException {
    Props pu = new Props().addProperties(System.getProperties());
    return doNewServer(pu.getIntProperty(PORT, 0), pu);
  }
  
  public Server newServer(int port) throws RemoteException{
    Props pu = new Props().addProperties(System.getProperties());
    return doNewServer(port, pu);
  }

  protected Server doNewServer(int port, Props props) throws RemoteException{
    SocketRmiServer         server;
    int                     maxThreads = ThreadPool.NO_MAX;
    long                    resetInterval; 
    String                  bindAddress = null;
    UbikServerSocketFactory serverSocketFactory = null;
    
    maxThreads = props.getIntProperty(Consts.SERVER_MAX_THREADS, 0);     
    if(maxThreads == 0){
      maxThreads = props.getIntProperty(MAX_THREADS, 0);
    }
    
    try{
      bindAddress = props.getProperty(BIND_ADDRESS, Localhost.getAnyLocalAddress().getHostAddress());
    }catch(IOException e){
      throw new RemoteException("Invalid bind address", e);
    }
    
    if(port == 0){
      try{
        port = new TcpPortSelector().select(bindAddress);
      }catch(IOException e){
        throw new RemoteException("Could not acquire random port");
      }
    }

    if (props.getProperty(SERVER_FACTORY) != null) {
      String factoryClassName = props.getProperty(SERVER_FACTORY);

      try {
        serverSocketFactory = (UbikServerSocketFactory) Class.forName(factoryClassName).newInstance();
      } catch (InstantiationException e) {
        throw new java.rmi.RemoteException("Problem instantiating server socket factory", e);
      } catch (IllegalAccessException e) {
        throw new java.rmi.RemoteException("Could not find server socket factory; " +
            "make sure factory class is public, and has a public, no-arg constructor", e);
      } catch (ClassNotFoundException e) {
        throw new java.rmi.RemoteException("Could not find server socket factory implementation: " + factoryClassName, e);
      }
    }
    
    resetInterval = props.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL);
    
    try {
      server = SocketRmiServer.Builder.create()
        .setBindAddress(bindAddress)
        .setMaxThreads(maxThreads)
        .setResetInterval(resetInterval)
        .setPort(port)
        .setServerSocketFactory(serverSocketFactory)
        .build();
    } catch (IOException e) {
      throw new RemoteException("Could not create server", e);
    }
    return server;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   */
  public String getTransportType() {
    return TRANSPORT_TYPE;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public void shutdown() {
    doCleanPools();
  }
  
  private void doCleanPools() {
    SocketClientConnectionPool[] poolsArray = 
      (SocketClientConnectionPool[]) pools.values().toArray(new SocketClientConnectionPool[pools.size()]);

    for(SocketClientConnectionPool pool : poolsArray) {
      if ((System.currentTimeMillis() - pool.internalPool().getLastUsageTime()) >= DEFAULT_STALE_CLIENT_CONNECTION_TIME) {
        log.debug("Shrinking socket client connection pool...");
        pool.internalPool().shrinkTo(0);
      }
    }
  }
}