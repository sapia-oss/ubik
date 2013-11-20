package org.sapia.ubik.rmi.server.command;

import java.util.List;

/**
 * A colocated {@link ResponseSender} that dispatches {@link Response}s to the
 * in-memory {@link CallbackResponseQueue}.
 * 
 * @author Yanick Duchesne
 */
class ColocatedResponseSender implements ResponseSender {

  private CallbackResponseQueue queue;

  ColocatedResponseSender(CallbackResponseQueue queue) {
    this.queue = queue;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.ResponseSender#sendResponses(Destination,
   *      java.util.List)
   */
  public void sendResponses(Destination dest, List<Response> responses) {
    queue.onResponses(responses);
  }
}
