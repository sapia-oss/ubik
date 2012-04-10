package org.sapia.ubik.util;

import java.util.concurrent.TimeUnit;

import org.sapia.ubik.util.Clock.SystemClock;

/**
 * Measures a duration.
 * 
 * @author yduchesne
 *
 */
public class Chrono {
  
	private Clock clock = SystemClock.getInstance();
  private long start;
  
  public Chrono(Clock clock) {
  	this.clock = clock;
  	this.start = clock.currentTimeMillis();
  }
  
  public Chrono() {
  	this.start = clock.currentTimeMillis();
  }  
  
  /**
   * @return the number of millis elapsed since this instance's start.
   */
  public long getElapsed() { 
    return clock.currentTimeMillis() - start;
  }
  
  /**
   * @param unit the {@link TimeUnit} in which to return the elapsed time.
   * @return the time elapsed since this instance's start, expressed in the 
   * given time unit.
   */
  public long getElapsed(TimeUnit unit) {
    return TimeUnit.SECONDS.convert(getElapsed(), TimeUnit.MILLISECONDS);
  }

}
