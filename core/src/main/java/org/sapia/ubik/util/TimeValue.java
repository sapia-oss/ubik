package org.sapia.ubik.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
public class TimeValue implements Externalizable {
  
  static final long serialVersionUID = 1L;

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
  public TimeValue(long value, TimeUnit unit) {
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
    if (unit.equals(TimeUnit.MILLISECONDS)) {
      return value;
    } 
    return TimeUnit.MILLISECONDS.convert(value, unit);
  }

  /**
   * @return this instance's value in seconds.
   */
  public long getValueInSeconds() {
    if (unit.equals(TimeUnit.SECONDS)) {
      return value;
    } 
    return TimeUnit.SECONDS.convert(value, unit);
  }

  /**
   * @param millis
   *          a milliseconds value.
   * @return a new {@link TimeValue}.
   */
  public static TimeValue createMillis(long millis) {
    return new TimeValue(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * @param seconds
   *          a seconds value.
   * @return a new {@link TimeValue}.
   */
  public static TimeValue createSeconds(long seconds) {
    return new TimeValue(seconds, TimeUnit.SECONDS);
  }

  /**
   * @param s a {@link String} corresponding to a {@link TimeValue} literal.
   * @return a new {@link TimeValue}.
   */
  public static TimeValue valueOf(String s) {
    for (String n : UNIT_NAMES) {
      if (s.contains(n)) {
        int i = s.indexOf(n);
        TimeUnit unit = UNITS_BY_NAME.get(n);
        Assertions.notNull(unit, "Could not find time unit for %s", s.substring(i));
        return new TimeValue(Long.parseLong(s.substring(0, i)), unit);
      }
    }
    return TimeValue.createMillis(Long.parseLong(s));
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    this.unit = (TimeUnit) in.readObject();
    this.value = in.readLong();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(unit);
    out.writeLong(value);
  }
  
  // --------------------------------------------------------------------------
  // Object overriddes

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TimeValue) {
      TimeValue other = (TimeValue) obj;
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
    return new StringBuilder().append(value).append(" ").append(unit).toString();
  }
}