package org.sapia.ubik.mcast;

/**
 * Thrown when a synchronous response is expected but was not received with a
 * given timeframe.
 * 
 * @author Yanick Duchesne
 */
public class TimeoutException extends Exception {

  static final long serialVersionUID = 1L;

  public TimeoutException() {
    super("Response not received");
  }
}
