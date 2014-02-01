package org.sapia.ubik.mcast.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.ubik.concurrent.SynchronizedRef;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.Purgatory.DownNode;
import org.sapia.ubik.mcast.control.challenge.ChallengeCompletionNotification;
import org.sapia.ubik.mcast.control.challenge.ChallengeCompletionNotificationHandler;
import org.sapia.ubik.mcast.control.challenge.ChallengeRequest;
import org.sapia.ubik.mcast.control.challenge.ChallengeRequestHandler;
import org.sapia.ubik.mcast.control.heartbeat.DownNotification;
import org.sapia.ubik.mcast.control.heartbeat.DownNotificationHandler;
import org.sapia.ubik.mcast.control.heartbeat.HeartbeatRequest;
import org.sapia.ubik.mcast.control.heartbeat.HeartbeatRequestHandler;
import org.sapia.ubik.mcast.control.heartbeat.PingRequest;
import org.sapia.ubik.mcast.control.heartbeat.PingRequestHandler;
import org.sapia.ubik.util.Clock;
import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Delay;
import org.sapia.ubik.util.Function;

/**
 * Controls the state of an {@link EventChannel} and behaves accordingly. It is
 * mainly in charge of triggering the challenge process at startup.
 * 
 * @author yduchesne
 * 
 */
public class EventChannelController {

  // --------------------------------------------------------------------------

  static class PendingResponseState {

    private ControlResponseHandler handler;
    private long requestId;
    private long requestTime;

    public PendingResponseState(ControlResponseHandler handler, long requestId, long requestTime) {
      this.handler = handler;
      this.requestId = requestId;
      this.requestTime = requestTime;
    }

    ControlResponseHandler getHandler() {
      return handler;
    }

    long getRequestId() {
      return requestId;
    }

    public long getRequestTime() {
      return requestTime;
    }

  }

  // ==========================================================================

  private Category log = Log.createCategory(getClass());

  private ControllerConfiguration config;
  private ControllerContext context;

  private SynchronizedRef<PendingResponseState> ref = new SynchronizedRef<PendingResponseState>();
  private Map<String, ControlRequestHandler> requestHandlers = new HashMap<String, ControlRequestHandler>();
  private Map<String, ControlNotificationHandler> notificationHandlers = new HashMap<String, ControlNotificationHandler>();
  private Map<String, SynchronousControlRequestHandler> syncRequestHandlers = new HashMap<String, SynchronousControlRequestHandler>();
  private Delay autoResyncInterval, masterBroadcastInterval;

  public EventChannelController(ControllerConfiguration config, ChannelCallback callback) {
    this(Clock.SystemClock.getInstance(), config, callback);
  }

  public EventChannelController(Clock clock, ControllerConfiguration config, ChannelCallback callback) {
    this.config = config;
    context = new ControllerContext(this, callback, clock);
    requestHandlers.put(ChallengeRequest.class.getName(), new ChallengeRequestHandler(context));
    requestHandlers.put(HeartbeatRequest.class.getName(), new HeartbeatRequestHandler(context));
    syncRequestHandlers.put(PingRequest.class.getName(), new PingRequestHandler(context));
    notificationHandlers.put(DownNotification.class.getName(), new DownNotificationHandler(context));
    notificationHandlers.put(ChallengeCompletionNotification.class.getName(), new ChallengeCompletionNotificationHandler(context));

    autoResyncInterval      = new Delay(clock, config.getResyncInterval());
    masterBroadcastInterval = new Delay(clock, config.getMasterBroadcastInterval());
  }

  ControllerConfiguration getConfig() {
    return config;
  }

  public ControllerContext getContext() {
    return context;
  }

  public void checkStatus() {

    synchronized (ref) {

      // Are there responses pending ? If yes, this check will evaluate to true
      if (ref.isSet()) {
        log.debug("There is already a response handler that is active");
        // checking if pending responses have timed out: if yes, we're
        // discarding the current
        // response handler
        if (context.getClock().currentTimeMillis() - ref.get().getRequestTime() >= config.getResponseTimeout()) {
          log.debug("Response timeout detected, cancelling handling of responses");
          ref.get().getHandler().onResponseTimeOut();
          ref.unset();
          performControl();
        }
      } else {
        log.debug("No response handler currently set, performing control");
        performControl();
      }
    }

  }

  public synchronized void onResponse(String originNode, ControlResponse response) {
    synchronized (ref) {
      if (ref.isUnset()) {
        log.debug("No response handler currently present, discarding response %s", response);
      } else {
        PendingResponseState state = ref.get();
        if (state.getRequestId() != response.getRequestId()) {
          log.debug("Request ID does not match response ID (%s vs %s); discarding response %s", state.getRequestId(), response.getRequestId(),
              response);
        }
        try {
          if (state.getHandler().handle(originNode, response)) {
            ref.set(null);
          }
        } catch (RuntimeException e) {
          log.error("Error caught handling response; discarding response handler", e);
          ref.unset();
        }
      }
    }
  }

  public synchronized void onRequest(String originNode, ControlRequest request) {
    ControlRequestHandler handler = requestHandlers.get(request.getClass().getName());
    try {
      if (handler != null) {
        handler.handle(originNode, request);
      } else {
        log.error("No request handler for request %s", request);
      }
    } finally {
      // cascading the request
      request.getTargetedNodes().remove(context.getNode());
      context.getChannelCallback().sendRequest(request);
    }
  }

  public synchronized SynchronousControlResponse onSynchronousRequest(String originNode, SynchronousControlRequest request) {
    SynchronousControlRequestHandler handler = syncRequestHandlers.get(request.getClass().getName());
    if (handler != null) {
      return handler.handle(originNode, request);
    } else {
      log.error("No request handler for request %s", request);
      return null;
    }
  }

  public synchronized void onNotification(String originNode, ControlNotification notification) {
    ControlNotificationHandler handler = notificationHandlers.get(notification.getClass().getName());
    try {
      if (handler != null) {
        handler.handle(originNode, notification);
      } else {
        log.error("No notification handler for notification %s; got: %s", notification, notificationHandlers);
      }
    } finally {
      // cascading the notification
      notification.getTargetedNodes().remove(context.getNode());
      context.getChannelCallback().sendNotification(notification);
    }
  }

  private void performControl() {
    log.debug("*********** Current role is %s ***********", context.getRole());
    switch (context.getRole()) {

    // Role is currently undefined, proceeding to challenge
    case UNDEFINED:
      doTriggerChallenge(false);
      break;

    // ----------------------------------------------------------------------

    // Challenge is on-going: this node has deemed it's the master, it has sent
    // a ChallengeRequest and is waiting for the corresponding
    // ChallengeResponses.
    // We're not doing anything until the current response handler completes.
    case MASTER_CANDIDATE:
      break;

    // ----------------------------------------------------------------------

    // This is the master: it is sending a heartbeat request and creating a
    // matching
    // response handler.
    case MASTER:

      // forcing resync of this node if need be
      if (context.getChannelCallback().getNodes().isEmpty() && autoResyncInterval.isOver()) {
        log.debug("Node appears alone in the cluster, forcing a resync");
        context.getChannelCallback().resync();
        autoResyncInterval.reset();
      } else {

        // either resynching or sending master broadcast
        if (context.getChannelCallback().getNodes().size() <= config.getResyncNodeCount() && autoResyncInterval.isOver()) {
          log.info("Number of peers deemed not enough, forcing a resync");
          context.getChannelCallback().resync();
          autoResyncInterval.reset();
        } else if (masterBroadcastInterval.isOver() && config.isMasterBroadcastEnabled()) {
          context.getChannelCallback().triggerMasterBroadcast();
        }

        // broadcast "force resync" events to all nodes in the purgatory
        if (context.getPurgatory().size() > 0) {
          log.info("Got %s nodes in purgatory", context.getPurgatory().size());
          List<Set<DownNode>> batches = Collections2.splitAsSets(context.getPurgatory().getDownNodes(), config.getForceResyncBatchSize());
          for (Set<DownNode> batch : batches) {
            context.getChannelCallback().forceResyncOf(Collections2.convertAsSet(batch, new Function<String, DownNode>() {
              @Override
              public String call(DownNode arg) {
                log.debug("Forcing resync for node in purgatory: %s", arg.getNode());
                arg.attempt();
                return arg.getNode();
              }
            }));
          }
          Set<String> purged = context.getPurgatory().clear(config.getForceResyncAttempts());
          if (!purged.isEmpty() && log.isInfo()) {
            log.info("Purged nodes from purgatory (those are definitely lost):");
            for (String p : purged) {
              log.info(p);
            }
          }
        }
        ControlRequest heartbeatRq = ControlRequestFactory.createHeartbeatRequest(context);
        ControlResponseHandler heartbeatHandler = ControlResponseHandlerFactory.createHeartbeatResponseHandler(context, new HashSet<String>(context
            .getChannelCallback().getNodes()));

        ref.set(new PendingResponseState(heartbeatHandler, heartbeatRq.getRequestId(), context.getClock().currentTimeMillis()));
        context.heartbeatRequestSent();
        log.debug("Sending heartbeat request to %s", heartbeatRq.getTargetedNodes());
        context.getChannelCallback().sendRequest(heartbeatRq);
      }
      break;

    // ----------------------------------------------------------------------

    // This node is a slave: has it received a heartbeat request "lately" ? If
    // no,
    // we're triggering a challenge: the master may be down.
    default: // SLAVE
      if (context.getChannelCallback().getNodes().isEmpty() && autoResyncInterval.isOver()) {
        log.debug("Node appears alone in the cluster, forcing a resync");
        context.getChannelCallback().resync();
        autoResyncInterval.reset();
      } else if (context.getClock().currentTimeMillis() - context.getLastHeartbeatRequestReceivedTime() >= config.getHeartbeatTimeout()) {
        if (context.getChannelCallback().getNodes().size() <= config.getResyncNodeCount() && autoResyncInterval.isOver()) {
          log.debug("Number of peers deemed not enough, forcing a resync");
          context.getChannelCallback().resync();
          autoResyncInterval.reset();
        }
        log.info("Heartbeat request has not been received in timely manner since last time, triggering challenge");
        doTriggerChallenge(true);
      }
    }
  }

  void triggerChallenge() {
    context.setRole(Role.MASTER_CANDIDATE);
    log.debug("Node %s triggering challenge", context.getNode());
    ControlRequest challengeRq = ControlRequestFactory.createChallengeRequest(context);
    ControlResponseHandler challengeHandler = ControlResponseHandlerFactory.createChallengeResponseHandler(context, context.getChannelCallback()
        .getNodes());
    ref.set(new PendingResponseState(challengeHandler, challengeRq.getRequestId(), context.getClock().currentTimeMillis()));
    context.challengeRequestSent();
    String masterNode = context.getMasterNode();
    if (masterNode != null) {
      challengeRq.getTargetedNodes().remove(context.getMasterNode());
      ControlRequest challengeRqCopy = ControlRequestFactory.createChallengeRequestCopy(context, challengeRq,
          Collections2.arrayToSet(context.getMasterNode()));
      context.getChannelCallback().sendRequest(challengeRqCopy);
      context.getChannelCallback().sendRequest(challengeRq);
    } else {
      context.getChannelCallback().sendRequest(challengeRq);
    }
  }

  private void doTriggerChallenge(boolean force) {
    List<String> nodes = new ArrayList<String>(context.getChannelCallback().getNodes());

    // Sorting the node identifiers and comparing them to this node's. In the
    // end,
    // the node that comes "first" becomes the master.
    Collections.sort(nodes);
    if (nodes.size() > 0) {
      log.debug("Node %s has %s sibling node(s): %s", context.getNode(), nodes.size(), nodes);
      if (force || context.getNode().compareTo(nodes.get(0)) <= 0) {
        // Master role not yet confirmed, upgrading to candidate for now.
        log.debug("Setting self (%s) to candidate for master", context.getNode());
        context.setRole(Role.MASTER_CANDIDATE);
      }
      // Lonely node: automatically becomes the master
    } else {
      log.debug("No other nodes found, setting self to candidate for master", nodes.size());
      context.setRole(Role.MASTER_CANDIDATE);
    }

    if (context.getRole() == Role.MASTER_CANDIDATE) {
      triggerChallenge();
    } else {
      log.debug("Node %s is setting itelf to slave", context.getNode());
      context.setRole(Role.SLAVE);
    }
  }

}
