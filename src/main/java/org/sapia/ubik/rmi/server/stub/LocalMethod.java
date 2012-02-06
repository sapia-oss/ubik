package org.sapia.ubik.rmi.server.stub;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates a list of {@link LocalMethodInvoker}s. 
 * 
 * @author yduchesne
 */
public class LocalMethod {
  
  private String                   methodName;
  private List<LocalMethodInvoker> invokers    = new ArrayList<LocalMethodInvoker>();

  public LocalMethod(String methodName) {
    this.methodName = methodName;
  }
  
  /**
   * @return this instance's method name.
   */
  public String getMethodName() {
    return methodName;
  }
  
  /**
   * @param invoker a {@link LocalMethodInvoker}.
   * @return this instance.
   */
  public LocalMethod addLocalMethodInvoker(LocalMethodInvoker invoker) {
    invokers.add(invoker);
    return this;
  }
  
  /**
   * Internally probes its {@link LocalMethodInvoker}s to determine if one can invoke
   * the given method. If one is found, it is returned (otherwise this method returns <code>null</code>).
   * 
   * @param toInvoke the {@link Method} to invoke.
   * @return the {@link LocalMethodInvoker} that matches the given method, or <code>null</code> if
   * no matching one was found.
   */
  public LocalMethodInvoker getInvokerFor(Method toInvoke) {
    for(LocalMethodInvoker invoker : invokers) {
      if(invoker.apply(toInvoke)) {
        return invoker;
      }
    }    
    return null;
  }
  
  /**
   * Keeps {@link LocalMethod} on a per method-name basis, allowing for fast lookup.
   * 
   * @author yduchesne
   *
   */
  public static class LocalMethodMap {
    
    private Map<String, LocalMethod> localMethods = new ConcurrentHashMap<String, LocalMethod>();
    
    /**
     * @param chain a {@link LocalMethod} to add.
     * @return this instance.
     */
    public LocalMethodMap addLocalMethod(LocalMethod lm) {
      localMethods.put(lm.getMethodName(), lm);
      return this;
    }
    
    /**
     * @param toInvoke the {@link Method} to invoke.
     * @return the {@link LocalMethodInvoker} corresponding to the given method, or <code>null</code>
     * if no such instance exists.
     */
    public LocalMethodInvoker getInvokerFor(Method toInvoke) {
      LocalMethod localMethod = localMethods.get(toInvoke.getName());
      if(localMethod != null) {
        return localMethod.getInvokerFor(toInvoke);
      }
      return null;
    }
    
    /**
     * @return a default {@link LocalMethodMap}, which is configured to handle calls to 
     * <code>toString()</code>, <code>hashCode()</code>, <code>equals()</code>.
     */
    public static LocalMethodMap buildDefaultMap() {
      LocalMethodMap map = new LocalMethodMap();
      map
        .addLocalMethod(new LocalMethod("toString").addLocalMethodInvoker(LocalMethodInvoker.TO_STRING))
        .addLocalMethod(new LocalMethod("hashCode").addLocalMethodInvoker(LocalMethodInvoker.HASH_CODE))
        .addLocalMethod(new LocalMethod("equals").addLocalMethodInvoker(LocalMethodInvoker.EQUALS));
        
      return map;
    }
  }
  
}
