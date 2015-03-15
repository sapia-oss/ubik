package org.sapia.ubik.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Models a time range, consisting of min and max {@link TimeValue}s.
 *
 * @author yduchesne
 *
 */
public class TimeRange implements Externalizable {

  static final long serialVersionUID = 1L;
  
  private TimeValue min, max;

  /**
   * @param min the {@link TimeValue} to use as the lower bound.
   * @param max the {@link TimeValue} to use as the higher bound.
   */
  public TimeRange(TimeValue min, TimeValue max) {
    Assertions.isTrue(
        max.getValueInMillis() >= min.getValueInMillis(),
        "Max time must be greater than or equal to min time (max = %s, min = %s)",
        max, min
    );
    this.min = min;
    this.max = max;
  }

  /**
   * @return this range's lower bound.
   */
  public TimeValue getMin() {
    return min;
  }

  /**
   * @return this range's higher bound.
   */
  public TimeValue getMax() {
    return max;
  }

  /**
   * @return a randomly created {@link TimeValue}, calculated to be with this instance's
   * min and max {@link TimeValue}s.
   */
  public TimeValue getRandomTime() {
    if (min.getValue() == 0 && max.getValue() == 0) {
      return TimeValue.createMillis(0);
    }
    Random r = new Random(System.currentTimeMillis());
    if (min.getValueInMillis() == max.getValueInMillis()) {
      return min;
    }
    int diff = r.nextInt((int) max.getValueInMillis() - (int) min.getValueInMillis());
    return new TimeValue(min.getValueInMillis() + diff, TimeUnit.MILLISECONDS);
  }

  /**
   * Given an interval provided by the given time range literal, returns {@link TimeRange} instance. The following
   * are valid time range literals:
   * <pre>
   * 10000-15000
   * 10000:15000
   * 10000,15000
   * 10000|15000
   * </pre>
   *
   * The above examples show that the dash (-), pipe (|), colon (:), and comma (,) characters can be used to define time ranges.
   * <p>
   * Note that time literals may also be used:
   *
   * <pre>
   * 10s-15s
   * 10s:15s
   * 10s,15s
   * 10s|15s
   * </pre>
   *
   * @param a timeRangeLiteral
   * @return a new {@link TimeRange}.
   */
  public static TimeRange valueOf(String timeRangeLiteral) {
    StringTokenizer tk = new StringTokenizer(timeRangeLiteral, "-|:,");
    TimeValue min, max;
    Assertions.isTrue(tk.hasMoreTokens(), "Expected time literal, got: '%s'", timeRangeLiteral);
    min = TimeValue.valueOf(tk.nextToken());
    if (tk.hasMoreTokens()) {
      max = TimeValue.valueOf(tk.nextToken());
    } else {
      max = min;
    }
    if (min.getValueInMillis() > max.getValueInMillis()) {
      throw new IllegalArgumentException("Invalid time range (min > max): " + timeRangeLiteral);
    }
    return new TimeRange(min, max);
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    min = (TimeValue) in.readObject();
    max = (TimeValue) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(min);
    out.writeObject(max);
  }
  
  // --------------------------------------------------------------------------
  // Object overriddes

  @Override
  public String toString() {
    return Strings.toStringFor(this, "min", min, "max", max);
  }
}
