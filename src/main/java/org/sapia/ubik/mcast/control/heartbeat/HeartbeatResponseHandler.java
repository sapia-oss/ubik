package org.sapia.ubik.mcast.control.heartbeat;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.control.ControlNotificationFactory;
import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.mcast.control.ControlResponseHandler;
import org.sapia.ubik.mcast.control.ControllerContext;

/**
 * This class holds logic for handling {@link HeartbeatResponse}s.
 * 
 * @see HeartbeatRequest
 * @author yduchesne
 *
 */
public class HeartbeatResponseHandler implements ControlResponseHandler {
	
	private Category log = Log.createCategory(getClass());
	
	private ControllerContext context;
	private Set<String> 		  targetedNodes;
	private Set<String> 		  replyingNodes  = new HashSet<String>();
	
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
	public void onResponseTimeOut() {
		if(replyingNodes.size() < targetedNodes.size()) {
			log.debug("Received %s/%s responses (dead nodes detected)", replyingNodes.size(), targetedNodes.size());
			
			// those nodes that have replied or removed from the original set of targeted nodes,
			// which then holds the nodes that haven't replied.
			targetedNodes.removeAll(replyingNodes);
			
			for(String down : targetedNodes) {
				context.getChannelCallback().down(down);
			}
			
			context.getChannelCallback().sendNotification(
					ControlNotificationFactory.createDownNotification(replyingNodes, targetedNodes)
			);
			
		}
	}
}
