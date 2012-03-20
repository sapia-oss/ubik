package org.sapia.ubik.rmi.server.stub.creation;

import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.StubStrategy;

/**
 * Specifies stub creation behavior.
 * 
 * @author yduchesne
 *
 */
public interface StubCreationStrategy extends StubStrategy {
  
  /**
   * @param exported the {@link Object} that has been exported (as a remote object).
   * @param handler the {@link StubInvocationHandler} that the stub to create should wrap.
   * @return <code>true</code> if this instance should be used to create the stub corresponding
   * to the given parameters.
   */
  public boolean apply(Object exported, StubInvocationHandler handler);
  
  /**
   * @param exported the {@link Object} that has been exported (as a remote object).
   * @param handler the {@link StubInvocationHandler} that the stub to create should wrap.
   * @param stubInterfaces the array of {@link Class} corresponding to the interfaces that
   * the stub should implement.
   * @return a new stub, wrapping the given handler.
   */
  public Object createStubFor(Object exported, StubInvocationHandler handler, Class<?>[] stubInterfaces);

}
