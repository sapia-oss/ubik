package org.sapia.ubik.mcast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.control.ChannelCallback;
import org.sapia.ubik.mcast.control.ControlNotification;
import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.EventChannelController;
import org.sapia.ubik.mcast.control.SplittableMessage;
import org.sapia.ubik.mcast.control.SynchronousControlRequest;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.mcast.udp.UDPBroadcastDispatcher;
import org.sapia.ubik.mcast.udp.UDPUnicastDispatcher;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Clock;
import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Function;
import org.sapia.ubik.util.Props;
import org.sapia.ubik.util.SoftReferenceList;

/**
 * An instance of this class represents a node in a given logical event channel.
 * Instances of this class are logically grouped on a per-domain basis. Remote
 * events are sent/dispatched to other instances of this class through the
 * network.
 * <p>
 * An instance of this class will only send/received events to/from other
 * instances of the same domain.
 *
 * @see org.sapia.ubik.mcast.DomainName
 * @see org.sapia.ubik.mcast.RemoteEvent
 *
 * @author Yanick Duchesne
 */
public class EventChannel {

  /**
   * This enum holds constants pertaining to an {@link EventChannel}'s role in a
   * domain/cluster.
   */
  public enum Role {

    UNDEFINED, MASTER_CANDIDATE, MASTER, SLAVE;

    public boolean isMaster() {
      return this == MASTER;
    }
  }

  /** Internal state */
  private enum State {
    CREATED, STARTED, CLOSED;
  }

  private static class EventChannelRefImpl implements EventChannelRef {

    private EventChannel owner;
    private boolean shouldCloseAndStart;

    private EventChannelRefImpl(EventChannel owner, boolean shouldCloseAndStart) {
      this.owner = owner;
      this.shouldCloseAndStart = shouldCloseAndStart;
    }

    @Override
    public void start() throws IOException {
      if (shouldCloseAndStart && !owner.isStarted() && !owner.isClosed()) {
        owner.start();
      }
    }

    @Override
    public void close() {
      if (shouldCloseAndStart && !owner.isClosed()) {
        owner.close();
      }
    }

    @Override
    public EventChannel get() {
      return owner;
    }

    @Override
    public int hashCode() {
      return owner.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof EventChannelRef) {
        return ((EventChannelRef) obj).equals(owner);
      }
      return false;
    }
  }

  // ==========================================================================

  private static final String DEFAULT_DOMAIN_NAME = "default";

  /**
   * Sent by a node when it receives a publish event. Allows discovery by the
   * node that just published itself.
   */
  static final String DISCOVER_EVT = "ubik/mcast/discover";

  /**
   * Sent at startup when a node first appears.
   */
  static final String PUBLISH_EVT = "ubik/mcast/publish";

  /**
   * Sent when a node (or set of nodes) are detected as down, and a last attempt
   * is being made to rediscover them - nodes that receive this event should try
   * to resync themselves.
   */
  static final String FORCE_RESYNC_EVT = "ubik/mcast/forceResync";

  /**
   * Sent by a node to notify other nodes that it is shutting down.
   */
  static final String SHUTDOWN_EVT = "ubik/mcast/shutdown";

  /**
   * Sent by a master node periodically.
   */
  static final String MASTER_BROADCAST = "ubik/mcast/master/broadcast";

  /**
   * Sent back as ack to master broadcast.
   */
  static final String MASTER_BROADCAST_ACK = "ubik/mcast/master/broadcast/ack";

  /**
   * Corresponds to all types of control events.
   */
  static final String CONTROL_EVT = "ubik/mcast/control";

  private static final int MIN_STARTUP_DELAY = 5000;
  private static final int MIN_STARTUP_DELAY_OFFSET = 10000;

  private static final int DEFAULT_MAX_PUB_ATTEMPTS = 3;
  private static final long MIN_PUB_INTERVAL = 2000;
  private static final int MIN_PUB_OFFSET = 5000;

  private static Set<EventChannel> CHANNELS_BY_DOMAIN = Collections.synchronizedSet(new HashSet<EventChannel>());

  private Category log = Log.createCategory(getClass());
  private Timer heartbeatTimer = new Timer("Ubik.EventChannel.Timer", true);
  private BroadcastDispatcher broadcast;
  private UnicastDispatcher unicast;
  private EventConsumer consumer;
  private ChannelEventListener listener;
  private View view = new View();
  private EventChannelController controller;
  private int controlBatchSize;
  private ServerAddress address;
  private SoftReferenceList<DiscoveryListener> discoListeners = new SoftReferenceList<DiscoveryListener>();
  private volatile State state = State.CREATED;
  private int maxPublishAttempts = DEFAULT_MAX_PUB_ATTEMPTS;
  private long publishInterval = MIN_PUB_INTERVAL + new Random().nextInt(MIN_PUB_OFFSET);
  private ConnectionStateListenerList stateListeners = new ConnectionStateListenerList();
  private ExecutorService publishExecutor = Executors.newSingleThreadExecutor(NamedThreadFactory.createWith("Ubik.EventChannel.Publish").setDaemon(
      true));

  /**
   * Creates an instance of this class that will use IP multicast and UDP
   * unicast.
   *
   * @param domain
   *          the domain name of this instance.
   * @param mcastHost
   *          the multicast address that this instance will use to broadcast
   *          remote events.
   * @param mcastPort
   *          the multicast port that this instance will use to broadcast remote
   *          events.
   * @throws IOException
   *           if a problem occurs creating this instance.
   * @see DomainName
   * @see UDPBroadcastDispatcher
   * @see UDPUnicastDispatcher
   */
  public EventChannel(String domain, String mcastHost, int mcastPort) throws IOException {
    Props props = new Props().addSystemProperties();
    consumer = new EventConsumer(domain);
    broadcast = new UDPBroadcastDispatcher(consumer, mcastHost, mcastPort, props.getIntProperty(Consts.MCAST_TTL, Defaults.DEFAULT_TTL));
    unicast = new UDPUnicastDispatcher(consumer, props.getIntProperty(Consts.MCAST_HANDLER_COUNT, Defaults.DEFAULT_HANDLER_COUNT));
    init(props);
  }

  /**
   * Returns an {@link EventChannelRef} which will effectively start/close this instance
   * upon its {@link #start()} or {@link EventChannelRef#close()} method being invoked.
   *
   * @return an {@link EventChannelRef} pointing to this instance.
   */
  public EventChannelRef getReference() {
    return new EventChannelRefImpl(this, true);
  }

  /**
   * Returns an {@link EventChannelRef} which will NOT start/close this instance
   * upon its {@link #start()} or {@link EventChannelRef#close()} method being invoked.
   * @return an {@link EventChannelRef} pointing to this instance.
   */
  public EventChannelRef getManagedReference() {
    return new EventChannelRefImpl(this, false);
  }

  /**
   * @param listener adds the given {@link ConnectionStateListener}.
   */
  public void addConnectionStateListener(ConnectionStateListener listener) {
    stateListeners.add(listener);
  }

  /**
   * @param listener removes the given {@link ConnectionStateListener} from this instance.
   */
  public void removeConnectionStateListener(ConnectionStateListener listener) {
    stateListeners.add(listener);
  }

  /**
   * Creates an instance of this class that uses a
   * {@link UDPBroadcastDispatcher} and a {@link UDPUnicastDispatcher}. The
   * broadcast dispatcher will use the default multicast address and port.
   *
   * @param domain
   *          this instance's domain.
   * @throws IOException
   * @see {@link Consts#DEFAULT_MCAST_ADDR}
   * @see Consts#DEFAULT_MCAST_PORT
   * @see UDPBroadcastDispatcher
   * @see UDPUnicastDispatcher
   */
  public EventChannel(String domain) throws IOException {
    this(domain, Consts.DEFAULT_MCAST_ADDR, Consts.DEFAULT_MCAST_PORT);
  }

  /**
   * Creates an instance of this class that will use the given properties to
   * configures its internal unicast and broadcast dispatchers.
   *
   * @param domain
   *          the domain name of this instance.
   * @param config
   *          the {@link Properties} containing unicast and multicast
   *          configuration.
   * @throws IOException
   *           if a problem occurs creating this instance.
   * @see UnicastDispatcher
   * @see BroadcastDispatcher
   */
  public EventChannel(String domain, Props config) throws IOException {
    config.addSystemProperties();
    consumer  = new EventConsumer(domain);
    unicast   = DispatcherFactory.createUnicastDispatcher(consumer, config);
    broadcast = DispatcherFactory.createBroadcastDispatcher(consumer, config);
    init(config);
  }

  /**
   * @param consumer
   *          the {@link EventConsumer} that the event channel will use.
   * @param unicast
   *          the {@link UnicastDispatcher} that the event channel will use.
   * @param broadcast
   *          the {@link BroadcastDispatcher} that the event channel will use.
   */
  public EventChannel(EventConsumer consumer, UnicastDispatcher unicast, BroadcastDispatcher broadcast) {
    this.consumer = consumer;
    this.unicast = unicast;
    this.broadcast = broadcast;
    init(new Props().addSystemProperties());
  }

  /**
   * @return this instance's {@link Role}
   */
  public Role getRole() {
    return controller.getContext().getRole();
  }

  /**
   * Returns this instance's domain name.
   *
   * @return a {@link DomainName}.
   */
  public DomainName getDomainName() {
    return consumer.getDomainName();
  }

  /**
   * @return this instance's {@link MulticastAddress}.
   */
  public MulticastAddress getMulticastAddress() {
    return broadcast.getMulticastAddress();
  }

  /**
   * @return this instance's unicast {@link ServerAddress}.
   */
  public ServerAddress getUnicastAddress() {
    return unicast.getAddress();
  }

  /**
   * Starts this instances. This method should be called after instantiating
   * this instance, prior to start receiving/sending remote events.
   *
   * @throws IOException
   *           if an IO problem occurs starting this instance.
   */
  public synchronized void start() throws IOException {
    if (state == State.CREATED) {

      broadcast.addConnectionStateListener(new ConnectionStateListener() {

        private int resyncAttempts = 0;

        @Override
        public void onReconnected() {
          if (doResync()) stateListeners.notifyReconnected();
        }

        @Override
        public void onDisconnected() {
          stateListeners.notifyDisconnected();
        }

        @Override
        public void onConnected() {
          if (doResync()) stateListeners.notifyConnected();
        }

        private boolean doResync() {
          try {
            resync();
            if (resyncAttempts > 0) {
              Thread.sleep(new Random().nextInt(MIN_STARTUP_DELAY) + MIN_STARTUP_DELAY_OFFSET);
            }
            resyncAttempts++;
            return true;
          } catch (InterruptedException e) {
            return false;
          }
        }
      });

      broadcast.start();
      unicast.start();
      address = unicast.getAddress();
      CHANNELS_BY_DOMAIN.add(this);
      state = State.STARTED;
    }
  }

  /**
   * Forces a resync of this instance with the cluster.
   */
  public synchronized void resync() {
    publishExecutor.execute(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < maxPublishAttempts; i++) {
          try {
            broadcast.dispatch(address, false, PUBLISH_EVT, address);
          } catch (IOException e) {
            log.warning("Error publishing presence to cluster", e);
          }
          try {
            Thread.sleep(publishInterval);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
    });
  }

  /**
   * @param targetedNodes
   *          sends a "force resync" event to the targeted nodes, in order for
   *          them to attempt resyncing with the cluster.
   */
  public synchronized void forceResyncOf(final Set<String> targetedNodes) {
    publishExecutor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          broadcast.dispatch(address, false, FORCE_RESYNC_EVT, targetedNodes);
        } catch (IOException e) {
          log.warning("Error sending force resync event to cluster", e);
        }
      }
    });
  }

  /**
   * @param targetedNodes
   *          sends a "force resync" event to all nodes, in order for them to
   *          attempt resyncing with the cluster.
   */
  public synchronized void forceResync() {
    forceResyncOf(null);
  }

  /**
   * Closes this instance.
   */
  public synchronized void close() {
    if (state == State.STARTED) {
      CHANNELS_BY_DOMAIN.remove(this);
      try {
        this.broadcast.dispatch(unicast.getAddress(), this.getDomainName().toString(), SHUTDOWN_EVT, "SHUTDOWN");
      } catch (IOException e) {
        log.info("Could not send shutdown event", e, new Object[] {});
      }
      publishExecutor.shutdownNow();
      heartbeatTimer.cancel();
      broadcast.close();
      unicast.close();
      state = State.CLOSED;
    }
  }

  /**
   * @return the unmodifiable {@link Set} of {@link EventChannelRef} instances corresponding to currently active
   * {@link EventChannel}s.
   */
  public static synchronized Set<EventChannelRef> getActiveChannels() {
    return Collections2.convertAsSet(CHANNELS_BY_DOMAIN, new Function<EventChannelRef, EventChannel>() {
      @Override
      public EventChannelRef call(EventChannel c) {
        return new EventChannelRefImpl(c, false);
      }
    });
  }

  /**
   * @param condition a {@link Condition}.
   * @return the {@link EventChannelRef} matching the given condition, or <code>null</code>
   * if <code>null</code> if no such match occurs.
   */
  public static synchronized EventChannelRef selectActiveChannel(Condition<EventChannel> condition) {
    for (EventChannel c : CHANNELS_BY_DOMAIN) {
      if (condition.apply(c)) {
        return new EventChannelRefImpl(c, false);
      }
    }
    return null;
  }

  /**
   * @return <code>true</code> if the {@link #start()} method was called on this
   *         instance.
   *
   * @see #start()
   */
  public boolean isStarted() {
    return state == State.STARTED;
  }

  /**
   * @return <code>true</code> if the {@link #close()} method was called on this
   *         instance.
   *
   * @see #close()
   */
  public boolean isClosed() {
    return state == State.CLOSED;
  }

  /**
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(boolean, String,
   *      Object)
   */
  public void dispatch(boolean alldomains, String type, Object data) throws IOException {
    broadcast.dispatch(unicast.getAddress(), alldomains, type, data);
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#dispatch(ServerAddress, String,
   *      Object)
   */
  public void dispatch(ServerAddress addr, String type, Object data) throws IOException {
    unicast.dispatch(addr, type, data);
  }

  /**
   * Dispatches the given data to all nodes in this instance's domain.
   *
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(String, String,
   *      Object)
   */
  public void dispatch(String type, Object data) throws IOException {
    log.debug("Sending event %s - %s", type, data);
    broadcast.dispatch(unicast.getAddress(), consumer.getDomainName().toString(), type, data);
  }

  /**
   * Synchronously sends a remote event to the node corresponding to the given
   * {@link ServerAddress}, and returns the corresponding response.
   *
   * @param addr
   *          the {@link ServerAddress} of the node to which to send the remote
   *          event.
   * @param type
   *          the "logical type" of the remote event.
   * @param data
   *          the data to encapsulate in the remote event.
   *
   * @return the {@link Response} corresponding to this call.
   *
   * @see RemoteEvent
   */
  public Response send(ServerAddress addr, String type, Object data) throws IOException, TimeoutException {
    return unicast.send(addr, type, data);
  }

  /**
   * @see UnicastDispatcher#send(List, String, Object)
   */
  public RespList send(List<ServerAddress> addresses, String type, Object data) throws IOException, TimeoutException, InterruptedException {
    return unicast.send(addresses, type, data);
  }

  /**
   * Synchronously sends a remote event to all this instance's nodes and returns
   * the corresponding responses.
   *
   * @see UnicastDispatcher#send(List, String, Object)
   */
  public RespList send(String type, Object data) throws IOException, InterruptedException {
    return unicast.send(view.getNodeAddresses(), type, data);
  }

  /**
   * Registers a listener of asynchronous remote events of the given type.
   *
   * @param type
   *          the logical type of the remote events to listen for.
   * @param listener
   *          an {@link AsyncEventListener}.
   */
  public synchronized void registerAsyncListener(String type, AsyncEventListener listener) {
    consumer.registerAsyncListener(type, listener);
  }

  /**
   * Registers a listener of synchronous remote events of the given type.
   *
   * @param type
   *          the logical type of the remote events to listen for.
   * @param listener
   *          a {@link SyncEventListener}.
   *
   * @throws ListenerAlreadyRegisteredException
   *           if a listener has already been registered for the given event
   *           type.
   */
  public synchronized void registerSyncListener(String type, SyncEventListener listener) throws ListenerAlreadyRegisteredException {
    consumer.registerSyncListener(type, listener);
  }

  /**
   * Unregisters the given listener from this instance.
   *
   * @param listener
   *          an {@link AsyncEventListener}.
   */
  public synchronized void unregisterAsyncListener(AsyncEventListener listener) {
    consumer.unregisterListener(listener);
  }

  /**
   * Unregisters the given listener from this instance.
   *
   * @param listener
   *          an {@link SyncEventListener}.
   */
  public synchronized void unregisterSyncListener(SyncEventListener listener) {
    consumer.unregisterListener(listener);
  }

  /**
   * Adds the given listener to this instance.
   *
   * @see View#addEventChannelStateListener(EventChannelStateListener)
   */
  public synchronized void addEventChannelStateListener(EventChannelStateListener listener) {
    view.addEventChannelStateListener(listener);
  }

  /**
   * Removes the given listener from this instance.
   *
   * @see View#removeEventChannelStateListener(EventChannelStateListener)
   */
  public synchronized boolean removeEventChannelStateListener(EventChannelStateListener listener) {
    return view.removeEventChannelStateListener(listener);
  }

  /**
   * Adds the given discovery listener to this instance.
   *
   * @param listener
   *          a {@link DiscoveryListener}.
   */
  public void addDiscoveryListener(DiscoveryListener listener) {
    discoListeners.add(listener);
  }

  /**
   * Removes the given discovery listener from this instance.
   *
   * @param listener
   *          a {@link DiscoveryListener}.
   * @return <code>true</code> if the removal occurred.
   */
  public boolean removeDiscoveryListener(DiscoveryListener listener) {
    return discoListeners.remove(listener);
  }

  /**
   * Returns this instance's "view".
   *
   * @return a {@link View}.
   */
  public View getView() {
    return view;
  }

  /**
   * @see EventConsumer#containsAsyncListener(AsyncEventListener)
   */
  public synchronized boolean containsAsyncListener(AsyncEventListener listener) {
    return consumer.containsAsyncListener(listener);
  }

  /**
   * @see EventConsumer#containsSyncListener(SyncEventListener)
   */
  public synchronized boolean containsSyncListener(SyncEventListener listener) {
    return consumer.containsSyncListener(listener);
  }

  /**
   * @see BroadcastDispatcher#getNode()
   */
  public String getNode() {
    return broadcast.getNode();
  }

  EventChannelController getController() {
    return controller;
  }

  void sendControlMessage(SplittableMessage msg) {
    msg.getTargetedNodes().remove(getNode());
    if (!msg.getTargetedNodes().isEmpty()) {
      log.debug("Sending control message %s to nodes: %s", msg.getClass().getSimpleName(), msg.getTargetedNodes());
      List<SplittableMessage> splits = msg.split(controlBatchSize);
      for (SplittableMessage toSend : splits) {
        ServerAddress address = null;
        while (address == null && !toSend.getTargetedNodes().isEmpty()) {
          try {
            String next = toSend.getTargetedNodes().iterator().next();
            log.debug("Sending control message %s with %s targeted nodes to next node %s", toSend.getClass().getSimpleName(), toSend
                .getTargetedNodes().size() - 1, next);
            toSend.getTargetedNodes().remove(next);
            address = view.getAddressFor(next);
            if (address != null) {
              unicast.dispatch(address, CONTROL_EVT, toSend);
            }
            Thread.yield();
            break;
          } catch (Exception e) {
            log.info("Could not send control message to %s", e, address);
            address = null;
          }
        }
      }
    }
  }

  /**
   * This method starts an instance of this class blocks the current thread
   * until the JVM is terminated.
   *
   * @param args
   *          this class' arguments (the only argument taken is the domain
   *          name).
   * @throws Exception
   *           if an error occurs starting this instance.
   */
  public static void main(String[] args) throws Exception {

    String domain = DEFAULT_DOMAIN_NAME;
    if (args.length > 0) {
      domain = args[0];
    }
    System.out.println("Starting event channel on domain: " + domain + ". Type CTRL-C to terminate.");

    final EventChannel channel = new EventChannel(domain, Props.getSystemProperties());
    channel.start();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        channel.close();
      }
    });

    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      channel.close();
    }
  }


  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EventChannel) {
      EventChannel other = (EventChannel) obj;
      return consumer.getNode().equals(other.getNode());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return consumer.getNode().hashCode();
  }

  // ==========================================================================

  private class ChannelCallbackImpl implements ChannelCallback {

    @Override
    public ServerAddress getAddress() {
      return EventChannel.this.getUnicastAddress();
    }

    @Override
    public String getNode() {
      return EventChannel.this.getNode();
    }

    @Override
    public Set<String> getNodes() {
      return view.getNodesAsSet();
    }

    @Override
    public void heartbeat(String node, ServerAddress addr) {
      view.heartbeat(addr, node);
    }

    @Override
    public void resync() {
      EventChannel.this.resync();
    }

    @Override
    public void down(String node) {
      view.removeDeadNode(node);
    }

    @Override
    public void sendNotification(ControlNotification notif) {
      sendControlMessage(notif);
    }

    @Override
    public Set<SynchronousControlResponse> sendSynchronousRequest(Set<String> targetedNodes, SynchronousControlRequest request)
        throws InterruptedException, IOException {

      List<ServerAddress> targetAddresses = new ArrayList<ServerAddress>();
      for (String targetedNode : targetedNodes) {
        ServerAddress addr = view.getAddressFor(targetedNode);
        if (addr != null) {
          targetAddresses.add(addr);
        }
      }

      RespList responses = unicast.send(targetAddresses, CONTROL_EVT, request);

      Set<SynchronousControlResponse> toReturn = new HashSet<SynchronousControlResponse>();
      for (int i = 0; i < responses.count(); i++) {
        Response r = responses.get(i);
        if (!r.isNone() && !r.isError() && r.getData() != null) {
          toReturn.add((SynchronousControlResponse) r.getData());
        }
      }
      return toReturn;
    }

    @Override
    public void sendRequest(ControlRequest req) {
      sendControlMessage(req);
    }

    @Override
    public void sendResponse(String masterNode, ControlResponse res) {
      try {
        ServerAddress addr = view.getAddressFor(masterNode);
        if (addr != null) {
          unicast.dispatch(addr, CONTROL_EVT, res);
        } else {
          log.debug("Address for master node %s not found. Currently have nodes: %s", masterNode, view.getNodes());
        }
      } catch (IOException e) {
        log.error("Could not send control response", e);
      }
    }

    @Override
    public void triggerMasterBroadcast() {
      publishExecutor.execute(new Runnable() {
        @Override
        public void run() {
          for (int i = 0; i < maxPublishAttempts; i++) {
            try {
              broadcast.dispatch(address, false, MASTER_BROADCAST, address);
            } catch (IOException e) {
              log.warning("Error performing master broadcast presence to cluster", e);
            }
            try {
              Thread.sleep(publishInterval);
            } catch (InterruptedException e) {
              break;
            }
          }
        }
      });
    }

    @Override
    public void forceResyncOf(Set<String> targetedNodes) {
      EventChannel.this.forceResyncOf(targetedNodes);
    }

  }

  // ==========================================================================

  private class ChannelEventListener implements AsyncEventListener, SyncEventListener {

    @Override
    public Object onSyncEvent(RemoteEvent evt) {

      log.debug("Received remote event %s from %s", evt.getType(), evt.getNode());

      if (evt.getType().equals(CONTROL_EVT)) {
        try {
          Object data = evt.getData();
          if (data instanceof SynchronousControlRequest) {
            return controller.onSynchronousRequest(evt.getNode(), (SynchronousControlRequest) data);
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process synchronous control request", e);
        }
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAsyncEvent(RemoteEvent evt) {

      log.debug("Received remote event %s from %s", evt.getType(), evt.getNode());

      // ----------------------------------------------------------------------

      if (evt.getType().equals(PUBLISH_EVT)) {
        try {
          ServerAddress addr = (ServerAddress) evt.getData();
          if (addr == null) {
            return;
          }

          view.addHost(addr, evt.getNode());
          unicast.dispatch(addr, DISCOVER_EVT, address);
          notifyDiscoListeners(addr, evt);
          if (controller.getContext().getPurgatory().remove(evt.getNode())) {
            log.debug("Removed node from purgatory: %s", evt.getNode());
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process event " + evt.getType(), e);
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(MASTER_BROADCAST)) {
        try {
          ServerAddress addr = (ServerAddress) evt.getData();
          if (addr == null) {
            return;
          }

          String thisMaster = controller.getContext().getMasterNode();

          // if the broadcast comes from a node that's not in this instance's
          // view, or if the this node's
          // current master (if it exists) does not corresponds to the node that
          // sent the broadcast,
          // we're acking that broadcast.
          if (controller.getContext().getRole() == Role.MASTER || view.addHost(addr, evt.getNode())
              || (thisMaster != null && !thisMaster.equals(evt.getNode()))) {

            // if this node is itself a master, trigger a challenge
            if (controller.getContext().getRole() == Role.MASTER) {
              controller.getContext().triggerChallenge();
            }
            log.debug("Acking master broadcast from %s", evt.getNode());
            unicast.dispatch(addr, MASTER_BROADCAST_ACK, address);
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process event " + evt.getType(), e);
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(MASTER_BROADCAST_ACK)) {

        // if a ack is received, its from a node that doesn't have this node in
        // its view (or as a master).
        // In such a case, we're forcing a resync of that node by adding it to
        // the purgatory.
        if (!view.containsNode(evt.getNode())) {
          log.debug("Received master broadcast ack from %s - adding it to purgatory to force resync", evt.getNode());
          controller.getContext().getPurgatory().add(evt.getNode());
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(FORCE_RESYNC_EVT)) {
        try {
          Set<String> targetedNodes = (Set<String>) evt.getData();
          if (targetedNodes == null || targetedNodes.contains(EventChannel.this.broadcast.getNode())) {
            log.debug("Received force resync event: proceeding to resync");
            resync();
          } else {
            log.debug("Ignoring force resync event: node %s is not in targeted set: %s", broadcast.getNode(), targetedNodes);
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process event " + evt.getType(), e);
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(DISCOVER_EVT)) {
        try {
          ServerAddress addr = (ServerAddress) evt.getData();
          if (addr == null) {
            return;
          }
          if (view.addHost(addr, evt.getNode())) {
            notifyDiscoListeners(addr, evt);
          }
          if (controller.getContext().getPurgatory().remove(evt.getNode())) {
            log.debug("Removed node from purgatory: %s", evt.getNode());
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process event" + evt.getType(), e);
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(SHUTDOWN_EVT)) {
        view.removeDeadNode(evt.getNode());

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(CONTROL_EVT)) {
        try {
          Object data = evt.getData();
          view.addHost(getUnicastAddress(), evt.getNode());
          if (data instanceof ControlRequest) {
            controller.onRequest(evt.getNode(), (ControlRequest) data);
          } else if (data instanceof ControlNotification) {
            controller.onNotification(evt.getNode(), (ControlNotification) data);
          } else if (data instanceof ControlResponse) {
            controller.onResponse(evt.getNode(), (ControlResponse) data);
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process control event", e);
        }
      }
    }
  }

  private void notifyDiscoListeners(ServerAddress addr, RemoteEvent evt) {
    for (DiscoveryListener listener : discoListeners) {
      listener.onDiscovery(addr, evt);
    }
  }

  private void init(Props props) {
    listener = new ChannelEventListener();
    consumer.registerAsyncListener(PUBLISH_EVT, listener);
    consumer.registerAsyncListener(FORCE_RESYNC_EVT, listener);
    consumer.registerAsyncListener(DISCOVER_EVT, listener);
    consumer.registerAsyncListener(SHUTDOWN_EVT, listener);
    consumer.registerAsyncListener(MASTER_BROADCAST, listener);
    consumer.registerAsyncListener(MASTER_BROADCAST_ACK, listener);
    consumer.registerAsyncListener(CONTROL_EVT, listener);
    try {
      consumer.registerSyncListener(CONTROL_EVT, listener);
    } catch (ListenerAlreadyRegisteredException e) {
      throw new IllegalStateException("Could not register sync event listener", e);
    }
    long heartbeatTimeout = props.getLongProperty(Consts.MCAST_HEARTBEAT_TIMEOUT, Defaults.DEFAULT_HEARTBEAT_TIMEOUT);
    long heartbeatInterval = props.getLongProperty(Consts.MCAST_HEARTBEAT_INTERVAL, Defaults.DEFAULT_HEARTBEAT_INTERVAL);
    long controlResponseTimeout = props.getLongProperty(Consts.MCAST_CONTROL_RESPONSE_TIMEOUT, Defaults.DEFAULT_CONTROL_RESPONSE_TIMEOUT);
    int forceResyncAttempts = props.getIntProperty(Consts.MCAST_HEARTBEAT_FORCE_RESYNC_ATTEMPTS, Defaults.DEFAULT_FORCE_RESYNC_ATTEMPTS);
    int forceResyncBatchSize = props.getIntProperty(Consts.MCAST_HEARTBEAT_FORCE_RESYNC_BATCH_SIZE, Defaults.DEFAULT_FORCE_RESYNC_BATCH_SIZE);
    long masterBroadcastInterval = props.getLongProperty(Consts.MCAST_MASTER_BROADCAST_INTERVAL, Defaults.DEFAULT_MASTER_BROADCAST_INTERVAL);
    boolean masterBroadcastEnabled = props.getBooleanProperty(Consts.MCAST_MASTER_BROADCAST_ENABLED, true);

    this.controlBatchSize = props.getIntProperty(Consts.MCAST_CONTROL_SPLIT_SIZE, Defaults.DEFAULT_CONTROL_SPLIT_SIZE);

    log.debug("Heartbeat timeout set to %s", heartbeatTimeout);
    log.debug("Heartbeat interval set to %s", heartbeatInterval);
    log.debug("Control response timeout set to %s", controlResponseTimeout);
    log.debug("Forced resync attempts set to %s", forceResyncAttempts);
    log.debug("Forced resync batch size set to %s", forceResyncBatchSize);
    log.debug("Master broadcast enabled: %s", masterBroadcastEnabled);
    log.debug("Master broadcast interval set to %s", masterBroadcastInterval);

    ControllerConfiguration config = new ControllerConfiguration();
    config.setHeartbeatInterval(heartbeatInterval);
    config.setHeartbeatTimeout(heartbeatTimeout);
    config.setResponseTimeout(controlResponseTimeout);
    config.setForceResyncAttempts(forceResyncAttempts);
    config.setForceResyncBatchSize(forceResyncBatchSize);
    config.setMasterBroadcastEnabled(masterBroadcastEnabled);
    config.setMasterBroadcastInterval(masterBroadcastInterval);
    controller = new EventChannelController(createClock(), config, new ChannelCallbackImpl());
    startTimer(heartbeatInterval);
  }

  protected void startTimer(long heartbeatInterval) {
    Random random = new Random();
    long firstTaskStartupDelay = MIN_STARTUP_DELAY + random.nextInt(MIN_STARTUP_DELAY_OFFSET);
    heartbeatTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (state == State.STARTED) {
          controller.checkStatus();
        }
      }
    }, firstTaskStartupDelay, heartbeatInterval);
  }

  protected Clock createClock() {
    return Clock.SystemClock.getInstance();
  }

}
