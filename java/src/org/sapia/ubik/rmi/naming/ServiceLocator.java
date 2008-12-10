package org.sapia.ubik.rmi.naming;

import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.naming.remote.proxy.JNDIHandler;

import java.util.HashMap;
import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;


/**
 * This class implements the Service Locator pattern. It acts as a universal
 * lookup "helper", in conjunction with pluggable <code>ServiceHandler</code>
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
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
  private static Map _handlers = new HashMap();

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

        if (className == null) {
          throw new IllegalStateException(
            "no class name defined for transport provider: " + propName);
        }

        try {
          handler = (ServiceHandler) Class.forName(className).newInstance();
          registerHandler(scheme, handler);
        } catch (Throwable e) {
          e.printStackTrace();
          throw new IllegalStateException(
            "could not instantiate transport provider: " + className);
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

    ServiceHandler handler = (ServiceHandler) _handlers.get(uri.getScheme());

    if (handler == null) {
      throw new NamingException("no handler found for: " + uri.getScheme());
    }

    return handler.handleLookup(uri.getHost(), uri.getPort(),
      uri.getQueryString().getPath(), uri.getQueryString().getParameters());
  }

  /**
   * Registers the given handler with the passed in scheme.
   *
   * @param scheme a URI scheme.
   * @param handler a <code>ServiceHandler</code>.
   * @throws IllegalStateException if a handler is already registered with the given scheme.
   */
  public static void registerHandler(String scheme, ServiceHandler handler)
    throws IllegalStateException {
    if (_handlers.get(scheme) != null) {
      throw new IllegalStateException(
        "service handler already registered for :" + scheme);
    }

    _handlers.put(scheme, handler);
  }
}
