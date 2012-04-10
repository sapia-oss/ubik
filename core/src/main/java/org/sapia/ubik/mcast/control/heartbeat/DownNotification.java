package org.sapia.ubik.mcast.control.heartbeat;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import org.sapia.ubik.mcast.control.ControlNotification;

/**
 * A notification that is cascaded to slave nodes that are still up, in order
 * to notify them of which nodes that have been detected as down.
 * 
 * @see DownNotificationHandler
 * 
 * @author yduchesne
 *
 */
public class DownNotification extends ControlNotification implements Externalizable {
	
	private Set<String> downNodes;

	/**
	 * Meant for externalization only
	 */
	public DownNotification() {
  }
	
	private DownNotification(Set<String> targetedNodes) {
		super(targetedNodes);
	}
	
	/**
	 * @param targetedNodes the {@link Set} of identifiers of the nodes that are
	 * targeted by this notification.
	 * @param downNodes the {@link Set} of identifiers corresponding to the nodes that are down.
	 */
  public DownNotification(Set<String> targetedNodes, Set<String> downNodes) {
  	super(targetedNodes);
  	this.downNodes = downNodes;
  }
  
  /**
   * @return the {@link Set} of identifiers corresponding to the nodes that are down.
   */
  public Set<String> getDownNodes() {
	  return downNodes;
  }
  
  @Override
  protected ControlNotification getCopy(Set<String> targetedNodes) {
    DownNotification copy = new DownNotification(targetedNodes);
    copy.downNodes = this.downNodes;
    return copy;
  }
	
  @Override
  @SuppressWarnings(value = "unchecked")
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
  	super.readExternal(in);
  	downNodes = (Set<String>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
  	super.writeExternal(out);
  	out.writeObject(downNodes);
  }
  
}
