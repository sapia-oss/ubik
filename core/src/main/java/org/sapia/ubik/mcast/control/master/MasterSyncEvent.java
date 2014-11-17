package org.sapia.ubik.mcast.control.master;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;

/**
 * Sent by the master when a slave's node count differs.
 * 
 * @author yduchesne
 *
 */
public class MasterSyncEvent extends ControlEvent {
  
  private List<NodeInfo> view;
 
  /**
   * Do not call: meant for externalization only.
   */
  public MasterSyncEvent() {
  }
  
  public MasterSyncEvent(List<NodeInfo> view) {
    this.view = view;
  }
  
  /**
   * @return the {@link List} of {@link NodeInfo} corresponding to the master's view.
   */
  public List<NodeInfo> getMasterView() {
    return view;
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
