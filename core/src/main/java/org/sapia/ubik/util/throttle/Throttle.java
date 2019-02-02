package org.sapia.ubik.util.throttle;

/**
 * Abstract throttling logic.
 * 
 * @author yduchesne
 *
 */
public interface Throttle {

  /**
   * This method returns <code>true</code> if the calling thread may proceed with the throttle operation.
   * Otherwise, the method returns false. Implementations are exected to return immediately (that is: not
   * have the calling thread to a <code>wait()</code> on a certain condition).
   * 
   * @return <code>true</code> if the calling thread may proceed.
   */
  boolean tryAcquire();
}
