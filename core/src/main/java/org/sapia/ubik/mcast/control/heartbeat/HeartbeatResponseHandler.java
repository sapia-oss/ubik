package org.sapia.ubik.mcast.control.heartbeat;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.control.ControlNotificationFactory;
import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.mcast.control.ControlResponseHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.rmi.server.stats.Hits;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;
import org.sapia.ubik.util.Collections2;

/**
 * This class holds logic for handling {@link HeartbeatResponse}s.
 * 
 * @see HeartbeatRequest
 * @author yduchesne
 *
 */
public class HeartbeatResponseHandler implements ControlResponseHandler {
	
	private Category log = Log.createCategory(getClass());
	
	private Hits  downNodesPerHour;
	private Timer heartbeatResponseHandlingDuration;
	private Timer synchronousPingTime;
	private Timer downNotificationSendTime;
	
	private ControllerContext context;
	private Set<String> 		  targetedNodes;
	private Set<String> 		  replyingNodes  = new HashSet<String>();
	private long              creationTime;
	
	/**
	 * @param context the {@link ControllerContext}
	 * @param targetedNodes the identifiers of the nodes that had originally targeted
	 * by the heartbeat request.
	 */
	public HeartbeatResponseHandler(
			ControllerContext context, 
			Set<String> targetedNodes) {
		this.context       = context;
		this.targetedNodes = targetedNodes;
		this.creationTime  = context.getClock().currentTimeMillis();
		
		this.downNodesPerHour =  Stats.getInstance().getHitsBuilder(
        getClass(), 
        "DownNodesPerHour", 
        "The number of nodes detected as down per hour")
        .perHour().build();
		
		this.heartbeatResponseHandlingDuration = Stats.getInstance().createTimer(
				getClass(), 
				"HeartbeatResponseHandlingDuration", "The time taken to receive/handle heartbeat responses from slave nodes");
		
		this.synchronousPingTime = Stats.getInstance().createTimer(
				getClass(), 
				"PingSendTime", 
				"The time take to send/receive a ping request/response"
		);
		
		this.downNotificationSendTime = Stats.getInstance().createTimer(
				getClass(), 
				"DownNotificationSendTime", 
				"The time take to send/receive a down notification"
		);		
  }

	/**
	 * @returns <code>true</code> if the given response is a {@link HeartbeatResponse}.
	 */
	@Override
	public boolean accepts(ControlResponse response) {
		return response instanceof HeartbeatResponse;
	}
	
	/**
	 * @param originNode the identifier of the node from which the response originates.
	 * @param response a {@link ControlResponse}, expected to be a {@link HeartbeatResponse}.
	 * @return <code>true</code> if all expected responses have been received, false otherwise.
	 */
	@Override
	public synchronized boolean handle(String originNode, ControlResponse response) {
		if(response instanceof HeartbeatResponse) {
			HeartbeatResponse heartbeatRs = (HeartbeatResponse)response;
			log.debug("Received heartbeat response from %s", originNode);
			context.getChannelCallback().heartbeat(originNode, heartbeatRs.getUnicastAddress());
			replyingNodes.add(originNode);
			if(replyingNodes.size() >= targetedNodes.size()) {
				log.debug("All expected heartbeats received");
				
				context.notifyHeartbeatCompleted(targetedNodes.size(), replyingNodes.size());
				this.heartbeatResponseHandlingDuration.increase(context.getClock().currentTimeMillis() - creationTime);
				return true;
			}
			log.debug("Received %s/%s responses thus far...", replyingNodes.size(), targetedNodes.size());
			return false;
		} else {
			log.debug("Expected a heartbeat response, got %s", response);
			return false;
		}
	}
	
	@Override
	public synchronized void onResponseTimeOut() {
		if (replyingNodes.size() >= targetedNodes.size()) {
			log.debug("Received %s/%s responses. All expected responses received", replyingNodes.size(), targetedNodes.size());
			this.heartbeatResponseHandlingDuration.increase(context.getClock().currentTimeMillis() - creationTime);
		} else {
			log.debug("Received %s/%s responses (dead nodes detected)", replyingNodes.size(), targetedNodes.size());
			
			// those nodes that have replied or removed from the original set of targeted nodes,
			// which then holds the nodes that haven't replied.
			
			int expectedCount = targetedNodes.size();
			targetedNodes.removeAll(replyingNodes);
			
			Set<SynchronousControlResponse> responses;

			log.debug("Sending synchronous ping requests");

			try {
				synchronousPingTime.start();
				responses = context.getChannelCallback().sendSynchronousRequest(targetedNodes, new PingRequest());
				synchronousPingTime.end();				
			} catch (Exception e) {
				synchronousPingTime.end();				
				throw new IllegalStateException("Could not send request", e);
			}
				
			Set<String> responding = Collections2.convertAsSet(
			  responses, 
			  new org.sapia.ubik.util.Function<String, SynchronousControlResponse>() {
  				public String call(SynchronousControlResponse res) {
  					return res.getOriginNode();
  				}
			  }
			);
			
			log.warning("Got %s/%s nodes that responded to the last resort ping", responding.size(), targetedNodes.size());
			log.warning("Missing nodes will be removed");
				
			targetedNodes.removeAll(responding);
			replyingNodes.addAll(responding);
			
			if (!targetedNodes.isEmpty()) {
				log.debug("Got %s down nodes", targetedNodes.size());
  			for(String down : targetedNodes) {
  				context.getChannelCallback().down(down);
  			}
			} else {
				log.debug("All nodes responded, no down nodes detected");
			}
			
			downNotificationSendTime.start();
			context.getChannelCallback().sendNotification(
					ControlNotificationFactory.createDownNotification(replyingNodes, targetedNodes)
			);
			downNotificationSendTime.end();
			downNodesPerHour.hit(targetedNodes.size());
			
			context.notifyHeartbeatCompleted(expectedCount, replyingNodes.size());
			this.heartbeatResponseHandlingDuration.increase(context.getClock().currentTimeMillis() - creationTime);
		}
	}
}
