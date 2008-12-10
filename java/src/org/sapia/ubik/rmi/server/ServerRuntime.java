package org.sapia.ubik.rmi.server;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.gc.ServerGC;
import org.sapia.ubik.rmi.server.invocation.RMICommandProcessor;
import org.sapia.ubik.taskman.TaskManager;


/**
 * Implements the server-side behavior of RMI.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerRuntime {
  static final int DEFAULT_MAX_CALLBACK_THREADS = 1;

  /**
   * Holds <code>OID</code>-to-object mappings. Keeps distributed object references
   * so that the latter are spared from the VM GC while remote applications might still
   * access to the said references.
   */
  public final ObjectTable objectTable = new ObjectTable();

  /** Holds <code>OID</code>-to-object mappings. */
  /**
   * The dispatcher of events destined to be intercepted by <code>Interceptor</code> instances.
   * Dispatches server-side events. This mechanism can conveniently be used by client apps
   * to dispatch their own custom events.
   */
  public final MultiDispatcher dispatcher = new MultiDispatcher();

  /**
   * The object implementing the server-side distributed GC mechanism.
   */
  public final ServerGC gc;

  /**
   * The processor handling incoming <code>RMICommand</code> instances.
   *
   * @see RMICommand
   */
  public final RMICommandProcessor processor;

  /**
   * The single <code>Server</code> held by this instance.
   */
  public final ServerTable server = new ServerTable();

  ServerRuntime(TaskManager taskman) {
    int maxThreads = DEFAULT_MAX_CALLBACK_THREADS;

    if (System.getProperty(Consts.SERVER_CALLBACK_MAX_THREADS) != null) {
      try {
        if ((System.getProperty(Consts.CALLBACK_ENABLED) != null) &&
              System.getProperty(Consts.CALLBACK_ENABLED).equalsIgnoreCase("true")) {
          maxThreads = Integer.parseInt(System.getProperty(
                Consts.SERVER_CALLBACK_MAX_THREADS));
        } else {
          maxThreads = 1;
        }
      } catch (NumberFormatException e) {
        Log.warning(ServerRuntime.class,
          "invalid value for system property: " +
          Consts.SERVER_CALLBACK_MAX_THREADS + "; using default: " +
          maxThreads);
      }
    }

    processor   = new RMICommandProcessor(maxThreads);
    gc          = new ServerGC(taskman);
  }

  /**
   * Adds an interceptor of server-side events to this instance.
   *
   * @see Interceptor
   * @see MultiDispatcher#addInterceptor(Class, Interceptor)
   */
  public synchronized void addInterceptor(Class eventClass, Interceptor it)
    throws InvalidInterceptorException {
    dispatcher.addInterceptor(eventClass, it);
  }

  /**
   * Dispatches the given event to the underlying server-side interceptors.
   *
   * @see MultiDispatcher#dispatch(Event)
   */
  public void dispatchEvent(Event event) {
    dispatcher.dispatch(event);
  }

  /**
   * Shuts down this instance.
   */
  void shutdown(long timeout) throws InterruptedException {
    Log.warning(ServerRuntime.class, "Shutting down command processor");
    processor.shutdown(timeout);
    Log.warning(ServerRuntime.class, "Shutting down server");
    server.close();
  }
}
