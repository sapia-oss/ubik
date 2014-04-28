package org.sapia.ubik.util;

import java.util.Random;
import java.util.StringTokenizer;


/**
 * Provides time-related utility methods.
 *
 * @author yduchesne
 *
 */
public class TimeUtil {

  private TimeUtil() {
  }

  /**
   * Given an interval provided by two {@link Time} instances, returns a random {@link Time} within that interval.
   *
   * @param min the minimum {@link Time}.
   * @param max the maximum {@link Time}.
   * @return a new {@link Time}, randomly created to be within the given interval.
   */
  public static Time createRandomTime(Time min, Time max) {
    if (min.equals(max)) {
      return min;
    }
    Assertions.isFalse(max.getValueInMillis() < min.getValueInMillis(),
        "Max time must be greater than or equal to min time (max = %s, min = %s)", max, min);
    Random rand = new Random(System.currentTimeMillis());
    int diff = rand.nextInt((int) max.getValueInMillis() - (int) min.getValueInMillis());
    Time interval = Time.createMillis(min.getValueInMillis() + diff);
    return interval;
  }

  /**
   * Given an interval provided by the given time range literal, returns a random {@link Time} within that interval. The following
   * are valid time ranges:
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
   * @param timeRangeLiteral
   * @return a new {@link Time}, randomly created to be within the given interval.
   */
  public static Time parseRandomTime(String timeRangeLiteral) {
    StringTokenizer tk = new StringTokenizer(timeRangeLiteral, "-|:,");
    Time min, max;
    Assertions.isTrue(tk.hasMoreTokens(), "Expected time literal, got: '%s'", timeRangeLiteral);
    min = Time.valueOf(tk.nextToken());
    if (tk.hasMoreTokens()) {
      max = Time.valueOf(tk.nextToken());
    } else {
      return min;
    }
    return createRandomTime(min, max);
  }

}
