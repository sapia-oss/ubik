package org.sapia.ubik.mcast.control.heartbeat;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.avis.common.RuntimeInterruptedException;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SplitteableMessage;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.rmi.server.stats.Stats;

/**
 * A standalone senders for {@link SynchronousHeartbeatRequest}s.
 * 
 * @author yduchesne
 *
 */
public class SynchronousHeartbeatRequestSender extends HeartbeatResponseHandlerSupport {
  
  private Stopwatch syncHeartbeatResponseTime = Stats.createStopwatch(getClass(), "MasterSyncHeartbeatResponseTime", "Synchronous heartbeat response time");

  /**
   * @param context the {@link ControllerContext} to use corresponding to the event channel in the context
   * of which this instance is used.
   */
  public SynchronousHeartbeatRequestSender(ControllerContext context) {
    super(context, context.getChannelCallback().getNodes());
  }

  public void sendHearbeatRequest() {
    
    SynchronousHeartbeatRequest request = new SynchronousHeartbeatRequest(getTargetedNodes());
    
    if (request.getTargetedNodes().isEmpty()) {
      log.debug("No nodes in cluster. Not sending heartbeat request");
      return;
    } else if (log.isTrace()) {
      log.trace("Initial target nodes:");
      for (String t : request.getTargetedNodes()) {
        log.trace("  ==> %s", t);
      }
    }
    
    try {
      List<SplitteableMessage> toSend = request.split(getContext().getConfig().getHeartbeatControlMessageSplitSize());
      
      log.debug("Cascading heartbeat request splits (got %s splits)", toSend.size());
      
      String[]                      targets  = new String[toSend.size()]; 
      SynchronousHeartbeatRequest[] requests = new SynchronousHeartbeatRequest[toSend.size()];
      
      for (int i = 0; i < toSend.size(); i++) {
        targets[i] = toSend.get(i).getTargetedNodes().iterator().next();
        requests[i] = (SynchronousHeartbeatRequest) toSend.get(i);
        requests[i].getTargetedNodes().remove(targets[i]); 
        log.report("Cascading to %s, which has %s nodes to cascade to", targets[i], requests[i].getTargetedNodes().size());
        if (log.isReport()) {
          for (String t : requests[i].getTargetedNodes()) {
            log.report("  => %s", t);
          }
        }
      }

      int healthyNodeCount = 0;
      Set<SynchronousControlResponse> responses; 
      Split split = syncHeartbeatResponseTime.start();
      try {
        responses = getContext().getChannelCallback().sendSynchronousRequests(
            targets, requests, getContext().getConfig().getSyncHeartBeatResponseTimeout()
        );
      } finally {
        split.stop();
      }
      
      log.report("Got %s sync responses: ", responses.size());
      for (SynchronousControlResponse r : responses) {
        SynchronousHeartbeatResponse hr = (SynchronousHeartbeatResponse) r;
        healthyNodeCount += hr.getHealthyNodes().size();
        log.report("  ==> Got sync response from: %s", r.getOriginAddress());
        if (log.isReport()) {
          for (NodeInfo n : hr.getHealthyNodes()) {
            log.report("    ==> healthy node %s", n);
          }
        }
        doHandleResponse(hr.getOriginNode(), hr.getOriginAddress(), hr.getHealthyNodes());
      }
      log.debug("Received %s responses, got %s healthy nodes confirmed", responses.size(), healthyNodeCount);
      doHandleTimeout();
    } catch (IOException e) {
      log.error("Error caught sending synchronous ping request", e);
    } catch (InterruptedException e) {
      throw new RuntimeInterruptedException(e);
    }
  }
}
