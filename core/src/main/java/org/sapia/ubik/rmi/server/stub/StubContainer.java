package org.sapia.ubik.rmi.server.stub;

import java.rmi.RemoteException;

/**
 * Implementations of this interface allow to bind stubs to remote JNDI servers
 * without the latter having to contain the stubs' interfaces in their
 * classpath.
 * 
 * @author Yanick Duchesne
 */
public interface StubContainer extends java.io.Serializable {
  /**
   * Returns the stub that this instance contains.
   * 
   * @return the stub that this instance contains.
   * @param loader
   *          a {@link ClassLoader}
   * @throws RemoteException
   *           if the stub could not be created.
   */
  public Object toStub(ClassLoader loader) throws RemoteException;

  /**
   * Returns the invocation handler that this instance wraps.
   * 
   * @return a {@link StubInvocationHandler}.
   */
  public StubInvocationHandler getStubInvocationHandler();
}
