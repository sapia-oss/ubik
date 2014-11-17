package org.sapia.ubik.mcast.control.master;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;

/**
 * Event class used as part of master broadcast, and master broadcast ack.
 * 
 * @author yduchesne
 *
 */
public class MasterBroadcastAckEvent extends ControlEvent {
  
  private List<NodeInfo> view;

  /**
   * Do not call: meant for externalization only.
   */
  public MasterBroadcastAckEvent() {
  }
  
  /**
   * @param view the {@link NodeInfo} instances corresponding to the nodes in the slave's view.
   */
  public MasterBroadcastAckEvent(List<NodeInfo> view) {
    this.view = view;
  }
  
  /**
   * @return the nodes at the slave.
   */
  public List<NodeInfo> getSlaveView() {
    return view;
  }
  
  /**
   * @param otherView a {@link Collection} of {@link NodeInfo} instances corresponding to another view.
   * @return <code>true</code> if this instance's view is deemed the same as the one passed in.
   */
  public boolean isSameView(Collection<NodeInfo> otherView) {
    Set<NodeInfo> nodes = new HashSet<>(view);
    return nodes.containsAll(otherView) && nodes.size() == otherView.size();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    view = (List<NodeInfo>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(view);
  }

}
