package org.sapia.ubik.rmi.server;

/**
 * A runtime exception meant to be inherited by exception classes whose instances
 * must trigger automatic failover on the client side.
 * 
 * @author yduchesne
 *
 */
public class RuntimeRemoteException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public RuntimeRemoteException() {
  }
  
  public RuntimeRemoteException(String msg) {
    super(msg);
  }
  
  public RuntimeRemoteException(String msg, Throwable err) {
    super(msg, err);
  }

}
