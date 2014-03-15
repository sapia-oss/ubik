package org.sapia.ubik.rmi.server.stub;

import java.lang.reflect.InvocationHandler;
import java.util.Collection;

/**
 * Base interface of invocation handlers that implement the logic of dynamic
 * stubs - created by the Ubik RMI runtime.
 *
 * @author Yanick Duchesne
 *
 */
public interface StubInvocationHandler extends InvocationHandler, HealthCheck {

  /**
   * Returns a {@link StubContainer} that wraps this instance's state.
   *
   * @return a {@link StubContainer}
   */
  public StubContainer toStubContainer(Object proxy);

  /**
   * Returns a collection, in order to allows supporting multiple remote
   * endpoints behind a stub.
   *
   * @return this instance's {@link RemoteRefContext}s.
   */
  public Collection<RemoteRefContext> getContexts();

}
