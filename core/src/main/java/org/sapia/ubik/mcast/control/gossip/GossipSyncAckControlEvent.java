package org.sapia.ubik.mcast.control.gossip;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;

public class GossipSyncAckControlEvent extends ControlEvent {
  
  private List<NodeInfo> view;

  /**
   * DO NOT CALL: meant for externalization
   */
  public GossipSyncAckControlEvent() {
  }
  
  public GossipSyncAckControlEvent(List<NodeInfo> view) {
    this.view = view;
  }
  
  public List<NodeInfo> getView() {
    return view;
  }
 
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    view = (List<NodeInfo>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(view);
  }

}
