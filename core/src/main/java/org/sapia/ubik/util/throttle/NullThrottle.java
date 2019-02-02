package org.sapia.ubik.util.throttle;

/**
 * Implements a pass-through {@link Throttle}.
 * 
 * @author yduchesne
 *
 */
public class NullThrottle implements Throttle {
  
  @Override
  public boolean tryAcquire() {
    return true;
  }

}
