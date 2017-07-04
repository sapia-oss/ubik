package org.sapia.ubik.util;

/**
 * Functional interface that accepts an argument and specifies
 * no return value.
 * 
 * @author yduchesne
 *
 */
public interface VoidFunc<T> {
  
  /**
   * @param arg an arbitrary argument.
   */
  public void call(T arg);

}
