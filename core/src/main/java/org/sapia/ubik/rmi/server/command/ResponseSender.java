package org.sapia.ubik.rmi.server.command;

import java.util.List;


/**
 * Specifies the behavior of a sender of {@link Response} instances.
 *
 * @see CommandProcessor#setResponseSender(ResponseSender)
 *
 * @author Yanick Duchesne
 */
public interface ResponseSender {
  /**
   * Sends the given list of responses to the host corresponding to the
   * given destination.
   *
   * @param destination a {@link Destination}.
   * @param responses a {@link List} of {@link Response} objects.
   */
  public void sendResponses(Destination destination, java.util.List<Response> responses);
}
