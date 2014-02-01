package org.sapia.ubik.mcast.control.heartbeat;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.control.ControlNotificationFactory;
import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.mcast.control.ControlResponseHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Props;

/**
 * This class holds logic for handling {@link HeartbeatResponse}s.
 * 
 * @see HeartbeatRequest
 * @author yduchesne
 * 
 */
public class HeartbeatResponseHandler implements ControlResponseHandler {

  /**
   * Property used in development to test handling of unresponding nodes. Do not
   * use otherwise.
   */
  private static final String HEARTBEAT_PING_DISABLED = "ubik.rmi.naming.mcast.heartbeat.ping.disabled";

  private Category log = Log.createCategory(getClass());

  private int maxPingAttempts;
  private long pingInterval;
  private ControllerContext context;
  private Set<String> targetedNodes;
  private Set<String> replyingNodes = new HashSet<String>();
  private boolean pingDisabled = false;
  private boolean forceResync = true;

  /**
   * @param context
   *          the {@link ControllerContext}
   * @param targetedNodes
   *          the identifiers of the nodes that had originally targeted by the
   *          heartbeat request.
   */
  public HeartbeatResponseHandler(ControllerContext context, Set<String> targetedNodes) {
    this.context = context;
    this.targetedNodes = targetedNodes;
    Props sysProps = Props.getSystemProperties();
    maxPingAttempts = sysProps.getIntProperty(Consts.MCAST_MAX_PING_ATTEMPTS, Defaults.DEFAULT_PING_ATTEMPTS);
    pingInterval = sysProps.getLongProperty(Consts.MCAST_PING_INTERVAL, Defaults.DEFAULT_PING_INTERVAL);
    pingDisabled = sysProps.getBooleanProperty(HEARTBEAT_PING_DISABLED, false);
    forceResync = sysProps.getBooleanProperty(Consts.MCAST_HEARTBEAT_FORCE_RESYNC, true);
  }

  /**
   * @returns <code>true</code> if the given response is a
   *          {@link HeartbeatResponse}.
   */
  @Override
  public boolean accepts(ControlResponse response) {
    return response instanceof HeartbeatResponse;
  }

  /**
   * @param originNode
   *          the identifier of the node from which the response originates.
   * @param response
   *          a {@link ControlResponse}, expected to be a
   *          {@link HeartbeatResponse}.
   * @return <code>true</code> if all expected responses have been received,
   *         false otherwise.
   */
  @Override
  public synchronized boolean handle(String originNode, ControlResponse response) {
    if (response instanceof HeartbeatResponse) {
      HeartbeatResponse heartbeatRs = (HeartbeatResponse) response;
      log.debug("Received heartbeat response from %s", originNode);
      context.getChannelCallback().heartbeat(originNode, heartbeatRs.getUnicastAddress());
      replyingNodes.add(originNode);
      if (replyingNodes.size() >= targetedNodes.size()) {
        log.debug("All expected heartbeats received");

        context.notifyHeartbeatCompleted(targetedNodes.size(), replyingNodes.size());
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
    } else {
      log.info("Received %s/%s responses (dead nodes detected)", replyingNodes.size(), targetedNodes.size());

      // those nodes that have replied or removed from the original set of
      // targeted nodes,
      // which then holds the nodes that haven't replied.

      int expectedCount = targetedNodes.size();
      targetedNodes.removeAll(replyingNodes);

      log.debug("Sending synchronous ping requests");
      Set<String> responding = doSendPing();

      log.warning("Got %s/%s nodes that responded to the last resort ping", responding.size(), targetedNodes.size());
      log.warning("Missing nodes will be removed");

      targetedNodes.removeAll(responding);
      replyingNodes.addAll(responding);

      if (!targetedNodes.isEmpty()) {
        log.info("Got %s down nodes", targetedNodes.size());
        for (String down : targetedNodes) {
          context.getChannelCallback().down(down);
        }
      } else {
        log.debug("All nodes responded, no down nodes detected");
      }

      context.getChannelCallback().sendNotification(ControlNotificationFactory.createDownNotification(replyingNodes, targetedNodes));

      context.notifyHeartbeatCompleted(expectedCount, replyingNodes.size());
      if (forceResync) {
        log.info("Added %s nodes to purgatory", targetedNodes.size());
        context.getPurgatory().addAll(targetedNodes);
      }
    }
  }

  private Set<String> doSendPing() {
    Set<String> responding = new HashSet<String>();
    Set<String> remainingTargets = new HashSet<String>(targetedNodes);

    if (!pingDisabled) {
      log.debug("Sending ping to %s", remainingTargets);
      for (int i = 0; i < maxPingAttempts && !remainingTargets.isEmpty(); i++) {
        try {
          Set<SynchronousControlResponse> responses = new HashSet<SynchronousControlResponse>();

          responses.addAll(context.getChannelCallback().sendSynchronousRequest(remainingTargets, new PingRequest()));

          Set<String> tmp = Collections2.convertAsSet(responses, new org.sapia.ubik.util.Function<String, SynchronousControlResponse>() {
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
          Thread.sleep(pingInterval);
        } catch (InterruptedException e) {
          log.warning("Thread interrupted, exiting");
          break;
        }
      }
    }

    return responding;
  }
}
