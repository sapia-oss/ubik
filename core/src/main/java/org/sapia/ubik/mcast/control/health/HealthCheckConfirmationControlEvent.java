package org.sapia.ubik.mcast.control.health;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;

/**
 * Sent to the node that originated a {@link DelegatedHealthCheckControlEvent}.
 * 
 * @author yduchesne
 *
 */
public class HealthCheckConfirmationControlEvent extends ControlEvent {

  private NodeInfo suspect;
  private boolean isUp;
  
  public HealthCheckConfirmationControlEvent() {
  }
  
  public HealthCheckConfirmationControlEvent(NodeInfo suspect, boolean isUp) {
    this.suspect = suspect;
    this.isUp = isUp;
  }
  
  /**
   * @return the {@link NodeInfo} corresponding to the node that was checked.
   */
  public NodeInfo getSuspect() {
    return suspect;
  }
  
  /**
   * @return <code>true</code> if the node corresponding to this instance's replied successfully to the health check.
   */
  public boolean isUp() {
    return isUp;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    suspect = (NodeInfo) in.readObject();
    isUp = in.readBoolean();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(suspect);
    out.writeBoolean(isUp);
  }
}
