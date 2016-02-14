package org.sapia.ubik.util;

public interface Range<T extends Comparable<T>> extends Iterable<T> {

  /**
   * @return this instance's minimum boundary.
   */
  public T getMin();

  /**
   * @return this instance's maximum boundary.
   */
  public T getMax();
  
}
