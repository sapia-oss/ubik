package org.sapia.ubik.util;

/**
 * Specifies a functional interface.
 * 
 * @author yduchesne
 * 
 * @param <R>
 *          the generic type of the function's return value.
 * @param <T>
 *          the generic type of the function's argument.
 */
public interface Function<R, T> {

  /**
   * @param arg
   *          a function argument.
   * @return the function's return value.
   */
  public R call(T arg);

}
