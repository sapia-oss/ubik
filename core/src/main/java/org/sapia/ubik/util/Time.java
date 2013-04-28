package org.sapia.ubik.util;

import java.util.concurrent.TimeUnit;

/**
 * Holds time, in a given time unit.
 * 
 * @author yduchesne
 *
 */
public class Time {
  
  long     value;
  TimeUnit unit;
  
  /**
   * @param value the time value with which to create this instance.
   * @param unit the {@link TimeUnit} in which the given value is expressed.
   */
  public Time(long value, TimeUnit unit) {
    this.value = value;
    this.unit  = unit;
  }
  
  /**
   * @return this instance's value.
   */
  public long getValue() {
    return value;
  }
  
  /**
   * @return this instance's {@link TimeUnit}.
   */
  public TimeUnit getUnit() {
    return unit;
  }
  
  /**
   * @return this instance's value in millis.
   */
  public long getValueInMillis() {
    return TimeUnit.MILLISECONDS.convert(value, unit);
  }

  /**
   * @return this instance's value in seconds.
   */
  public long getValueInSeconds() {
    return TimeUnit.SECONDS.convert(value, unit);
  }
  
  /**
   * @param millis a milliseconds value.
   * @return a new {@link Time}.
   */
  public static Time createMillis(long millis) {
    return new Time(millis, TimeUnit.MILLISECONDS);
  }
  
  /**
   * @param seconds a seconds value.
   * @return a new {@link Time}.
   */
  public static Time createSeconds(long seconds) {
    return new Time(seconds, TimeUnit.SECONDS);
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
      .append("[")
      .append(value)
      .append(" ")
      .append(unit)
      .append("]").toString();
  }
}