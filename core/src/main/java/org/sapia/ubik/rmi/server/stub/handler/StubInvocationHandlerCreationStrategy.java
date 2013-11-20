package org.sapia.ubik.rmi.server.stub.handler;

import java.rmi.RemoteException;

import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.StubStrategy;

/**
 * Specifies the behavior for creating {@link StubInvocationHandler}s.
 */
public interface StubInvocationHandlerCreationStrategy extends StubStrategy {

  /**
   * @param toExport
   *          the {@link Object} for which a stub (dynamic proxy) is to be
   *          created.
   * @return <code>true</code> if this instance can create a
   *         {@link StubInvocationHandler} for the given object.
   */
  public boolean apply(Object toExport);

  /**
   * @param toExport
   *          the {@link Object} for which a stub (dynamic proxy) is to be
   *          created.
   * @param context
   *          the {@link RemoteRefContext} that the
   *          {@link StubInvocationHandler} that will be created by this method
   *          should wrap.
   * @return the {@link StubInvocationHandler} that was created.
   * @throws Exception
   *           if a problem occurs creating the invocation handler.
   */
  public StubInvocationHandler createInvocationHandlerFor(Object toExport, RemoteRefContext context) throws RemoteException;

}