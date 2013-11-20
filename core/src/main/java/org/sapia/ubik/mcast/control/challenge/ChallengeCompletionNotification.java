package org.sapia.ubik.mcast.control.challenge;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import org.sapia.ubik.mcast.control.ControlNotification;

/**
 * Sent by a node to notify other nodes that it's set itself has the master
 * 
 * @author yduchesne
 * 
 */
public class ChallengeCompletionNotification extends ControlNotification {

  private String masterNode;

  /** Meant for externalization only */
  public ChallengeCompletionNotification() {
  }

  public ChallengeCompletionNotification(String masterNode, Set<String> targetedNodes) {
    super(targetedNodes);
    this.masterNode = masterNode;
  }

  @Override
  protected ControlNotification getCopy(Set<String> targetedNodes) {
    return new ChallengeCompletionNotification(masterNode, targetedNodes);
  }

  /**
   * @return the identifier of the master node - from which this notification
   *         originates.
   */
  public String getMasterNode() {
    return masterNode;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    masterNode = in.readUTF();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeUTF(masterNode);
  }
}
