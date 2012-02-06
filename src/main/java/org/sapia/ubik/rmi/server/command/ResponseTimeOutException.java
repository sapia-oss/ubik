package org.sapia.ubik.rmi.server.command;


/**
 * Thrown when an expected asynchronous response is expected but
 * does not come in before a specified timeout.
 *
 * @author Yanick Duchesne
 */
public class ResponseTimeOutException extends RuntimeException {
  
  static final long serialVersionUID = 1L;
  
  /**
   * Constructor for ResponseTimeOutException.
   */
  public ResponseTimeOutException() {
    super();
  }
}
