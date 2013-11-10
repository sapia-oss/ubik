package org.sapia.ubik.mcast.control;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.util.Clock;


/**
 * An instance of this class holds an {@link EventChannelController} contextual objects.
 * 
 * @author yduchesne
 *
 */
public class ControllerContext {

	private List<EventChannelControllerListener> listeners = new CopyOnWriteArrayList<EventChannelControllerListener>();
	
	private Role 						role;
	private Purgatory       purgatory                 = new Purgatory();
	private EventChannelController controller;
	private ChannelCallback channelCallback;
	private Clock						clock; 
	private AtomicLong 			lastHeartbeatReqRcvTime 	= new AtomicLong();
	private AtomicLong 			lastHeartbeatReqSentTime 	= new AtomicLong();
	private AtomicLong 			lastChallengeReqRcvTime 	= new AtomicLong();
	private AtomicLong 			lastChallengeReqSentTime 	= new AtomicLong();
	private String          masterNode;
	
	/**
	 * Creates an instance of this class with the given objects.
	 * 
	 * @param the {@link EventChannelController} to which this instance corresponds.
	 * @param callback the {@link ChannelCallback} corresponding to the {@link EventChannel} in the context of which
	 * this instance is created.
	 * @param clock the {@link Clock} instance to use.
	 */
	ControllerContext(
	    EventChannelController controller,
			ChannelCallback callback, 
			Clock clock) {
	  this.controller      = controller;
		this.role 					 = Role.UNDEFINED;
		this.channelCallback = callback;
		this.clock           = clock;
		lastHeartbeatReqRcvTime.set(clock.currentTimeMillis());
  }
	
	/**
	 * @return the identifier of the node of this instance's related event channel.
	 * @see EventChannel#getNode()
	 */
	public String getNode() {
	  return channelCallback.getNode();
  }
	
	/**
	 * @return the {@link ChannelCallback} to which this instance corresponds.
	 */
	public ChannelCallback getChannelCallback() {
	  return channelCallback;
  }
	
	/**
	 * @return the {@link Purgatory}.
	 */
	public Purgatory getPurgatory() {
    return purgatory;
  }
	
	/**
	 * @return this instance's {@link Role}.
	 */
	public synchronized Role getRole() {
	  return role;
  }
	
	/**
	 * @param role the {@link Role} to assign to this instance.
	 */
	public synchronized void setRole(Role role) {
	  if (role == Role.MASTER) {
	    masterNode = null;
	  }
	  this.role = role;
  }
	
	/**
	 * @return the {@link Clock} that this instance uses.
	 */
	public Clock getClock() {
	  return clock;
  }
	
	/**
	 * @return the time (in millis) at which the last heartbeat request was received.
	 */
	public long getLastHeartbeatRequestReceivedTime() {
	  return lastHeartbeatReqRcvTime.get();
  }
	
	/**
	 * Internally updates the time at which the last heartbeat request was received.
	 * @see #lastHeartbeatRcvTime
	 */
	public void heartbeatRequestReceived() {
		lastHeartbeatReqRcvTime.set(clock.currentTimeMillis());
	}
	
	/**
	 * @return the time (in millis) at which the last heartbeat request was sent.
	 */
	public long getLastHeartbeatRequestSentTime() {
	  return lastHeartbeatReqSentTime.get();
  }
	
	/**
	 * Internally updates the time at which the last heartbeat request was sent.
	 * @see #lastHeartbeatSentTime
	 */
	public void heartbeatRequestSent() {
		lastHeartbeatReqSentTime.set(clock.currentTimeMillis());
	}
	
	/**
	 * @return the time (in millis) at which the last challenge request was triggered.
	 */
	public long getLastChallengeRequestSentTime() {
	  return lastChallengeReqSentTime.get();
  }
	
	/**
	 * Internally updates the time at which the last challenge request was sent.
	 * @see #lastChallengeReqSentTime.
	 */
	public void challengeRequestSent() {
		lastChallengeReqSentTime.set(clock.currentTimeMillis());
	}
	
	/**
	 * @return the time (in millis) at which the last challenge request was received.
	 */
	public long getLastChallengeRequestReceivedTime() {
	  return lastChallengeReqRcvTime.get();
  }
	
	/**
	 * Internally updates the time at which the last challenge request was received.
	 * @see #lastChallengeReqSentTime.
	 */
	public void challengeRequestReceived() {
		lastChallengeReqRcvTime.set(clock.currentTimeMillis());
	}
	
	/**
	 * Internally notifies this instance's listeners that the challenge to elect 
	 * the master node has completed.
	 */
	public void notifyChallengeCompleted() {
		for (EventChannelControllerListener listener : listeners) {
			listener.onChallengeCompleted(this);
		}
	}

	/**
	 * Internally notifies this instance's listeners that the challenge to elect 
	 * the master node has completed.
	 */
	public void notifyHeartbeatCompleted(int expectedCount, int effectiveCount) {
		for (EventChannelControllerListener listener : listeners) {
			listener.onHeartBeatCompleted(this, expectedCount, effectiveCount);
		}
	}
	
	/**
	 * Adds the given listener to this instance.
	 * 
	 * @param listener an {@link EventChannelControllerListener} to add to this instance.
	 */
	public void addControllerListener(EventChannelControllerListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param listener an {@link EventChannelControllerListener} to remove from this instance.
	 */
	public void removeControllerListener(EventChannelControllerListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Forces the challenge to determine the master.
	 */
	public void triggerChallenge() {
	  controller.triggerChallenge();
	}
	
	/**
	 * @param masterNode the ID of the master node.
	 */
	public synchronized void setMasterNode(String masterNode) {
	  this.masterNode = masterNode;
	}
	
	/**
	 * @return the ID of the node that is currently the master 
	 * (will be <code>null</code> if no heartbeat request has yet been received).
	 */
	public synchronized String getMasterNode() {
    return masterNode;
  }
}
