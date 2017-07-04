package org.sapia.ubik.rmi.interceptor;

import java.lang.reflect.Method;

/**
 * This class encapsulates interceptor meta-information.
 * 
 * @author yduchesne
 */
class InterceptorInfo {
  
  Object interceptor;
  Method method;

  InterceptorInfo(Object it, Method m) {
    interceptor = it;
    method = m;
  }
}
