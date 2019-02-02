package org.sapia.ubik.util.throttle;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;

/**
 * Performs basic rate limiting, based on a threshold per unit of time.
 * 
 * @author yduchesne
 *
 */
public class RateThrottle implements Throttle {
  
  private SysClock      clock   = RealtimeClock.getInstance();
  private TimeUnit      timeUnit;
  private AtomicInteger counter = new AtomicInteger();
  private int           threshold;
  private long          periodDuration;
  private long          periodStartTime;

  /**
   * @param threshold the threshold value to limit to, per period).
   * @param periodDuration the duration of the period to limit against.
   * @param timeUnit the {@link TimeUnit} in which the period is expressed.
   */
  public RateThrottle(int threshold, long periodDuration, TimeUnit timeUnit) {
    this(RealtimeClock.getInstance(), threshold, periodDuration, timeUnit);
  }
  
  /**
   * @param clock the {@link SysClock} to used for calculating elapsed time and marking the beginning of the period.
   * @param threshold the threshold value to limit to, per period).
   * @param periodDuration the duration of the period to limit against.
   * @param timeUnit the {@link TimeUnit} in which the period is expressed.
   */
  public RateThrottle(SysClock clock, int threshold, long periodDuration, TimeUnit timeUnit) {
    this.clock          = clock;
    this.threshold      = threshold;
    this.periodDuration = periodDuration;
    this.timeUnit       = timeUnit;
    this.periodStartTime = timeUnit.convert(clock.nanoTime(), TimeUnit.NANOSECONDS);
  }
  
  @Override
  public synchronized boolean tryAcquire() {
    counter.incrementAndGet();
    double elapsed = timeUnit.convert(clock.nanoTime(), TimeUnit.NANOSECONDS) - periodStartTime;
    if (elapsed >= periodDuration) {
      counter.set(0);
      periodStartTime = timeUnit.convert(clock.nanoTime(), TimeUnit.NANOSECONDS);
      elapsed = timeUnit.convert(clock.nanoTime(), TimeUnit.NANOSECONDS) - periodStartTime;
    }
    double rate = elapsed == 0 ? (double) counter.get() : (double) counter.get() / elapsed; 
    if (rate > threshold) {
      return false;
    } 
    return true;
  }
}
