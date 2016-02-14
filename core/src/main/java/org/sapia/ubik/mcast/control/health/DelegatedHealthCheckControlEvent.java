package org.sapia.ubik.mcast.control.health;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;

/**
 * Sent to delegate nodes so that they perform the health check, to make
 * sure network issues that the node requesting the health check may have
 * are worked around.
 * 
 * @author yduchesne
 *
 */
public class DelegatedHealthCheckControlEvent extends ControlEvent {
  
  private NodeInfo toCheck;
  
  /**
   * DO NOT CALL: meant for externalization only
   */
  public DelegatedHealthCheckControlEvent() {
  }
  
  public DelegatedHealthCheckControlEvent(NodeInfo nodeInfo) {
    this.toCheck = nodeInfo;
  }
  
  public NodeInfo getTarget() {
    return toCheck;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    toCheck = (NodeInfo) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(toCheck);
  }

}
