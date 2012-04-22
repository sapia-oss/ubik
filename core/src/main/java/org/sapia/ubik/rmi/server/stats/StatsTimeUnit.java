package org.sapia.ubik.rmi.server.stats;

import java.util.concurrent.TimeUnit;

/**
 * Use as a replacement for the JDK's {@link TimeUnit} enum: this class supports converting time in non-integer values
 * For example, 20 minutes would be 0.333333... - as converted using the {@link TimeUnit} enum, it would amount to 0 hour.
 * 
 * @see #convertFrom(double, StatsTimeUnit)
 * 
 * @author yduchesne
 */
public enum StatsTimeUnit {

	NANOSECONDS(1),
  MILLISECONDS(NANOSECONDS.unitInNanos * 1000000),
  SECONDS(MILLISECONDS.unitInNanos * 1000),
  MINUTES(SECONDS.unitInNanos * 60),
  HOURS(MINUTES.unitInNanos * 60),
  DAYS(HOURS.unitInNanos * 24);
  
  private double unitInNanos;
  
  StatsTimeUnit(double unitInNanos) {
    this.unitInNanos= unitInNanos;
  }

  /**
   * Returns the {@link StatsTimeUnit} correponding to the given JDK {@link TimeUnit}. The following
   * JDK time units are supported:
   * 
   * <ul>
   *   <li> {@link TimeUnit#NANOSECONDS}
   *   <li> {@link TimeUnit#SECONDS}
   *   <li> {@link TimeUnit#MINUTES}
   *   <li> {@link TimeUnit#HOURS}
   *   <li> {@link TimeUnit#DAYS}
   * </ul>
   * 
   * @param timeunit a {@link TimeUnit}.
   * @return the {@link StatsTimeUnit} corresponding to the given JDK time unit.
   */
  public static final StatsTimeUnit fromJdkTimeUnit(TimeUnit timeunit) {
    switch(timeunit) {
      case NANOSECONDS:  return StatsTimeUnit.NANOSECONDS;
      case MILLISECONDS: return StatsTimeUnit.MILLISECONDS;
      case SECONDS:      return StatsTimeUnit.SECONDS;
      case MINUTES:      return StatsTimeUnit.MINUTES;
      case HOURS:        return StatsTimeUnit.HOURS;
      case DAYS:         return StatsTimeUnit.DAYS;  
      default:
        throw new IllegalStateException(String.format("Cannot convert %s", timeunit));
    }
  }
  
  /**
   * @param time the time expressed in the given time unit.
   * @param srcTimeUnit the {@link StatsTimeUnit} to convert from.
   * @return the time, converted to this instance.
   */
  public double convertFrom(double time, StatsTimeUnit srcTimeUnit) {
    double srcNanos = srcTimeUnit.unitInNanos * time;
    return srcNanos / unitInNanos;
  }
  
  /**
   * @param from the {@link StatsTimeUnit} to convert from.
   * @param to the {@link StatsTimeUnit} to convert to.
   * @return the number of times a unit of the given <code>from</code> time unit fits in to 
   * the given <code>to</code> time unit.
   */
  public static double getConversionFactor(StatsTimeUnit from, StatsTimeUnit to) {
    return to.convertFrom(1, from);
  }
  
  public static void main(String[] args) {
	  System.out.println(StatsTimeUnit.MILLISECONDS.convertFrom(50000, StatsTimeUnit.NANOSECONDS));
  }

}
