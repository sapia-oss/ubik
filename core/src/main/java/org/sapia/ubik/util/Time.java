package org.sapia.ubik.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Holds time, in a given time unit.
 *
 * @author yduchesne
 *
 */
public class Time {

  private long     value;
  private TimeUnit unit;

  private static final Map<String, TimeUnit> UNITS_BY_NAME = new HashMap<String, TimeUnit>();
  private static final List<String> UNIT_NAMES = new ArrayList<>();
  static {
    UNITS_BY_NAME.put("h", TimeUnit.HOURS);
    UNITS_BY_NAME.put("hr", TimeUnit.HOURS);
    UNITS_BY_NAME.put("min", TimeUnit.MINUTES);
    UNITS_BY_NAME.put("s", TimeUnit.SECONDS);
    UNITS_BY_NAME.put("sec", TimeUnit.SECONDS);
    UNITS_BY_NAME.put("ms", TimeUnit.MILLISECONDS);

    UNIT_NAMES.add("h");
    UNIT_NAMES.add("hr");
    UNIT_NAMES.add("min");
    UNIT_NAMES.add("sec");
    UNIT_NAMES.add("ms");
    UNIT_NAMES.add("s");
  }

  /**
   * @param value
   *          the time value with which to create this instance.
   * @param unit
   *          the {@link TimeUnit} in which the given value is expressed.
   */
  public Time(long value, TimeUnit unit) {
    this.value = value;
    this.unit = unit;
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
   * @param millis
   *          a milliseconds value.
   * @return a new {@link Time}.
   */
  public static Time createMillis(long millis) {
    return new Time(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * @param seconds
   *          a seconds value.
   * @return a new {@link Time}.
   */
  public static Time createSeconds(long seconds) {
    return new Time(seconds, TimeUnit.SECONDS);
  }

  /**
   * @param s a {@link String} corresponding to a {@link Time} literal.
   * @return a new {@link Time}.
   */
  public static Time valueOf(String s) {
    for (String n : UNIT_NAMES) {
      if (s.contains(n)) {
        int i = s.indexOf(n);
        TimeUnit unit = UNITS_BY_NAME.get(n);
        Assertions.notNull(unit, "Could not find time unit for %s", s.substring(i));
        return new Time(Long.parseLong(s.substring(0, i)), unit);
      }
    }
    return Time.createMillis(Long.parseLong(s));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Time) {
      Time other = (Time) obj;
      return getValueInMillis() == other.getValueInMillis();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (int) getValueInMillis();
  }

  @Override
  public String toString() {
    return new StringBuilder().append(value).append(unit).toString();
  }
}