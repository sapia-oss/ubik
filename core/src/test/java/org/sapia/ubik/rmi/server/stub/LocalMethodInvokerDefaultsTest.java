package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LocalMethodInvokerDefaultsTest {

  @Test
  public void testToStringInvoker() throws Exception {
    LocalMethodInvoker invoker = LocalMethodInvoker.TO_STRING;
    assertTrue(invoker.apply(Object.class.getDeclaredMethod("toString", new Class<?>[] {})));
  }

  @Test
  public void testEqualsInvoker() throws Exception {
    LocalMethodInvoker invoker = LocalMethodInvoker.EQUALS;
    assertTrue(invoker.apply(Object.class.getDeclaredMethod("equals", new Class<?>[] { Object.class })));
  }

  @Test
  public void testHashcodeInvoker() throws Exception {
    LocalMethodInvoker invoker = LocalMethodInvoker.HASH_CODE;
    assertTrue(invoker.apply(Object.class.getDeclaredMethod("hashCode", new Class<?>[] {})));
  }

  @Test
  public void testGetStubInvocationHandlerInvoker() throws Exception {
    LocalMethodInvoker invoker = LocalMethodInvoker.GET_STUB_INVOCATION_HANDLER;
    assertTrue(invoker.apply(Stub.class.getDeclaredMethod("getStubInvocationHandler", new Class<?>[] {})));
  }
}
