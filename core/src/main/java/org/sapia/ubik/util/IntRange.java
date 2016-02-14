package org.sapia.ubik.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Models a range of integers.
 * 
 * @author yduchesne
 *
 */
public class IntRange implements Range<Integer> {

  private static final int PRIME = 31;
  
  private Integer min, max;
  
  /**
   * Constructs an instance of this class with 0 as min and max values.
   */
  public IntRange() {
    this(0, 0);
  }
  
  /**
   * Constructs an instance of this class with the given min and max values.
   * 
   * @param min a given minimum.
   * @param max a given maximum.
   */
  public IntRange(int min, int max) {
    Assertions.isTrue(min <= max, "Min must be lower than/equal to max (min = %s, max = %s)", min, max);
    this.min = min;
    this.max = max;
  }
  
  @Override
  public Integer getMin() {
    return min;
  }
  
  @Override
  public Integer getMax() {
    return max;
  }
  
  /**
   * @param min a given minimum.
   * @return a new {@link IntRange}, with this instance's maximum, and the 
   * given min value.
   */
  public IntRange withMin(int min) {
    return new IntRange(min, max);
  }
  
  /**
   * @param max a given maximum.
   * @return a new {@link IntRange}, with this instance's minimum, and the 
   * given max value.
   */
  public IntRange withMax(int max) {
    return new IntRange(min, max);
  }
  
  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      int counter = min;
      @Override
      public boolean hasNext() {
        return counter <= max;
      }
      
      @Override
      public Integer next() {
        return counter++;
      }
    };
  }
  
  /**
   * @param transformer a transformer {@link Func} instance.
   * @return a {@link List} with values produced by the given transformer function.
   */
  public <R> List<R> transform(Func<R, Integer> transformer) {
    List<R> toReturn = new ArrayList<>(max - min + 1);
    for (Integer i : this) {
      toReturn.add(transformer.call(i));
    }
    return toReturn;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  @SuppressWarnings("rawtypes")
  public boolean equals(Object obj) {
    if (obj instanceof Range) {
      Range other = (Range) obj;
      return min.equals(other.getMin()) && max.equals(other.getMax());
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return min * PRIME + max * PRIME;
  }
  
  @Override
  public String toString() {
    return new StringBuilder("[")
        .append(min).append(',')
        .append(max).append("]")
        .toString();
  }
}
