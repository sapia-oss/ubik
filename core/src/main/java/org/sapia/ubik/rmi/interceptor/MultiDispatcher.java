package org.sapia.ubik.rmi.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Log;


/**
 * This dispatcher allows to register multiple interceptors
 * for a given event.
 *
 * @author Yanick Duchesne
 */
public class MultiDispatcher {
  
  Map<Class<?>, List<InterceptorInfo>> _interceptors = new ConcurrentHashMap<Class<?>, List<InterceptorInfo>>();

  /**
   * Adds an interceptor for the given event type.
   *
   * @param event an event class.
   * @param it an <code>Interceptor</code> instance.
   *
   * @throws InvalidInterceptorException if the interceptor could not be added.
   */
  public void addInterceptor(Class<?> event, Interceptor it)
    throws InvalidInterceptorException {
    Class<?>  itClass   = it.getClass();
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

    List<InterceptorInfo> interceptors = _interceptors.get(event);

    if (interceptors == null) {
      interceptors = new ArrayList<InterceptorInfo>();
      _interceptors.put(event, interceptors);
    }

    interceptors.add(new InterceptorInfo(it, m));
  }

  /**
   * Dispatches the given event to all interceptors that have
   * registered for the event's class.
   */
  public void dispatch(Event event) {
    List<InterceptorInfo> interceptors = _interceptors.get(event.getClass());

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
