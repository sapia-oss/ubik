package org.sapia.ubik.mcast.control;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.sapia.ubik.mcast.control.challenge.ChallengeRequest;
import org.sapia.ubik.mcast.control.heartbeat.HeartbeatRequest;
import org.sapia.ubik.mcast.control.heartbeat.PingRequest;

/**
 * The factory of {@link ControlRequest}s.
 * 
 * @author yduchesne
 *
 */
class ControlRequestFactory {
	
	private static AtomicLong counter = new AtomicLong();

	/**
	 * @param context the {@link ControllerContext}.
	 * @return a new {@link HeartbeatRequest}.
	 */
	static ControlRequest createHeartbeatRequest(ControllerContext context) {
		ControlRequest req = new HeartbeatRequest(
				context.getClock(), 
				createId(), 
				context.getNode(), 
				context.getChannelCallback().getAddress(),
				context.getChannelCallback().getNodes());
		return req;
	}

	/**
	 * @param context the {@link ControllerContext}.
	 * @return a new {@link ChallengeRequest}.
	 */
	static ControlRequest createChallengeRequest(ControllerContext context) {
		ControlRequest req = new ChallengeRequest(
				context.getClock(), 
				createId(), 
				context.getNode(), 
				context.getChannelCallback().getAddress(),
				context.getChannelCallback().getNodes());
		return req;
	}
	
  /**
   * @param context the {@link ControllerContext}.
   * @return a new {@link ChallengeRequest}.
   */
  static ControlRequest createChallengeRequestCopy(ControllerContext context, ControlRequest request, Set<String> targetedNodes) {
    ControlRequest req = new ChallengeRequest(
        context.getClock(), 
        request.getRequestId(), 
        context.getNode(), 
        context.getChannelCallback().getAddress(),
        targetedNodes);
    return req;
  }	
	
	/**
	 * @param context the {@link ControllerContext}.
	 * @return a new {@link PingRequest}.
	 */
	static SynchronousControlRequest createPingRequest(ControllerContext context) {
		return new PingRequest();
	}
	
	private static synchronized long createId() {
		long id = counter.incrementAndGet();
		if(id == Long.MAX_VALUE) {
			counter.set(0);
		}
		return id;
	}
}
