package org.sapia.ubik.rmi.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.gc.ClientGC;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher;
import org.sapia.ubik.taskman.TaskManager;

import com.google.inject.Singleton;


/**
 * Implements Ubik RMI's client-side runtime environment. This class' main
 * task is to manage remote references and client-side interceptors.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClientRuntime {
  /**
   * The dispatcher of method invocations to remote servers.
   */
  static final InvocationDispatcher invoker = new InvocationDispatcher();

  /**
   * Address of the singleton server that will receive call-backs.
   *
   * @see Singleton
   */
  private static Map<String, ServerAddress> _serverAddresses = new ConcurrentHashMap<String, ServerAddress>();

  /**
   * The dispatcher of events destined to be intercepted by <code>Interceptor</code> instances.
   * Dispatches client-side events. This mechanism can conveniently be used by client apps
   * to dispatch their own custom events.
   *
   * @see Interceptor
   */
  public final MultiDispatcher dispatcher = new MultiDispatcher();

  /** The client-side part of the distributed garbage-collection mechanism */
  final ClientGC gc;

  ClientRuntime(TaskManager taskman) {
    gc = new ClientGC(taskman);
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
  boolean isCallback(String transportType) {
    if (_serverAddresses.get(transportType) == null) {
      doInit(transportType);
    }

    return _serverAddresses.get(transportType) != null;
  }

  void shutdown(long timeout) throws InterruptedException {
  }

  /**
   * Returns this client's call-back server address (corresponds to the
   * this client's singleton server, which will process incoming responses).
   *
   * @see Consts#CALLBACK_ENABLED
   *
   * @return this client's <code>ServerAddress</code> for call-backs.
   * @throws IllegalStateException if this client does not allow call-backs.
   */
  public ServerAddress getCallbackAddress(String transportType)
    throws IllegalStateException {
    if (_serverAddresses.get(transportType) == null) {
      throw new IllegalStateException(
        "no callback server was instantiated; make sure " +
        "the following system property is set to 'true' upon VM startup: " +
        Consts.CALLBACK_ENABLED);
    }

    return _serverAddresses.get(transportType);
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

  /***
   * Performs initialization of this instance.
   */
  synchronized void doInit(String transportType) {
    if ((System.getProperty(Consts.CALLBACK_ENABLED) != null) &&
          System.getProperty(Consts.CALLBACK_ENABLED).equalsIgnoreCase("true")) {
      if (_serverAddresses.get(transportType) != null) {
        return;
      }

      Log.warning(getClass(),
        "Creating server to receive callbacks on transport: " + transportType);

      ServerAddress serverAddress = null;

      if (Hub.serverRuntime.server.isInit(transportType)) {
        serverAddress = Hub.serverRuntime.server.getServerAddress(transportType);
      } else {
        try {
          serverAddress = Hub.serverRuntime.server.init(transportType);
        } catch (java.rmi.RemoteException e) {
          Log.error(ClientRuntime.class, e);
        }
      }

      _serverAddresses.put(transportType, serverAddress);
    }
  }
}
