package org.sapia.ubik.rmi.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Log;

/**
 * This dispatcher allows to register multiple interceptors for a given event.
 * 
 * @author yduchesne
 */
public class MultiDispatcher {

  private Map<Class<?>, List<InterceptorInfo>> interceptors = new ConcurrentHashMap<Class<?>, List<InterceptorInfo>>();

  private boolean lenient = true;
  
  /**
   * @param lenient if <code>true</code>, indicates that errors caught trying to invoke interceptors should be
   *                swallowed silently (<code>true</code> by default).
   */
  public void setLenient(boolean lenient) {
    this.lenient = lenient;
  }
  
  /**
   * Adds an interceptor for the given event type.
   * 
   * @param event
   *          an event class.
   * @param it
   *          an event interceptor.
   * 
   * @throws InvalidInterceptorException
   *           if the interceptor could not be added.
   */
  public void addInterceptor(Class<?> event, Object it) throws InvalidInterceptorException {
    Method m = tryFindMethod(event, it.getClass());
    
    if (m == null) {
      throw new InvalidInterceptorException("No method found on interceptor " + it + " that accepts event of type: " + event.getName());
    }
    
    List<InterceptorInfo> interceptorList = interceptors.get(event);
   
    if (interceptorList == null) {
      synchronized (interceptors) {
        interceptorList = interceptors.get(event);
        if (interceptorList == null) {
          interceptorList = new ArrayList<InterceptorInfo>();
          interceptors.put(event, interceptorList);
        }
      }
    }

    interceptorList.add(new InterceptorInfo(it, m));
  }
    
  /**
   * Dispatches the given event to all interceptors that have registered for the
   * event's class.
   */
  public void dispatch(Object event) {
    List<InterceptorInfo> interceptors = findInterceptors(event.getClass());

    if (interceptors.isEmpty()) {
      return;
    }
    
    InterceptorInfo info;

    for (int i = 0; i < interceptors.size(); i++) {
      info = (InterceptorInfo) interceptors.get(i);

      try {
        info.method.invoke(info.interceptor, new Object[] { event });
      } catch (InvocationTargetException e) {
        if (lenient) {
          handleError(e.getTargetException());
        } else {
          if (e.getTargetException() instanceof RuntimeException) {
            throw (RuntimeException) e.getTargetException();
          } else {
            throw new IllegalStateException("Error invoking method " + info.method + " on " + info.interceptor, e.getTargetException());
          }
        }
      } catch (RuntimeException e) {
        if (lenient) {
          handleError(e);
        } else {
          throw e;
        }
      } catch (Throwable t) {
        if (lenient) {
          throw new IllegalStateException("Error invoking method " + info.method + " on " + info.interceptor, t);
        }
      }
    }
  }

  /**
   * Template method that is called internally when an error is trapped when
   * invoking the call-back method on a given interceptor instance.
   */
  protected void handleError(Throwable t) {
    Log.error(getClass(), t);
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private List<InterceptorInfo> findInterceptors(Class<?> eventClass) {
    List<InterceptorInfo> interceptorList = interceptors.get(eventClass);

    if (interceptorList == null) {
      synchronized (interceptors) {
        interceptorList = interceptors.get(eventClass);
        if (interceptorList == null) {
          interceptorList = new ArrayList<>();
          for (List<InterceptorInfo> currentList : interceptors.values()) {
            for (InterceptorInfo currentIt : currentList) {
              Method m = tryFindMethod(eventClass, currentIt.interceptor.getClass());
              if (m != null) {
                interceptorList.add(new InterceptorInfo(currentIt.interceptor, m));
              }
            }
          }
          interceptors.put(eventClass, interceptorList);
        }
      }
    }
    
    return interceptorList == null ? Collections.emptyList() : interceptorList;
    
  }
  
  private Method tryFindMethod(Class<?> eventClass, Class<?> itClass) {
    
    for (Method m : itClass.getMethods()) {
      if (Modifier.isPublic(m.getModifiers()) 
          && m.getParameterTypes().length == 1 
          && !m.getParameterTypes()[0].equals(Object.class)
          && m.getParameterTypes()[0].isAssignableFrom(eventClass)) {
        return m;
      }
    }
    
    return null;
    
  }

}
