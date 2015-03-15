package org.sapia.ubik.mcast.control.heartbeat;

import java.util.HashSet;
import java.util.Set;

import org.avis.common.RuntimeInterruptedException;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlNotificationFactory;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.Collects;

/**
 * Abstract class implementing base heartbeat response handling logic.
 * 
 * @author yduchesne
 *
 */
public abstract class HeartbeatResponseHandlerSupport {
  
  protected final Category log = Log.createCategory(getClass());

  private Stopwatch pingResponseTime = Stats.createStopwatch(getClass(), "PingResponseTime", "Ping response time");

  private ControllerContext context;
  private Set<String> targetedNodes;
  private Set<String> replyingNodes = new HashSet<String>();
  
  /**
   * @param context
   *          the {@link ControllerContext}
   * @param targetedNodes
   *          the identifiers of the nodes that had originally targeted by the
   *          heartbeat request.
   */
  protected HeartbeatResponseHandlerSupport(ControllerContext context, Set<String> targetedNodes) {
    this.context = context;
    this.targetedNodes = targetedNodes;
  }
  
  Set<String> getTargetedNodes() {
    return targetedNodes;
  }
  
  ControllerContext getContext() {
    return context;
  }

  protected synchronized boolean doHandleResponse(String originNode, ServerAddress originAddress, Set<NodeInfo> upNodes) {
    for (NodeInfo n : upNodes) {
      log.report("Received heartbeat response from %s", n);
      context.getChannelCallback().heartbeatResponse(n.getNode(), n.getAddr());
      replyingNodes.add(n.getNode());
    }
    if (replyingNodes.size() >= targetedNodes.size()) {
      log.report("All expected heartbeats received");

      context.notifyHeartbeatCompleted(targetedNodes.size(), replyingNodes.size());
      return true;
    }
    log.report("Received %s/%s responses thus far...", replyingNodes.size(), targetedNodes.size());
    return false;
  }
  
  protected synchronized void doHandleTimeout() {
    if (replyingNodes.size() >= targetedNodes.size()) {
      log.report("Received %s/%s responses. All expected responses received", replyingNodes.size(), targetedNodes.size());
    } else {
      log.report("Received %s/%s responses (dead nodes detected)", replyingNodes.size(), targetedNodes.size());

      // those nodes that have replied or removed from the original set of
      // targeted nodes,
      // which then holds the nodes that haven't replied.

      int expectedCount = targetedNodes.size();
      targetedNodes.removeAll(replyingNodes);

      log.debug("Sending synchronous ping requests");
     
      Set<String> responding;
      Split split = pingResponseTime.start();
      try {
        responding = doSendPing();
      } finally {
        split.stop();
      }

      log.warning("Got %s/%s nodes that responded to the last resort ping", responding.size(), targetedNodes.size());
      if (log.isTrace()) {
        for (String r : responding) {
          log.trace("  => %s", r);
        }
      }

      targetedNodes.removeAll(responding);
      replyingNodes.addAll(responding);

      if (!targetedNodes.isEmpty()) {
        log.info("Got %s down nodes", targetedNodes.size());
        for (String down : targetedNodes) {
          context.getChannelCallback().down(down);
        }
        context.getChannelCallback().sendNotification(ControlNotificationFactory.createDownNotification(replyingNodes, targetedNodes));
      } else {
        log.debug("All nodes responded, no down nodes detected");
      }

      context.notifyHeartbeatCompleted(expectedCount, replyingNodes.size());
      if (context.getConfig().isForceResync()) {
        if (!targetedNodes.isEmpty()) {
          log.info("Added %s nodes to purgatory", targetedNodes.size());
          context.getPurgatory().addAll(targetedNodes);
        }
      }
    }    
  }
    
  private Set<String> doSendPing() {
    Set<String> responding = new HashSet<String>();
    Set<String> remainingTargets = new HashSet<String>(targetedNodes);

    if (!context.getConfig().isPingDisabled()) {
      log.debug("Sending ping to %s", remainingTargets);
      for (int i = 0; i < context.getConfig().getMaxPingAttempts() && !remainingTargets.isEmpty(); i++) {
        try {
          Set<SynchronousControlResponse> responses = new HashSet<SynchronousControlResponse>();

          responses.addAll(context.getChannelCallback().sendSynchronousRequest(remainingTargets, new PingRequest(), context.getConfig().getSyncResponseTimeout()));

          Set<String> tmp = Collects.convertAsSet(responses, new org.sapia.ubik.util.Func<String, SynchronousControlResponse>() {
            @Override
            public String call(SynchronousControlResponse res) {
              return res.getOriginNode();
            }
          });
          remainingTargets.removeAll(tmp);
          responding.addAll(tmp);

        } catch (Exception e) {
          throw new IllegalStateException("Could not send request", e);
        }
        try {
          Thread.sleep(context.getConfig().getPingInterval().getValueInMillis());
        } catch (InterruptedException e) {
          log.warning("Thread interrupted, exiting");
          throw new RuntimeInterruptedException(e);
        }
      }
    }
    return responding;
  }
}
