package org.sapia.ubik.mcast.control.master;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.mcast.control.ControlEvent;

/**
 * Event class used as part of master broadcast, and master broadcast ack.
 * 
 * @author yduchesne
 *
 */
public class MasterBroadcastAckEvent extends ControlEvent {
  
  private int nodeCount;
  
  /**
   * Do not call: meant for externalization only.
   */
  public MasterBroadcastAckEvent() {
  }
  
  /**
   * @param nodeCount the number of nodes in the slave's view.
   */
  public MasterBroadcastAckEvent(int nodeCount) {
    this.nodeCount = nodeCount;
  }
  
  /**
   * @return this instance's node count.
   */
  public int getNodeCount() {
    return nodeCount;
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    nodeCount = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(nodeCount);
  }

}
