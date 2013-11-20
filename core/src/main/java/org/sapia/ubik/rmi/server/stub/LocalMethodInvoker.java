package org.sapia.ubik.rmi.server.stub;

import java.lang.reflect.Method;

/**
 * An instance of this interface is meant to invoke methods of remote stubs that
 * are not meant for being called remotely, such as <code>toString()</code>,
 * <code>equals()</code>, <code>hashCode()</code>, etc.
 * 
 * @author yduchesne
 * 
 */
public interface LocalMethodInvoker {

  public static LocalMethodInvoker TO_STRING = new LocalMethodInvoker() {

    public boolean apply(Method toInvoke) {
      return toInvoke.getParameterTypes().length == 0;
    }

    public Object invoke(StubInvocationHandler handler, Object[] params) throws Throwable {
      return handler.toString();
    }

  };

  // --------------------------------------------------------------------------

  public static LocalMethodInvoker EQUALS = new LocalMethodInvoker() {

    public boolean apply(Method toInvoke) {
      return toInvoke.getParameterTypes().length == 1;
    }

    public Object invoke(StubInvocationHandler handler, Object[] params) throws Throwable {
      return handler.equals(params[0]);
    }

  };

  // --------------------------------------------------------------------------

  public static LocalMethodInvoker HASH_CODE = new LocalMethodInvoker() {

    public boolean apply(Method toInvoke) {
      return toInvoke.getParameterTypes().length == 0;
    }

    public Object invoke(StubInvocationHandler handler, Object[] params) throws Throwable {
      return handler.hashCode();
    }

  };

  // --------------------------------------------------------------------------

  public static LocalMethodInvoker GET_STUB_INVOCATION_HANDLER = new LocalMethodInvoker() {

    public boolean apply(Method toInvoke) {
      return toInvoke.getDeclaringClass().equals(Stub.class);
    }

    public Object invoke(StubInvocationHandler handler, Object[] params) throws Throwable {
      return handler;
    }

  };

  // ==========================================================================

  /**
   * @param toInvoke
   *          the {@link Method} to invoke.
   * @return <code>true</code> if this instance can invoke the given method.
   */
  public boolean apply(Method toInvoke);

  /**
   * @param handler
   *          the {@link StubInvocationHandler} that received the given method.
   * @param params
   *          the parameters that were received for the method.
   * @return the {@link Object} corresponding to the method's return value.
   * @throws Throwable
   *           if an error occurs while handling the method's invocation.
   */
  public Object invoke(StubInvocationHandler handler, Object[] params) throws Throwable;
}
