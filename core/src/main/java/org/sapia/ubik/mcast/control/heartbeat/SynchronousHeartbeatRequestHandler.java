package org.sapia.ubik.mcast.control.heartbeat;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.avis.common.RuntimeInterruptedException;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SplitteableMessage;
import org.sapia.ubik.mcast.control.SynchronousControlRequest;
import org.sapia.ubik.mcast.control.SynchronousControlRequestHandler;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.stats.Stats;

/**
 * This class encapsulates the logic for handling {@link SynchronousHeartbeatRequest}s.
 * 
 * @author yduchesne
 * 
 */
public class SynchronousHeartbeatRequestHandler implements SynchronousControlRequestHandler {

  private Stopwatch syncHeartbeatResponseTime = Stats.createStopwatch(getClass(), "SlaveSyncHeartbeatResponseTime", "Synchronous heartbeat response time");
  
  private Category          log      = Log.createCategory(getClass());
  private ControllerContext context;

  /**
   * @param context
   *          the {@link ControllerContext}.
   */
  public SynchronousHeartbeatRequestHandler(ControllerContext context) {
    this.context = context;
  }

  /**
   * @param originNode
   *          the node from which the request has been cascaded.
   * @param originAddress 
   *          the unicast {@link ServerAddress} of the origin node.
   * @param request
   *          a {@link ControlRequest} (expected to be a
   *          {@link HeartbeatRequest}).
   */
  @Override
  public SynchronousControlResponse handle(String originNode, ServerAddress originAddress, SynchronousControlRequest request) {
    log.info("Receiving heartbeat request from: %s", originNode);
    if (context.getConfig().isIgnoreHeartbeatRequests()) {
      if (context.getRole() == Role.MASTER) {
        log.info("Received heartbeat request from other master node %s, triggering challenge", originNode);
        context.triggerChallenge();
      }
      return null;
    } else {
      context.heartbeatRequestReceived();
      context.getChannelCallback().heartbeatRequest(originNode, originAddress);
      SynchronousHeartbeatRequest req = (SynchronousHeartbeatRequest) request;
      
      // removing self from targets.
      req.getTargetedNodes().remove(context.getNode());
      if (!req.getTargetedNodes().isEmpty()) {
        List<SplitteableMessage>     toSend   = req.split(context.getConfig().getHeartbeatControlMessageSplitSize());
        SynchronousHeartbeatResponse response = new SynchronousHeartbeatResponse(context.getNode(), context.getChannelCallback().getAddress());
        
        log.debug("Cascading heartbeat request splits (got %s splits)", toSend.size());
        
        String[]                      targets  = new String[toSend.size()]; 
        SynchronousHeartbeatRequest[] requests = new SynchronousHeartbeatRequest[toSend.size()];
        
        for (int i = 0; i < toSend.size(); i++) {
          targets[i] = toSend.get(i).getTargetedNodes().iterator().next();
          requests[i] = (SynchronousHeartbeatRequest) toSend.get(i);
          
          log.report("Cascading to %s, which as %s nodes to cascade to", targets[i], requests[i].getTargetedNodes().size());
          if (log.isReport()) {
            for (String t : requests[i].getTargetedNodes()) {
              log.report("  => %s", t);
            }
          }
        }

        Split split = syncHeartbeatResponseTime.start();
        try {
          
          Set<SynchronousControlResponse> responses =  context.getChannelCallback().sendSynchronousRequests(
              targets, requests, context.getConfig().getSyncHeartBeatResponseTimeout()
          );
          log.report("Received %s heartbeat responses", responses.size());
          for (SynchronousControlResponse r : responses) {
            SynchronousHeartbeatResponse hr = (SynchronousHeartbeatResponse) r;
            response.getHealthyNodes().addAll(hr.getHealthyNodes());
            if (log.isReport()) {
              for (NodeInfo n : hr.getHealthyNodes()) {
                log.report("  ==> healthy node %s", n);
              }
            }
          }
        } catch (IOException e) {
          log.error("Could not send heartbeat requests", e);
        } catch (InterruptedException e) {
          throw new RuntimeInterruptedException(e);
        } finally{
          split.stop();
        }
      
        log.debug("Finished cascading heartbeat request splits");
   
        if (context.getRole() == Role.MASTER) {
          log.info("Received heartbeat request from other master node %s, triggering challenge", originNode);
          context.triggerChallenge();
        } 
        return response;

      } else {
        log.debug("Not targets left to dispatch to: returning response immediately");
        if (context.getRole() == Role.MASTER) {
          log.info("Received heartbeat request from other master node %s, triggering challenge", originNode);
          context.triggerChallenge();
        }
        
        SynchronousHeartbeatResponse response = new SynchronousHeartbeatResponse(context.getNode(), context.getChannelCallback().getAddress());
        return response;
      }
      
    }
  }


}
