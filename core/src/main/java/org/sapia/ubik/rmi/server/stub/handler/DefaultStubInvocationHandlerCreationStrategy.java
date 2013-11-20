package org.sapia.ubik.rmi.server.stub.handler;

import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.server.StubProcessor;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.RemoteRefEx;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;

/**
 * A default {@link StubInvocationHandlerCreationStrategy}.
 * 
 * @see StubProcessor
 * 
 * @author yduchesne
 * 
 */
public class DefaultStubInvocationHandlerCreationStrategy implements StubInvocationHandlerCreationStrategy {

  @Override
  public void init(ModuleContext context) {
  }

  @Override
  public boolean apply(Object toExport) {
    return true;
  }

  @Override
  public StubInvocationHandler createInvocationHandlerFor(Object toExport, RemoteRefContext context) {
    return new RemoteRefEx(context);
  }
}