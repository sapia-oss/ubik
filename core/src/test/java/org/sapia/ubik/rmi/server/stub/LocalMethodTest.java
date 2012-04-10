package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class LocalMethodTest {
  
  LocalMethod localMethod;

  @Before
  public void setUp() throws Exception {
    localMethod = new LocalMethod("toString");

  }

  @Test
  public void testGetMethodName() {
    assertEquals("toString", localMethod.getMethodName());
  }

  @Test
  public void testInvokerNotNull() throws Exception {
    localMethod.addLocalMethodInvoker(new ToStringInvoker());
    Method method = Object.class.getDeclaredMethod("toString", new Class<?>[]{});
    LocalMethodInvoker invoker = localMethod.getInvokerFor(method);
    assertEquals("Expected ToStringInvoker", ToStringInvoker.class, invoker.getClass());
  }
  
  @Test
  public void testInvokerNull() throws Exception {
    localMethod.addLocalMethodInvoker(new ToStringInvoker());
    Method method = ToStringWithArg.class.getDeclaredMethod("toString", new Class<?>[]{String.class});
    LocalMethodInvoker invoker = localMethod.getInvokerFor(method);
    assertNull("Expected null invoker", invoker);
  }
  
  @Test
  public void testBuiltInToStringInvoker() throws Exception {
    LocalMethod.LocalMethodMap map = LocalMethod.LocalMethodMap.buildDefaultMap();
    Method method = Object.class.getDeclaredMethod("toString", new Class<?>[]{});
    assertNotNull("Invoker should not be null", map.getInvokerFor(method));
  }
  
  @Test
  public void testBuiltInEqualsInvoker() throws Exception {
    LocalMethod.LocalMethodMap map = LocalMethod.LocalMethodMap.buildDefaultMap();
    Method method = Object.class.getDeclaredMethod("equals", new Class<?>[]{Object.class});
    assertNotNull("Invoker should not be null", map.getInvokerFor(method));
  }
  
  @Test
  public void testBuiltInHashCodeInvoker() throws Exception {
    LocalMethod.LocalMethodMap map = LocalMethod.LocalMethodMap.buildDefaultMap();
    Method method = Object.class.getDeclaredMethod("hashCode", new Class<?>[]{});
    assertNotNull("Invoker should not be null", map.getInvokerFor(method));
  }

  public static class ToStringInvoker implements LocalMethodInvoker {
    
    @Override
    public boolean apply(Method toInvoke) {
      return toInvoke.getParameterTypes().length == 0;
    }
    
    @Override
    public Object invoke(StubInvocationHandler handler, Object[] params)
        throws Throwable {
      return null;
    }
    
  }
 
  public static class ToStringWithArg {
    
    public String toString(String arg) {
      return  toString();
    }
  }
  
}
