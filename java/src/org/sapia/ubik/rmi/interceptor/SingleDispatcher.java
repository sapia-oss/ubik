package org.sapia.ubik.rmi.interceptor;

import java.lang.reflect.Method;

import java.util.*;


/**
 * This dispatcher allows to register only one interceptor
 * per event type.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SingleDispatcher {
  Map _interceptors = new HashMap();

  /**
   * Registers an interceptor with the given event type.
   *
   * @param event an event class.
   * @param it an <code>Interceptor</code> instance.
   *
   * @throws InvalidInterceptorException if the interceptor could not be registered.
   */
  public void registerInterceptor(Class event, Interceptor it)
    throws InvalidInterceptorException {
    Class  itClass   = it.getClass();
    int    idx       = event.getName().lastIndexOf('.');
    String shortName;

    if (idx < 0) {
      shortName = event.getName();
    } else {
      shortName = event.getName().substring(idx + 1);
    }

    char[] content = shortName.toCharArray();
    content[0]   = Character.toUpperCase(content[0]);
    shortName    = "on" + new String(content);

    Method m;

    try {
      m = itClass.getMethod(shortName, new Class[] { event });
    } catch (Exception e) {
      throw new InvalidInterceptorException(e);
    }

    if (_interceptors.get(event) != null) {
      throw new InvalidInterceptorException(
        "interceptor already registered for " + event.getName());
    }

    _interceptors.put(event, new InterceptorInfo(it, m));
  }

  /**
   * Dispatches the given event to the interceptor that has
   * registered for the event's class.
   */
  public void dispatch(Event event) {
    InterceptorInfo info = (InterceptorInfo) _interceptors.get(event.getClass());

    if (info == null) {
      return;
    }

    try {
      info.method.invoke(info.interceptor, new Object[] { event });
    } catch (Throwable t) {
      handleError(t);
    }
  }

  /**
   * Template method that is called internally when an error is
   * trapped when invoking the call-back method on a given
   * interceptor instance.
   */
  protected void handleError(Throwable t) {
    t.printStackTrace();
  }
}
