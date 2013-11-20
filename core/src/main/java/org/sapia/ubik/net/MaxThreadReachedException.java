package org.sapia.ubik.net;

/**
 * Thrown when the maximum number of threads has been reached.
 * 
 * @author Yanick Duchesne
 */
public class MaxThreadReachedException extends Exception {

  static final long serialVersionUID = 1L;

  /**
   * Constructor for MaxThreadReachedException.
   * 
   * @param arg0
   */
  public MaxThreadReachedException(String arg0) {
    super(arg0);
  }

  /**
   * Constructor for MaxThreadReachedException.
   * 
   * @param arg0
   * @param arg1
   */
  public MaxThreadReachedException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  /**
   * Constructor for MaxThreadReachedException.
   * 
   * @param arg0
   */
  public MaxThreadReachedException(Throwable arg0) {
    super(arg0);
  }

  /**
   * Constructor for MaxThreadReachedException.
   */
  public MaxThreadReachedException() {
    super();
  }
}
