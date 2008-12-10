package org.sapia.ubik.rmi.interceptor;

import org.sapia.ubik.rmi.server.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.*;


/**
 * This dispatcher allows to register multiple interceptors
 * for a given event.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MultiDispatcher {
  Map _interceptors = new HashMap();

  /**
   * Adds an interceptor for the given event type.
   *
   * @param event an event class.
   * @param it an <code>Interceptor</code> instance.
   *
   * @throws InvalidInterceptorException if the interceptor could not be added.
   */
  public void addInterceptor(Class event, Interceptor it)
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

    List interceptors = (List) _interceptors.get(event);

    if (interceptors == null) {
      interceptors = new ArrayList();
      _interceptors.put(event, interceptors);
    }

    interceptors.add(new InterceptorInfo(it, m));
  }

  /**
   * Dispatches the given event to all interceptors that have
   * registered for the event's class.
   */
  public void dispatch(Event event) {
    List interceptors = (List) _interceptors.get(event.getClass());

    if (interceptors == null) {
      return;
    }

    InterceptorInfo info;

    for (int i = 0; i < interceptors.size(); i++) {
      info = (InterceptorInfo) interceptors.get(i);

      try {
        info.method.invoke(info.interceptor, new Object[] { event });
      } catch (Throwable t) {
        if (t instanceof RuntimeException) {
          throw (RuntimeException) t;
        } else if (t instanceof InvocationTargetException) {
          Throwable t2 = ((InvocationTargetException) t).getTargetException();

          if (t2 instanceof RuntimeException) {
            throw (RuntimeException) t2;
          }
        }

        handleError(t);
      }
    }
  }

  /**
   * Template method that is called internally when an error is
   * trapped when invoking the call-back method on a given
   * interceptor instance.
   */
  protected void handleError(Throwable t) {
    Log.error(getClass(), t);
  }
}
