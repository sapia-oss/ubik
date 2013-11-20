package org.sapia.ubik.util;

/**
 * This interface implements a "condition": a condition is tested against a
 * given object, returning <code>true</code> if it applies, <code>false</code>
 * otherwise.
 * <p>
 * How the outcome of a condition is treated, and in what context it is applied,
 * is arbitrary.
 * 
 * @author yduchesne
 * 
 */
public interface Condition<T> {

  /**
   * @param item
   *          the {@link Object} to test this condition on.
   * @return <code>true</code> if this condition is verified, <code>false</code>
   *         otherwise.
   */
  public boolean apply(T item);

}
