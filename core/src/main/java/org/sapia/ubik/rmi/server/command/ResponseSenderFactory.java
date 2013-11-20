package org.sapia.ubik.rmi.server.command;

/**
 * Specifies the behavior of factory that create {@link ResponseSender}s.
 * 
 * @author yduchesne
 * 
 */
public interface ResponseSenderFactory {

  /**
   * @param destination
   *          a {@link Destination}.
   * @return a {@link ResponseSender}.
   */
  public ResponseSender getResponseSenderFor(Destination destination);
}
