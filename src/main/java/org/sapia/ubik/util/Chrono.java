package org.sapia.ubik.util;

import java.util.concurrent.TimeUnit;

/**
 * Measures a duration.
 * 
 * @author yduchesne
 *
 */
public class Chrono {
  
  private long start = System.currentTimeMillis();
  
  /**
   * @return the number of millis elapsed since this instance's start.
   */
  public long getElapsed() { 
    return System.currentTimeMillis() - start;
  }
  
  /**
   * @param unit the {@link TimeUnit} in which to return the elapsed time.
   * @return the time elapsed since this instance's start, expressed in the 
   * given time unit.
   */
  public long getElapsed(TimeUnit unit) {
    return TimeUnit.MILLISECONDS.convert(getElapsed(), unit);
  }

}
