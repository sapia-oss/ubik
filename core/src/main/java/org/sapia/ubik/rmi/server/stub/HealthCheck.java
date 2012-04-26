package org.sapia.ubik.rmi.server.stub;

import java.rmi.RemoteException;

/**
 * This interface should be implemented by {@link StubInvocationHandler}s whose corresponding
 * stubs are bound to Ubik's JNDI: the JNDI implementation internally validates the stubs
 * by calling there handler's {@link #isValid()} method - provided such handlers implement this
 * interface.
 * <p>
 * This is a mechanism that allows returning to client stubs that are deemed valid.
 * 
 * @author Yanick Duchesne
 */
public interface HealthCheck {
  
  /**
   * @return <code>false</code> if the remote server is down.
   * @throws RemoteException if a {@link RemoteException} occurs.
   */
  public boolean isValid() throws RemoteException;
}
