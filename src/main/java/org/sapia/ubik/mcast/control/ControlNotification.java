package org.sapia.ubik.mcast.control;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.util.Collections2;

/**
 * A control notification is sent to nodes in order to notify them of certain conditions
 * occurring. It is not sent in response to a request, nor is a response expected for it.
 * 
 * @author yduchesne
 *
 */
public abstract class ControlNotification implements Externalizable {
	
	private Set<String> targetedNodes;

	/**
	 * Meant for externalization
	 */
	public ControlNotification() {
  }
	
	/**
	 * @param targetedNodes the {@link Set} of identifiers of the nodes that are targeted
	 * by the notification.
	 */
	protected ControlNotification(Set<String> targetedNodes) {
	  this.targetedNodes = targetedNodes;
  }
	
	/**
	 * @return this instance's {@link Set} of identifiers of the nodes that are targeted
	 * by the notification.
	 */
	public Set<String> getTargetedNodes() {
	  return targetedNodes;
  }
	
	/**
	 * Splits this notification into multiple other ones, each targeted at a subset of the original targeted notes.
	 * 
	 * @return a {@link List} of {@link ControlNotification}s.
	 */
	public List<ControlNotification> split(int batchSize) {
		List<Set<String>> 				batches 			= Collections2.splitAsSets(targetedNodes, batchSize);
		List<ControlNotification> notifications = new ArrayList<ControlNotification>();
		for(Set<String> batch : batches) {
			notifications.add(getCopy(batch));
		}
		return notifications;
	}
	
	protected abstract ControlNotification getCopy(Set<String> targetedNodes);
	
	@Override
	@SuppressWarnings(value = "unchecked")
	public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
		this.targetedNodes = (Set<String>) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(this.targetedNodes);
	}

}
