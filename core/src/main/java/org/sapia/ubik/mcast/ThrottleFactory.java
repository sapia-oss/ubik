package org.sapia.ubik.mcast;

import java.util.concurrent.TimeUnit;

import org.sapia.ubik.util.throttle.RateThrottle;
import org.sapia.ubik.util.throttle.Throttle;

/**
 * Specifies a factory behavior for creating {@link Throttle} instances.
 * 
 * @author yduchesne
 *
 */
public interface ThrottleFactory {

  /**
   * @return a new {@link Throttle}.
   */
  public Throttle createThrottle();
  
  
  /**
   * Implements a {@link ThrottleFactory} that creates {@link RateThrottle} instance.
   * 
   * @author yduchesne
   *
   */
  public static class RateThrottleFactory implements ThrottleFactory {
    
    private int      threshold;
    private TimeUnit timeUnit;
    
    public RateThrottleFactory(int threshold, TimeUnit timeUnit) {
      this.threshold = threshold;
      this.timeUnit  = timeUnit;
    }
    
    @Override
    public Throttle createThrottle() {
      return new RateThrottle(threshold, 1, timeUnit);
    }
    
  }
}
