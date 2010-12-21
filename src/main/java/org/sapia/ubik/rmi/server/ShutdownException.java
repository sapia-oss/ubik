package org.sapia.ubik.rmi.server;


/**
 * Thrown by the Ubik RMI runtime when operations are requested and it
 * is shutdown.
 *
 * @author Yanick Duchesne
 * 8-Sep-2003
 */
public class ShutdownException extends RuntimeException {
  
  static final long serialVersionUID = 1L;
  
  /**
   * Constructor for ShutdownException.
   */
  public ShutdownException() {
    super();
  }

  public ShutdownException(String msg) {
    super(msg);
  }
}
