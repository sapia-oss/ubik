package org.sapia.ubik.rmi.interceptor;


/**
 * This interface is a marker that interceptor objects must implement.
 * Interceptor classes are expected to defined methods that correspond
 * to the event types (or classes) they expect. Events are objects that
 * encapsulate state and are handled to the call back interceptor methods.
 * <p>
 * For example, suppose we define a given "event", through the
 * <code>LogEvent</code> class (note that the "event" string does not need to
 * appear in the class name). To intercept instances of this event, an
 * interceptor needs to be implemented, which will have the following
 * method:
 * <p>
 * <code>public void onLogEvent(LogEvent evt);</code>
 * <p>
 * As shown below, the method's signature must have the following pattern:
 * <p>
 * <code>on</code><i>EventClassName</i>(<i>EventClass</i>)
 * <p>
 * Once an interceptor class has been designed, it can be registered
 * with a dispatcher to intercept events of the specified class:
 * <p>
 * <pre>
 * SingleDispatcher disp = new SingleDispatcher();
 * LogInterceptor it = new LogInterceptor();
 * disp.registerInterceptor(LogEvent.class, it);
 *
 * // the following event will be intercepted by our interceptor.
 * disp.dispatch(new LogEvent());
 * </pre>
 * <p>
 * As one might have guessed, Java's introspection capabilities are
 * used to match the event class to the proper interceptor method at
 * registration time. This one-event-per-method scheme allows one interceptor
 * to register for multiple event types.
 * <p>
 * Dispatching behavior and event registration policies can vary from
 * one dispatcher to another.
 *
 * @author Yanick Duchesne
 */
public interface Interceptor {
}
