package org.sapia.ubik.rmi;

/**
 * Thrown when no object could be found on the server-side for a given remote
 * object reference. Analoguous to the JDK RMI's
 * {@link java.rmi.NoSuchObjectException}, except that it inherits
 * {@link RuntimeException} exception, and thus consists of an unchecked
 * exception.
 * 
 * @author yduchesne
 * 
 */
public class NoSuchObjectException extends RuntimeException {

  static final long serialVersionUID = 1L;

  /**
   * @param msg
   *          a message.
   */
  public NoSuchObjectException(String msg) {
    super(msg);
  }

}
