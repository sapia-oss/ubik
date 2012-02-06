package org.sapia.ubik.rmi.server;

import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;


/**
 * Implements the server-side behavior of RMI.
 *
 * @author Yanick Duchesne
 */
public final class ServerRuntime implements Module {
  
  /**
   * The dispatcher of events destined to be intercepted by {@link Interceptor} instances.
   * Dispatches server-side events. This mechanism can conveniently be used by client apps
   * to dispatch their own custom events.
   */
  private MultiDispatcher     dispatcher = new MultiDispatcher();

  
  @Override
  public void init(ModuleContext context) {
  }
    
  @Override
  public void start(ModuleContext context) {
  }
  
  @Override
  public void stop() {
  }
  
  /**
   * @return this instance's {@link MultiDispatcher}.
   * @see #dispatcher.
   */
  public MultiDispatcher getDispatcher() {
    return dispatcher;
  }
  

  /**
   * Adds an interceptor of server-side events to this instance.
   *
   * @see Interceptor
   * @see MultiDispatcher#addInterceptor(Class, Interceptor)
   */
  public synchronized void addInterceptor(Class<?> eventClass, Interceptor it)
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
}
