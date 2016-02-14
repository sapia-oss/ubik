package org.sapia.ubik.mcast.control;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.ubik.mcast.NodeInfo;

/**
 * A base notification implementing the {@link GossipMessage} interface.
 * 
 * @author yduchesne
 * 
 */
public abstract class GossipNotification implements Externalizable, GossipMessage {
  
  private List<NodeInfo> view;

  /**
   * DO NOT CALL: meant for externalization
   */
  public GossipNotification() {
  }
  
  public GossipNotification(List<NodeInfo> view) {
    this.view          = view;
  }
  
  @Override
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
