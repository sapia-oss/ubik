package org.sapia.ubik.rmi.server;

/**
 * Thrown by the Ubik RMI runtime when operations are requested and it is
 * shutdown.
 * 
 * @author Yanick Duchesne
 */
public class ShutdownException extends RuntimeRemoteException {

  static final long serialVersionUID = 1L;

  public ShutdownException() {
    super();
  }

  public ShutdownException(String msg) {
    super(msg);
  }
}
