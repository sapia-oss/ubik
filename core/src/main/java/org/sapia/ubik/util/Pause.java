package org.sapia.ubik.util;

import org.sapia.ubik.util.SysClock.RealtimeClock;

/**
 * Models a pause.
 * 
 * @author yduchesne
 * 
 */
public class Pause {

  private SysClock clock = RealtimeClock.getInstance();
  private long start;
  private long duration;

  /**
   * @param clock
   *          the {@link SysClock} to use.
   * @param duration
   *          the duration of this delay, in millis.
   */
  public Pause(SysClock clock, long duration) {
    this.clock = clock;
    this.duration = duration;
    this.start = clock.currentTimeMillis();
  }

  /**
   * @param duration
   *          the duration of this delay, in millis.
   */
  public Pause(long duration) {
    this.duration = duration;
    this.start = clock.currentTimeMillis();
  }

  /**
   * @return <code>true</code> if this delay is over.
   */
  public boolean isOver() {
    return clock.currentTimeMillis() - start >= duration;
  }

  /**
   * This method returns the number of millis that remain in this delay. This
   * method will return at least 0 (which indicates that the delay is over).
   * 
   * @return the number of remaining milliseconds in this delay.
   */
  public long remaining() {
    long remaining = duration - (clock.currentTimeMillis() - start);
    return remaining < 0 ? 0 : remaining;
  }

  /**
   * This method returns the number of millis that remain in this delay. This
   * method will return at least 1: this is to allow passing the returned value
   * to the {@link Object#wait(long)} method without incurring a "forever" wait.
   * 
   * @return the number of remaining milliseconds in this delay.
   */
  public long remainingNotZero() {
    long remaining = remaining();
    return remaining == 0 ? 1 : remaining;
  }

  /**
   * @return the duration (in millis) that was originally assigned to this
   *         instance.
   */
  public long getDuration() {
    return duration;
  }

  /**
   * @return the time (in millis) at which this instance was created.
   */
  public long getStart() {
    return start;
  }

  /**
   * Resets this instance.
   */
  public void reset() {
    start = clock.currentTimeMillis();
  }
}
