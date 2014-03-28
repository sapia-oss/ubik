package org.sapia.ubik.concurrent;

import java.util.concurrent.TimeUnit;

import org.sapia.ubik.util.SysClock;


/**
 * Grants passage every <code>n</code> millisecond(s).
 *
 * @author yduchesne
 *
 */
public class TimeIntervalBarrier {

  private SysClock      clock           = SysClock.RealtimeClock.getInstance();
  private volatile long lastAccess;
  private long          intervalMillis;

  /**
   * @param intervalMillis a time interval, in milliseconds.
   */
  public TimeIntervalBarrier(long intervalMillis) {
    this.intervalMillis = intervalMillis;
  }

  /**
   * @param clock a {@link SysClock}.
   * @param intervalMillis a time interval, in milliseconds.
   */
  public TimeIntervalBarrier(SysClock clock, long intervalMillis) {
    this.clock = clock;
    this.intervalMillis = intervalMillis;
  }

  /**
   * @return <code>true</code> if this barrier should be passed.
   */
  public synchronized boolean tryAcquire() {
    if (clock.currentTimeMillis() - lastAccess >= intervalMillis) {
      lastAccess = clock.currentTimeMillis();
      return true;
    }
    return false;
  }

  /**
   * @param millis an interval in milliseconds.
   * @return a new {@link TimeIntervalBarrier}.
   */
  public static TimeIntervalBarrier forMillis(long millis) {
    return new TimeIntervalBarrier(millis);
  }

  /**
   * @return a one second {@link TimeIntervalBarrier}.
   */
  public static TimeIntervalBarrier forOneSecond() {
    return new TimeIntervalBarrier(TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));
  }

  /**
   * @param time the barrier's time.
   * @param unit the {@link TimeUnit} in which the given time is expressed.
   * @return a new {@link TimeIntervalBarrier}.
   */
  public static TimeIntervalBarrier forTime(long time, TimeUnit unit) {
    return new TimeIntervalBarrier(TimeUnit.MILLISECONDS.convert(time, unit));
  }
}
