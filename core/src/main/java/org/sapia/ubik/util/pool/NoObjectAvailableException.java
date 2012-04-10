package org.sapia.ubik.util.pool;

/**
 * Exception thrown by a {@link Pool} instance when no object could be acquired from it.
 *
 * @author Yanick Duchesne
 */
public class NoObjectAvailableException extends RuntimeException {
  
  static final long serialVersionUID = 1L;
  
  /**
   * Constructor for NoObjectAvailableException.
   */
  public NoObjectAvailableException() {
    super();
  }
}
