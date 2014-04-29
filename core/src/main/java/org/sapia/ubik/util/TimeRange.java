package org.sapia.ubik.util;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Models a time range, consisting of min and max {@link Time}s.
 *
 * @author yduchesne
 *
 */
public class TimeRange {

  private Time min, max;

  /**
   * @param min the {@link Time} to use as the lower bound.
   * @param max the {@link Time} to use as the higher bound.
   */
  public TimeRange(Time min, Time max) {
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
  public Time getMin() {
    return min;
  }

  /**
   * @return this range's higher bound.
   */
  public Time getMax() {
    return max;
  }

  /**
   * @return a randomly created {@link Time}, calculated to be with this instance's
   * min and max {@link Time}s.
   */
  public Time getRandomTime() {
    Random r = new Random(System.currentTimeMillis());
    if (min.getValueInMillis() == max.getValueInMillis()) {
      return min;
    }
    int diff = r.nextInt((int) max.getValueInMillis() - (int) min.getValueInMillis());
    return new Time(min.getValueInMillis() + diff, TimeUnit.MILLISECONDS);
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
    Time min, max;
    Assertions.isTrue(tk.hasMoreTokens(), "Expected time literal, got: '%s'", timeRangeLiteral);
    min = Time.valueOf(tk.nextToken());
    if (tk.hasMoreTokens()) {
      max = Time.valueOf(tk.nextToken());
    } else {
      max = min;
    }
    return new TimeRange(min, max);
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "min", min, "max", max);
  }
}
