package org.sapia.ubik.util;

/**
 * Functional interface that specifies no argument(s).
 * 
 */
public interface NoArgFunc<R> {
  
  /**
   * @return an arbitrary value.
   */
  public R call();
  
}
