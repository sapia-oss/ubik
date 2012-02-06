package org.sapia.ubik.util;

/**
 * Models a delay.
 * 
 * @author yduchesne
 *
 */
public class Delay {
  
  private long start      = System.currentTimeMillis();
  private long duration;
  
  /**
   * @param timeout the duration of this delay, in millis.
   */
  public Delay(long duration) {
    this.duration = duration;
  }
  
  /**
   * @return <code>true</code> if this delay is over.
   */
  public boolean isOver() {
    return System.currentTimeMillis() - start >= duration;
  }
  
  /**
   * This method returns the number of millis that remain in this delay. This method
   * will return at least 0 (which indicates that the delay is over).
   * 
   * @return the number of remaining milliseconds in this delay.
   */
  public long remaining() {
    long remaining = duration - (System.currentTimeMillis() - start);
    return remaining < 0 ? 0 : remaining;
  }
  
  /**
   * This method returns the number of millis that remain in this delay. This method
   * will return at least 1: this is to allow passing the returned value to the {@link Object#wait(long)}
   * method without incurring a "forever" wait.
   * 
   * @return the number of remaining milliseconds in this delay.
   */
  public long remainingNotZero() {
    long remaining = remaining();
    return remaining == 0 ? 1 : remaining;
  }
  
  /**
   * @return the duration (in millis) that was originally assigned to this instance.
   */
  public long getDuration() {
    return duration;
  }

}
