package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.gc.ClientGC;
import org.sapia.ubik.rmi.server.gc.CommandRefer;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * Implements Ubik RMI's client-side runtime environment. This class' main
 * task is to manage remote references and client-side interceptors.
 *
 * @author Yanick Duchesne
 */
public class ClientRuntime implements Module{
  
  private Category log = Log.createCategory(getClass());
  
  /**
   * The dispatcher of events destined to be intercepted by {@link Interceptor} instances.
   * Dispatches client-side events. This mechanism can conveniently be used by client apps
   * to dispatch their own custom events.
   *
   * @see Interceptor
   */
  private final MultiDispatcher dispatcher = new MultiDispatcher();
  
  /**
   * The {@link ServerTable}.
   */
  private ServerTable serverTable;

  /** The client-side part of the distributed garbage-collection mechanism */
  private ClientGC gc;
  
  @Override
  public void init(ModuleContext context) {
    serverTable = context.lookup(ServerTable.class);
  }
  
  @Override
  public void start(ModuleContext context) {
    gc = context.lookup(ClientGC.class);
  }
  
  @Override
  public void stop() {
  }
 
  /**
   *  @return this instance's {@link MultiDispatcher}.
   *  @see #dispatcher.
   */
  public MultiDispatcher getDispatcher() {
    return dispatcher;
  }
  
  /**
   * @return the {@link ClientGC} instance.
   */
  public ClientGC getGc() {
    return gc;
  }

  /**
   * Returns <code>true</code> if this "client" is call-back enabled (if
   * it itself is a server supporting responses from remote servers in
   * the for of call-backs.
   *
   * @see Consts#CALLBACK_ENABLED
   *
   * @return <code>true</code> if this server supports call-back mode.
   */
  public boolean isCallback(String transportType) {
    return serverTable.hasServerFor(transportType);
  }

  void shutdown(long timeout) throws InterruptedException {
  }

  /**
   * Returns this client's call-back server address (corresponds to the
   * this client's singleton server, which will process incoming responses).
   *
   * @param transportType a the transport type for which to return the callback address.
   * @return the {@link ServerAddress} corresponding to the given transport type, and that should
   * be used as the callback address.
   * @throws IllegalStateException if this client does not allow call-backs.
   * @see Consts#CALLBACK_ENABLED
   */
  public ServerAddress getCallbackAddress(String transportType)
    throws IllegalStateException {
    if (!serverTable.hasServerFor(transportType)) {
      throw new IllegalStateException(
        "No callback server was instantiated; make sure " +
        "the following system property is set to 'true' upon VM startup: " +
        Consts.CALLBACK_ENABLED);
    }

    return serverTable.getServerAddress(transportType);
  }

  /**
   * Adds a client-side interceptor to this client.
   *
   * @see Interceptor
   * @see MultiDispatcher#addInterceptor(Class, Interceptor)
   */
  public synchronized void addInterceptor(Class<?> eventClass, Interceptor it)
    throws InvalidInterceptorException {
    dispatcher.addInterceptor(eventClass, it);
  }

  /**
   * Dispatches the given event to registered interceptors.
   *
   * @see Interceptor
   * @see MultiDispatcher#addInterceptor(Class, Interceptor)
   */
  public void dispatchEvent(Event event) {
    dispatcher.dispatch(event);
  }
  
  /**
   * This method is meant to increase the reference count of a remote object, whose corresponding stub
   * has reached the current JVM.
   * 
   * @param address the {@link ServerAddress} of the remote server to connect to.
   * @param oid the {@link OID} for which to create a reference.
   * @throws RemoteException
   */
  public void createReference(ServerAddress address, OID oid)
  throws RemoteException {
    Connections conns = Hub.getModules().getTransportManager().getConnectionsFor(address);
  
    try {
      doSend(conns, oid);
    } catch (ClassNotFoundException e) {
      throw new RemoteException("could not refer to object: " + oid + "@" +
        address, e);
    } catch (RemoteException e) {
      conns.clear();
  
      try {
        doSend(conns, oid);
      } catch (RemoteException e2) {
        log.warning("Could not refer to object: " + oid + "@" + 
            address + "; server probably down");
      } catch (Exception e2) {
        throw new RemoteException("Could not refer to object: " + oid + "@" +
          address, e2);
      }
    } catch (IOException e) {
      throw new RemoteException("Could not refer to object: " + oid + "@" +
        address, e);
    }
  }

  private static void doSend(Connections conns, OID oid)
  throws RemoteException, IOException, ClassNotFoundException {
    RmiConnection conn = null;
  
    try {
      conn = conns.acquire();
      conn.send(new CommandRefer(oid));
      conn.receive();
    } catch (Exception e) {
    	if (conn != null) {
    		conn.close();
    	}
    } finally {
      if (conn != null) {
        conns.release(conn);
      }
    }
  }  
}
