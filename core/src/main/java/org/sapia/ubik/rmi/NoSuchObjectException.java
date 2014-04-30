package org.sapia.ubik.rmi;

import org.sapia.ubik.rmi.server.RuntimeRemoteException;

/**
 * Thrown when no object could be found on the server-side for a given remote
 * object reference. Analoguous to the JDK RMI's
 * {@link java.rmi.NoSuchObjectException}, except that it inherits
 * {@link RuntimeRemoteException} exception, and thus consists of an unchecked
 * exception.
 * 
 * @author yduchesne
 * 
 */
public class NoSuchObjectException extends RuntimeRemoteException {

  static final long serialVersionUID = 1L;

  /**
   * @param msg
   *          a message.
   */
  public NoSuchObjectException(String msg) {
    super(msg);
  }

}
