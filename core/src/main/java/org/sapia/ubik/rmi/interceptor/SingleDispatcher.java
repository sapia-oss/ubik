package org.sapia.ubik.rmi.interceptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This dispatcher allows to register only one interceptor per event type.
 * 
 * @author Yanick Duchesne
 */
public class SingleDispatcher {

  Map<Class<?>, InterceptorInfo> _interceptors = new HashMap<Class<?>, InterceptorInfo>();

  /**
   * Registers an interceptor with the given event type.
   * 
   * @param event
   *          an event class.
   * @param it
   *          an <code>Interceptor</code> instance.
   * 
   * @throws InvalidInterceptorException
   *           if the interceptor could not be registered.
   */
  public void registerInterceptor(Class<?> event, Interceptor it) throws InvalidInterceptorException {
    Class<?> itClass = it.getClass();
    int idx = event.getName().lastIndexOf('.');
    String shortName;

    if (idx < 0) {
      shortName = event.getName();
    } else {
      shortName = event.getName().substring(idx + 1);
    }

    char[] content = shortName.toCharArray();
    content[0] = Character.toUpperCase(content[0]);
    shortName = "on" + new String(content);

    Method m;

    try {
      m = itClass.getMethod(shortName, new Class[] { event });
    } catch (Exception e) {
      throw new InvalidInterceptorException(e);
    }

    if (_interceptors.get(event) != null) {
      throw new InvalidInterceptorException("interceptor already registered for " + event.getName());
    }

    _interceptors.put(event, new InterceptorInfo(it, m));
  }

  /**
   * Dispatches the given event to the interceptor that has registered for the
   * event's class.
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
   * Template method that is called internally when an error is trapped when
   * invoking the call-back method on a given interceptor instance.
   */
  protected void handleError(Throwable t) {
    t.printStackTrace();
  }
}
