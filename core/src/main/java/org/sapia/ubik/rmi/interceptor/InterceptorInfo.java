package org.sapia.ubik.rmi.interceptor;

import java.lang.reflect.Method;

/**
 * This class encapsulates interceptor meta-information.
 * 
 * @author Yanick Duchesne
 */
class InterceptorInfo {
  Interceptor interceptor;
  Method method;

  InterceptorInfo(Interceptor it, Method m) {
    interceptor = it;
    method = m;
  }
}
