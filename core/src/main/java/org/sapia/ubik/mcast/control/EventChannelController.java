package org.sapia.ubik.mcast.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.NodeInfo.State;
import org.sapia.ubik.mcast.control.gossip.GossipSyncAckControlEvent;
import org.sapia.ubik.mcast.control.gossip.GossipSyncAckControlEventHandler;
import org.sapia.ubik.mcast.control.gossip.GossipSyncNotification;
import org.sapia.ubik.mcast.control.gossip.GossipSyncNotificationHandler;
import org.sapia.ubik.mcast.control.health.DelegatedHealthCheckControlEvent;
import org.sapia.ubik.mcast.control.health.DelegatedHealthCheckControlEventHandler;
import org.sapia.ubik.mcast.control.health.HealtchCheckConfirmationControlEventHandler;
import org.sapia.ubik.mcast.control.health.HealthCheckConfirmationControlEvent;
import org.sapia.ubik.mcast.control.health.SynchronousHealthCheckRequest;
import org.sapia.ubik.mcast.control.health.SynchronousHealthCheckRequestHandler;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Pause;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.UbikMetrics;

/**
 * Controls the state of an {@link EventChannel} and behaves accordingly. It is
 * mainly in charge of triggering the challenge process at startup.
 * 
 * @author yduchesne
 * 
 */
public class EventChannelController {

  // ==========================================================================
  
  private Category log = Log.createCategory(getClass());

  private ControllerConfiguration config;
  private ControllerContext context;

  private static final long DEFAULT_CTRL_INTERVAL = 15000;
  
  private Map<String, ControlEventHandler> eventHandlers = new HashMap<String, ControlEventHandler>();
  private Map<String, GossipNotificationHandler> gossipHandlers = new HashMap<String, GossipNotificationHandler>();
  private Map<String, ControlNotificationHandler> notificationHandlers = new HashMap<String, ControlNotificationHandler>();
  private Map<String, SynchronousControlRequestHandler> syncRequestHandlers = new HashMap<String, SynchronousControlRequestHandler>();
  private Pause controlInterval, gossipInterval, autoBroadcastInterval;
  
  public EventChannelController(ControllerConfiguration config, EventChannelFacade callback, UbikMetrics metrics) {
    this(SysClock.RealtimeClock.getInstance(), config, callback, metrics);
  }

  public EventChannelController(SysClock clock, ControllerConfiguration config, EventChannelFacade callback, UbikMetrics metrics) {
    this.config = config;
    context = new ControllerContext(callback, clock, config, metrics);

    syncRequestHandlers.put(SynchronousHealthCheckRequest.class.getName(), new SynchronousHealthCheckRequestHandler(context));
    gossipHandlers.put(GossipSyncNotification.class.getName(), new GossipSyncNotificationHandler(context));
    eventHandlers.put(GossipSyncAckControlEvent.class.getName(), new GossipSyncAckControlEventHandler(context));
    eventHandlers.put(DelegatedHealthCheckControlEvent.class.getName(), new DelegatedHealthCheckControlEventHandler(context));
    eventHandlers.put(HealthCheckConfirmationControlEvent.class.getName(), new HealtchCheckConfirmationControlEventHandler(context));
    
    controlInterval = new Pause(clock, DEFAULT_CTRL_INTERVAL);
    gossipInterval  = new Pause(clock, config.getGossipInterval().getValueInMillis());   
    autoBroadcastInterval = new Pause(clock, config.getAutoBroadcastInterval().getValueInMillis());
  }

  ControllerConfiguration getConfig() {
    return config;
  }

  public ControllerContext getContext() {
    return context;
  }

  public void checkStatus() {
    if (gossipInterval.isOver()) {
      doGossip();
      gossipInterval.reset();
    }
    if (controlInterval.isOver()) {
      performControl();
      controlInterval.reset();
    }
    if (config.isAutoBroadcastEnabled() && autoBroadcastInterval.isOver() && context.getEventChannel().getNodeCount() <= config.getAutoBroadcastThreshold()) {
      log.info("Performing auto-broadcast");
      context.getEventChannel().resync();
      autoBroadcastInterval.reset();
    }
  }
  
  public synchronized void onEvent(String originNode, ServerAddress originAddress, ControlEvent event) {
    ControlEventHandler handler = eventHandlers.get(event.getClass().getName());
    if (handler != null) {
      handler.handle(originNode, originAddress, event);
    } else {
      log.error("No request handler for request %s", event);
    }
  }

  public synchronized SynchronousControlResponse onSynchronousRequest(String originNode, ServerAddress originAddress, SynchronousControlRequest request) {
    SynchronousControlRequestHandler handler = syncRequestHandlers.get(request.getClass().getName());
    if (handler != null) {
      return handler.handle(originNode, originAddress, request);
    } else {
      log.error("No request handler for request %s", request);
      return null;
    }
  }

  public synchronized void onNotification(String originNode, ServerAddress originAddress, ControlNotification notification) {
    ControlNotificationHandler handler = notificationHandlers.get(notification.getClass().getName());
    try {
      if (handler != null) {
        handler.handle(originNode, originAddress, notification);
      } else {
        log.error("No notification handler for notification %s; got: %s", notification, notificationHandlers);
      }
    } finally {
      // cascading the notification
      notification.getTargetedNodes().remove(context.getNode());
      context.getEventChannel().sendNotification(notification);
    }
  }
  
  public synchronized void onGossipNotification(String originNode, ServerAddress originAddress, GossipNotification notification) {
    GossipNotificationHandler handler = gossipHandlers.get(notification.getClass().getName());
    if (handler != null) {
      handler.handle(originNode, originAddress, notification);
    } else {
      log.error("No notification handler for notification %s; got: %s", notification, gossipHandlers);
    }
  }

  private void performControl() {
    if (log.isReport()) {
      log.report("Heartbeat timeout set to: %s millis", context.getConfig().getHeartbeatTimeout());
      log.report("Node identifier: %s", context.getEventChannel().getNode());
    }
    doHealth();
    context.getEventChannel().cleanDeadNodes(DEFAULT_CTRL_INTERVAL*3);
  }

  // --------------------------------------------------------------------------
  // gossip
  
  private void doGossip() {
    if (config.isGossipEnabled()) {
      context.getEventChannel().sendGossipNotification(
          new GossipSyncNotification(context.getEventChannel().getView(GossipSyncNotification.NON_SUSPECT_NODES_FILTER))
      );
    }
  }
  
  // --------------------------------------------------------------------------
  // health check
  
  private void doHealth() {
    for (NodeInfo n : context.getEventChannel().getView()) {
      if (n.checkState(context.getConfig().getHeartbeatTimeout().getValueInMillis(), context.getClock()) == State.SUSPECT) {
        doSendTriggerHealthCheckFor(n);
      }
    }
  }
  
  private void doSendTriggerHealthCheckFor(NodeInfo suspect) {
    List<NodeInfo> currentNodes = context.getEventChannel().getView(n -> n.getState() != NodeInfo.State.SUSPECT);
    Set<NodeInfo>  delegates    = new HashSet<>();
    int counter = 0;
    for (NodeInfo n : currentNodes) {
      delegates.add(n);
      counter++;
      if (counter == context.getConfig().getHealthCheckDelegateCount()) {
        break;
      }
    }
    
    if (delegates.isEmpty()) {
      log.info("Node %s is suspect: performing healthcheck", suspect);
      
      try {
        context.getMetrics().incrementCounter("eventChannelController.suspectHealthCheck");
        Set<SynchronousControlResponse> responses = context.getEventChannel().sendSynchronousRequest(
            Collects.arrayToSet(suspect.getNode()), 
            new SynchronousHealthCheckRequest(), 
            context.getConfig().getHealthCheckDelegateTimeout()
        );
        if (responses.isEmpty()) {
          log.info("Received no response for healthcheck on %s (removing from view)", suspect);
          context.getEventChannel().down(suspect.getNode());
        } else {
          suspect.reset(context.getClock());
        }
      } catch (Exception e) {
        log.info("Unexpected error caught during healthcheck of %s (removing from view) - %s", suspect, e.getMessage());
        context.getEventChannel().down(suspect.getNode());
      }
    } else {
      log.info("Node %s is suspect: delegating healthcheck", suspect);
      DelegatedHealthCheckControlEvent event = new DelegatedHealthCheckControlEvent(suspect);
      for (NodeInfo d : delegates) {
        context.getMetrics().incrementCounter("eventChannelController.suspectDelegatedHealthCheck");
        context.getEventChannel().sendUnicastEvent(d.getAddr(), event);
      }
    }
  }
}
