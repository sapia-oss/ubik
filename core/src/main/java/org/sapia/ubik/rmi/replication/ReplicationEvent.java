package org.sapia.ubik.rmi.replication;

import org.sapia.ubik.rmi.interceptor.Event;


/**
 * @author Yanick Duchesne
 */
public class ReplicationEvent implements Event {
  
  private ReplicatedCommand cmd;

  /**
   * Creates an instance of this class with the given command.
   */
  public ReplicationEvent(ReplicatedCommand command) {
    cmd = command;
  }

  /**
   * @return the {@link ReplicatedCommand} that this instance holds.
   */
  public ReplicatedCommand getReplicatedCommand() {
    return cmd;
  }
}
