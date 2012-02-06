package org.sapia.ubik.rmi.server.stub.creation;

import java.lang.reflect.Proxy;

import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;

public class DefaultStubCreationStrategy implements StubCreationStrategy {
  
  @Override
  public boolean apply(Object exported, StubInvocationHandler handler) {
    return true;
  }
  
  public Object createStubFor(Object exported, StubInvocationHandler handler, java.lang.Class<?>[] stubInterfaces) {
    return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), stubInterfaces, handler);
  };

}
