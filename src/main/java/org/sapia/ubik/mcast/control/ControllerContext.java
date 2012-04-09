package org.sapia.ubik.mcast.control;

import java.util.concurrent.atomic.AtomicLong;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.control.EventChannelController.Role;
import org.sapia.ubik.util.Clock;

/**
 * An instance of this class holds an {@link EventChannelController} contextual objects.
 * 
 * @author yduchesne
 *
 */
public class ControllerContext {

	private EventChannelController.Role role;
	private ChannelCallback                  channelCallback;
	private Clock													   clock; 
	private AtomicLong 											 lastHeartbeatReqRcvTime 		= new AtomicLong();
	private AtomicLong 											 lastHeartbeatReqSentTime 	= new AtomicLong();
	private AtomicLong 											 lastChallengeReqRcvTime 		= new AtomicLong();
	private AtomicLong 											 lastChallengeReqSentTime 	= new AtomicLong();
	
	/**
	 * Creates an instance of this class with the given objects.
	 * 
	 * @param callback the {@link ChannelCallback} corresponding to the {@link EventChannel} in the context of which
	 * this instance is created.
	 * @param clock the {@link Clock} instance to use.
	 */
	ControllerContext(
			ChannelCallback callback, 
			Clock clock) {
		this.role 					 = EventChannelController.Role.UNDEFINED;
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
	 * @return this instance's {@link Role}.
	 */
	public synchronized EventChannelController.Role getRole() {
	  return role;
  }
	
	/**
	 * @param role the {@link Role} to assign to this instance.
	 */
	public synchronized void setRole(EventChannelController.Role role) {
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
	

}
