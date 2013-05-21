package org.sapia.ubik.rmi.naming;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.naming.remote.proxy.JNDIHandler;
import org.sapia.ubik.util.Assertions;


/**
 * This class implements the Service Locator pattern. It acts as a universal
 * lookup "helper", in conjunction with pluggable {@link ServiceHandler}
 * instances. The ServiceLocator encapsulates one to many service handlers;
 * for a given lookup, the locator retrieves the appropriate locator and then
 * delegates the lookup to the latter, returning the result to the caller.
 * <p>
 * Service handlers are plugged into the locator in one of the following ways:
 * <ul>
 * <li>By specifying service handlers as part of the system properties. This
 * requires mapping a service handler uri scheme to the handler's class. The
 * property name in this case must respect the following syntax:
 * <code>ubik.rmi.naming.service.handler.uri_scheme</code> (where 'uri_scheme'
 * is the scheme that is used to retreive a given object.
 * <li>by calling this class' <code>registerHandler(...)</code> method.
 * </ul>
 * <p>
 * For example, given the following service handler: <code>com.acme.MyServiceHandler</code>,
 * the configuration could be created has follows:
 * <pre>
 * System.setProperty("ubik.rmi.naming.service.handler.acme", "com.acme.MyServiceHandler");
 * </pre>
 *
 * Then, latter on, a lookup can be performed using this class:
 * <pre>
 * Object remote = ServiceLocator.lookup("acme://localhost:7070");
 * </pre>
 *
 * As shown above, the "acme" uri that was used to register the handler is
 * then used in the lookup. Using the scheme of the passed in URI, the
 * <code>lookup(...)</code> method delegates the operation to the proper
 * handler.
 * <p>
 * <b>IMPORTANT</b>: If service handlers are configured through system
 * properties, it is important to do so before invoking the ServiceLocator
 * class in any way: the latter retrieves the configured handlers in a
 * static initializer, which would prevent the handlers from being discovered
 * if configured after the ServiceLocator class' initialization.
 *
 * @author Yanick Duchesne
 */
public class ServiceLocator {
  /**
   * This constant identifies an unidentified port.
   */
  public static final int UNDEFINED_PORT = -1;

  /**
   * Corresponds to the prefix of properties specifying service handlers. The prefix
   * is: 'ubik.rmi.naming.service.handler'.
   */
  public static final String HANDLER_PATTERN = "ubik.rmi.naming.service.handler";

  /**
   * The URI scheme for the default service handler: ubik.
   *
   * @see JNDIHandler
   */
  public static final String UBIK_SCHEME = "ubik";

  /**
   * The URI scheme for ubik's reliable jndi handler: ubik.reliable.jndi.
   *
   * @see JNDIHandler
   */
  private static Map<String, ServiceHandler> handlers = new ConcurrentHashMap<String, ServiceHandler>();

  static {
    registerHandler(UBIK_SCHEME, new JNDIHandler());

    ServiceHandler handler;
    String[]       propNames = (String[]) System.getProperties().keySet()
                                                .toArray(new String[System.getProperties()
                                                                          .size()]);
    String propName;
    String className;
    String scheme;

    for (int i = 0; i < propNames.length; i++) {
      propName = (String) propNames[i];

      if (propName.startsWith(HANDLER_PATTERN)) {
        scheme   = propName.substring(HANDLER_PATTERN.length() + 1);

        className = System.getProperty(propName);

        Assertions.illegalState(className == null, "No class name defined for transport provider %s", propName);

        try {
          handler = (ServiceHandler) Class.forName(className).newInstance();
          registerHandler(scheme, handler);
        } catch (Throwable e) {
          throw new IllegalStateException(
            "Could not instantiate transport provider: " + className, e);
        }
      }
    }
  }

  /**
   * Performs the lookup for the given path.
   *
   * @param url the url of the object to look up.
   *
   * @return an <code>Object</code>
   * @throws NameNotFoundException if no object could be found for the
   * given url.
   * @throws NamingException if an error occurs while performing the lookup.

   */
  public static Object lookup(String url)
    throws NameNotFoundException, NamingException {
    Uri uri;

    try {
      uri = Uri.parse(url);
    } catch (UriSyntaxException e) {
      NamingException exc = new NamingException("could not look up " + url);
      exc.setRootCause(e);
      throw exc;
    }

    ServiceHandler handler = (ServiceHandler) handlers.get(uri.getScheme());

    if (handler == null) {
      throw new NamingException("no handler found for: " + uri.getScheme());
    }

    if(Log.isDebug()) {
    	Log.debug(ServiceLocator.class, "Delegating lookup to handler: " + handler);
    }
    
    try {
    Object obj = handler.handleLookup(uri.getHost(), uri.getPort(),
      uri.getQueryString().getPath(), uri.getQueryString().getParameters());
    
    if(Log.isDebug()) {
    	Log.debug(ServiceLocator.class, "Lookup finished");
    }
    
    return obj;
    } catch (Throwable t) {
    	t.printStackTrace();
    	throw new NamingException("Could not lookup");
    }
  }

  /**
   * Registers the given handler with the passed in scheme.
   *
   * @param scheme a URI scheme.
   * @param handler a {@link ServiceHandler}.
   * @throws IllegalStateException if a handler is already registered with the given scheme.
   */
  public static void registerHandler(String scheme, ServiceHandler handler) throws IllegalStateException {
    Assertions.illegalState(handlers.get(scheme) != null, "Service handler already registered for: %s", scheme);
    handlers.put(scheme, handler);
  }
  
  /**
   * Removes the handler associated to the given URI scheme.
   * 
   * @param scheme the URI scheme of the handler to remove from the {@link ServiceLocator}.
   */
  public static void unregisterHandler(String scheme) {
    handlers.remove(scheme);
  }
}
