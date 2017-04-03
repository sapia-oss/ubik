package org.sapia.ubik.mcast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlNotification;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.EventChannelController;
import org.sapia.ubik.mcast.control.EventChannelFacade;
import org.sapia.ubik.mcast.control.GossipMessage;
import org.sapia.ubik.mcast.control.GossipNotification;
import org.sapia.ubik.mcast.control.SplitteableMessage;
import org.sapia.ubik.mcast.control.SynchronousControlRequest;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.mcast.control.gossip.GossipSyncNotification;
import org.sapia.ubik.mcast.udp.UDPBroadcastDispatcher;
import org.sapia.ubik.mcast.udp.UDPUnicastDispatcher;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.SoftReferenceList;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.TimeRange;
import org.sapia.ubik.util.TimeValue;

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
  static final String DISCOVER_EVT         = "ubik/mcast/discover";

  /**
   * Sent at startup when a node first appears.
   */
  static final String PUBLISH_EVT          = "ubik/mcast/publish";

  /**
   * Sent when a node (or set of nodes) are detected as down, and a last attempt
   * is being made to rediscover them - nodes that receive this event should try
   * to resync themselves.
   */
  static final String FORCE_RESYNC_EVT     = "ubik/mcast/forceResync";

  /**
   * Sent by a node to notify other nodes that it is shutting down.
   */
  static final String SHUTDOWN_EVT         = "ubik/mcast/shutdown";

  /**
   * Sent by a node to notify other nodes that it is leaving.
   */
  static final String LEAVE_EVT            = "ubik/mcast/leave";
  
  /**
   * Sent by a master node periodically.
   */
  static final String MASTER_BROADCAST     = "ubik/mcast/master/broadcast";

  /**
   * Sent back as ack to master broadcast.
   */
  static final String MASTER_BROADCAST_ACK = "ubik/mcast/master/broadcast/ack";

  /**
   * Corresponds to all types of control events.
   */
  static final String CONTROL_EVT          = "ubik/mcast/control";
  
  private static final int       DEFAULT_MAX_PUB_ATTEMPTS     = 3;
  private static final TimeValue DEFAULT_READ_TIMEOUT         = TimeValue.createMillis(10000);
  private static final int       DEFAULT_PUBLISH_THREAD_COUNT = 5;
  private static final int       DEFAULT_PUBLISH_QUEUE_SIZE   = 1000;

  private static Set<EventChannel> CHANNELS_BY_DOMAIN = Collections.synchronizedSet(new HashSet<EventChannel>());

  private Category log = Log.createCategory(getClass());
  private static boolean              eventChannelReuse = Conf.getSystemProperties()
      .getBooleanProperty(Consts.MCAST_REUSE_EXISTINC_CHANNELS, true);
  private Timer                       heartbeatTimer = new Timer("Ubik.EventChannel.Timer", true);
  private BroadcastDispatcher         broadcast;
  private UnicastDispatcher           unicast;
  private EventConsumer               consumer;
  private ChannelEventListener        listener;
  private View                        view;
  private EventChannelController      controller;
  private int                         controlBatchSize;
  private ServerAddress               address;
  private volatile State              state                  = State.CREATED;
  private int                         maxPublishAttempts     = DEFAULT_MAX_PUB_ATTEMPTS;
  private TimeRange                   startDelayRange;
  private TimeRange                   publishIntervalRange;
  private TimeValue                   defaultReadTimeout     = DEFAULT_READ_TIMEOUT;
  private ConnectionStateListenerList stateListeners         = new ConnectionStateListenerList();
  private int                         publishThreadCount;
  private int                         publishQueueSize;
  private ExecutorService             publishExecutor;
  
  private SoftReferenceList<DiscoveryListener> discoListeners = new SoftReferenceList<DiscoveryListener>();

  /**
   * Creates an instance of this class which by default uses a
   * {@link UDPBroadcastDispatcher} and a {@link UDPUnicastDispatcher}. The
   * broadcast dispatcher will use the default multicast address and port.
   * <p>
   * The {@link BroadcastDispatcher}  and {@link UnicastDispatcher} to use
   * may be specified by system properties corresponding to {@link Consts#BROADCAST_PROVIDER}
   * and {@link Consts#UNICAST_PROVIDER}, respectively.
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
    this(domain, new Conf());
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
   * @see Consts#BROADCAST_PROVIDER
   * @see Consts#UNICAST_PROVIDER
   */
  public EventChannel(String domain, Conf config) throws IOException {
    config.addSystemProperties();
    consumer  = new EventConsumer(domain, config);
    unicast   = DispatcherFactory.createUnicastDispatcher(consumer, config);
    broadcast = DispatcherFactory.createBroadcastDispatcher(consumer, config);
    view      = new View(consumer.getNode());
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
    view      = new View(consumer.getNode());
    init(new Conf().addSystemProperties());
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
   * Starts this instances. This method should be called after instantiating
   * this instance, prior to start receiving/sending remote events.
   *
   * @throws IOException
   *           if an IO problem occurs starting this instance.
   */
  public synchronized void start() throws IOException {
    if (state == State.CREATED) {

      publishExecutor = new ThreadPoolExecutor(publishThreadCount, publishThreadCount,
          60, TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(publishQueueSize),
          NamedThreadFactory.createWith("Ubik.EventChannel.Publish").setDaemon(true));
      
      broadcast.addConnectionStateListener(new ConnectionStateListener() {

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
          resync();
          return true;
        }
      });

      broadcast.start();
      unicast.start();
      address = unicast.getAddress();
      CHANNELS_BY_DOMAIN.add(this);
      state   = State.STARTED;
    }
  }
  
  /**
   * Forces a domain change: this instance will leave the current domain, 
   * and publish itself in the given new domain.
   * 
   * @param newDomain the new domain to change to.
   */
  public synchronized void changeDomain(final String newDomain) {
    synchronized (CHANNELS_BY_DOMAIN) {
      CHANNELS_BY_DOMAIN.remove(this);
      try {
        broadcast.dispatch(address, false, LEAVE_EVT, newDomain);
      } catch (IOException e) {
        log.warning("Error broadcasting domain leave event");
      }
      view.removedFromDomain();
      consumer.changeDomain(newDomain);
      CHANNELS_BY_DOMAIN.add(this); 
      resync();
    }
  }
  
  /**
   * Forces a resync of this instance with the cluster.
   */
  public synchronized void resync() {
    publishExecutor.execute(
        doCreateTaskForPublishBraodcastEvent(maxPublishAttempts));
  }
  
  private TimerTask doCreateTaskForPublishBraodcastEvent(final int attemptCount) {
    return new TimerTask() {
      @Override
      public void run() {
        log.info("Publishing presence of this node (%s) to cluster (attempt count %s)", address, attemptCount);
        try {
          broadcast.dispatch(address, false, PUBLISH_EVT, address);
        } catch (IOException e) {
          log.warning("Error publishing presence to cluster", e);
        }
        
        int attemptLeft = attemptCount - 1;
        if (attemptLeft > 0) {
          heartbeatTimer.schedule(
              doCreateTaskForPublishBraodcastEvent(attemptLeft),
              publishIntervalRange.getRandomTime().getValueInMillis());
        }
      }
    };
  }

  /**
   * @param targetedNodes
   *          sends a "force resync" event to the targeted nodes, in order for
   *          them to attempt resyncing with the cluster.
   */
  public synchronized void forceResyncOf(final Set<String> targetedNodes) {
    publishExecutor.execute(() -> {
      try {
        broadcast.dispatch(address, false, FORCE_RESYNC_EVT, targetedNodes);
      } catch (IOException e) {
        log.warning("Error sending force resync event to cluster", e);
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
      consumer.stop();
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
    return Collects.convertAsSet(CHANNELS_BY_DOMAIN, new Func<EventChannelRef, EventChannel>() {
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
    if (eventChannelReuse) {
      for (EventChannel c : CHANNELS_BY_DOMAIN) {
        if (condition.apply(c)) {
          return new EventChannelRefImpl(c, false);
        }
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
  public Future<Void> dispatch(boolean alldomains, String type, Object data) {
    log.debug("Broadcasting async event %s to all domains - %s", type, data);
     return publishExecutor.<Void>submit(() -> {
        try {
          broadcast.dispatch(unicast.getAddress(), alldomains, type, data);
        } catch (Exception e) {
          log.warning("Could not broadcast async event %s to all domains (%s)", e, type, data);
          throw new ExecutionException("System error dispatching event", e);
        }
        return null;
      });
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#dispatch(ServerAddress, String,
   *      Object)
   */
  public Future<Void> dispatch(ServerAddress addr, String type, Object data) {
    log.debug("Sending async event %s - %s", type, data);
    return publishExecutor.<Void>submit(() -> {
      try {
        unicast.dispatch(addr, type, data);
      } catch (Exception e) {
        log.warning("Could not send async event %s to %s (%s)", e, type, addr, data);
        throw new ExecutionException("System error dispatching event", e);
      }
      return null;
    });
  }

  /**
   * Dispatches the given data to all nodes in this instance's domain.
   *
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(String, String,
   *      Object)
   */
  public Future<Void> dispatch(String type, Object data) {
    log.debug("Broadcasting async event %s - %s", type, data);
    return publishExecutor.<Void>submit(() -> {
      try {
        broadcast.dispatch(unicast.getAddress(), consumer.getDomainName().toString(), type, data);
      } catch (Exception e) {
        log.warning("Could not broadcast async event %s (%s)", e, type, data);
        throw new ExecutionException("System error dispatching event", e);
      }
      return null;
    });
  }

  /**
   * Sends the given data to the specified address, blocking for a response for the
   * default timeout duration that has been configured on this instance.
   * 
   * @see UnicastDispatcher#send(ServerAddress, String, Object, TimeValue)
   */
  public Response send(ServerAddress addr, String type, Object data) 
      throws IOException, TimeoutException {
    return unicast.send(addr, type, data, defaultReadTimeout);
  }
  
  /**
   * Sends the given data to the specified address, blocking for a response for the
   * given timeout duration.
   * 
   * @see UnicastDispatcher#send(ServerAddress, String, Object, TimeValue)
   */
  public Response send(ServerAddress addr, String type, Object data, TimeValue timeout) 
      throws IOException, TimeoutException {
    return unicast.send(addr, type, data, timeout);
  }
  
  /**
   * Sends the given data to the specified addresses, blocking for a response for the
   * default timeout duration that has been configured on this instance.
   * 
   * @see UnicastDispatcher#send(List, String, Object, TimeValue)
   */
  public RespList send(List<ServerAddress> addresses, String type, Object data) 
      throws IOException, TimeoutException, InterruptedException {
    return unicast.send(addresses, type, data, defaultReadTimeout);
  }
  
  /**
   * Sends the given data to the specified addresses, blocking for a response for the
   * given timeout duration.
   * 
   * @see UnicastDispatcher#send(List, String, Object, TimeValue)
   */
  public RespList send(List<ServerAddress> addresses, String type, Object data, TimeValue timeout) 
      throws IOException, TimeoutException, InterruptedException {
    return unicast.send(addresses, type, data, timeout);
  }
  
  /**
   * Sends the given data elements to the specified addresses (respectively), blocking for a response for the
   * default timeout duration that has been configured on this instance.
   * 
   * @see UnicastDispatcher#send(ServerAddress[], String, Object[], TimeValue)
   */
  public RespList send(ServerAddress[] addresses, String type, Object[] data) 
      throws IOException, TimeoutException, InterruptedException {
    return unicast.send(addresses, type, data, defaultReadTimeout);
  }
  
  /**
   * Sends the given data elements to the specified addresses (respectively), blocking for a response for the
   * given timeout duration.
   * 
   * @see UnicastDispatcher#send(ServerAddress[], String, Object[], TimeValue)
   */
  public RespList send(ServerAddress[] addresses, String type, Object[] data, TimeValue timeout) 
      throws IOException, TimeoutException, InterruptedException {
    return unicast.send(addresses, type, data, timeout);
  }

  /**
   * Synchronously sends a remote event to all this instance's nodes and returns
   * the corresponding responses. Blocks for the default timeout duration that has been
   * configured on this instance.
   *
   * @see UnicastDispatcher#send(ServerAddress, String, Object, TimeValue)
   */
  public RespList send(String type, Object data) throws IOException, InterruptedException {
    return unicast.send(view.getNodeAddresses(), type, data, defaultReadTimeout);
  }
  
  /**
   * Synchronously sends a remote event to all this instance's nodes and returns
   * the corresponding responses. Blocks for the given timeout duration.
   *
   * @see UnicastDispatcher#send(ServerAddress, String, Object, TimeValue)
   */
  public RespList send(String type, Object data, TimeValue timeout) 
      throws IOException, InterruptedException {
    return unicast.send(view.getNodeAddresses(), type, data, timeout);
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

  /**
   * Used when testing to disable active instance reuse.
   */
  public static void disableReuse() {
    eventChannelReuse = false;
  }

  /**
   * Used when testing to re-enable instance reuse.
   */
  public static void enableReuse() {
    eventChannelReuse = true;
  }

  EventChannelController getController() {
    return controller;
  }

  void sendControlMessage(SplitteableMessage msg) {
    publishExecutor.execute(() -> {
        msg.getTargetedNodes().remove(getNode());
        if (!msg.getTargetedNodes().isEmpty()) {
          log.debug("Sending control message %s to nodes: %s", msg.getClass().getSimpleName(), msg.getTargetedNodes());
          List<SplitteableMessage> splits = msg.split(controlBatchSize);
          for (SplitteableMessage toSend : splits) {
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
    });
  }
  
  void sendGossipMessage(final GossipMessage msg) {
    publishExecutor.execute(() -> {
      List<NodeInfo> candidates = controller.getContext().getEventChannel().getView(GossipSyncNotification.NON_SUSPECT_NODES_FILTER);        
      Collections.shuffle(candidates);
      
      int counter = 0;
      log.debug("Sending gossip notification: %s (got %s candidates)", msg, candidates.size());
      for (NodeInfo c : candidates) {
        try {
          log.debug("Sending to : %s", c);
          if (unicast.dispatch(c.getAddr(), CONTROL_EVT, msg)) {
            counter++;
            if(counter == controller.getContext().getConfig().getGossipNodeCount()) {
              break;
            }
          } else {
            c.suspect();
          }
         
        } catch (Exception e) {
          log.info("Could not send control message to %s", e, c.getAddr());
          c.suspect();
        }
      }
    });      
  }
  
  /**
   * Closes the statically cached event channels, and clears the cache.
   */
  public static void closeCachedChannels() {
    List<EventChannel> channels = new ArrayList<EventChannel>(CHANNELS_BY_DOMAIN);
    for (EventChannel ec : channels) {
      ec.close();
      CHANNELS_BY_DOMAIN.remove(ec);
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

    final EventChannel channel = new EventChannel(domain, Conf.getSystemProperties());
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

  private class ChannelCallbackImpl implements EventChannelFacade {

    @Override
    public ServerAddress getAddress() {
      return EventChannel.this.getUnicastAddress();
    }

    @Override
    public String getNode() {
      return EventChannel.this.getNode();
    }
    
    @Override
    public NodeInfo getNodeInfoFor(String node) {
      return view.getNodeInfo(node);
    }

    @Override
    public Set<String> getNodes() {
      return view.getNodesAsSet();
    }
    
    @Override
    public int getNodeCount() {
      return view.getNodeCount();
    }
    
    @Override
    public List<NodeInfo> getView() {
      return view.getNodeInfos();
    }
    
    @Override
    public List<NodeInfo> getView(Condition<NodeInfo> filter) {
      return view.getNodeInfos(filter);
    }
    
    @Override
    public boolean containsNode(String node) {
      return view.containsNode(node);
    }

    @Override
    public void heartbeat(String node, ServerAddress addr) {
      view.heartbeat(addr, node, controller.getContext().getClock());
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
    public void sendGossipNotification(GossipNotification notif) {
      sendGossipMessage(notif);
    }

    @Override
    public Future<Void> sendBroadcastEvent(final ControlEvent event) {
      return publishExecutor.<Void>submit(() -> {
          try {
            broadcast.dispatch(getUnicastAddress(), false, CONTROL_EVT, event);
          } catch (Exception e) {
            log.warning("Could not broadcast async control event %s (%s)", e, CONTROL_EVT, event);
            throw new ExecutionException("System error dispatching control event", e);
          }
          return null;
        });
    }
    
    @Override
    public Future<Void> sendUnicastEvent(final ServerAddress destination, final ControlEvent event) {
      return publishExecutor.<Void>submit(() -> {
          try {
            unicast.dispatch(destination, CONTROL_EVT, event);
          } catch (IOException e) {
            log.error("Could not dispatch control event", e);
          } catch (Exception e) {
            log.warning("Could not send async event %s to %s (%s)", e, CONTROL_EVT, destination, event);
            throw new ExecutionException("System error dispatching control event", e);
          }
          return null;
        });
    }
    
    @Override
    public boolean addNewNode(String node, ServerAddress addr) {
      return view.addHost(addr, node);
    }

    @Override
    public Set<SynchronousControlResponse> sendSynchronousRequest(Set<String> targetedNodes, SynchronousControlRequest request, TimeValue timeout)
        throws InterruptedException, IOException {

      log.debug("Sending sync request to %s target nodes", targetedNodes.size());
      
      List<ServerAddress> targetAddresses = new ArrayList<ServerAddress>();
      for (String targetedNode : targetedNodes) {
        ServerAddress addr = view.getAddressFor(targetedNode);
        if (addr != null) {
          targetAddresses.add(addr);
        } else {
          log.info("Could not resolve unicast address for node: %s", targetedNode);
        }
      }

      RespList responses = unicast.send(targetAddresses, CONTROL_EVT, request, timeout);

      Set<SynchronousControlResponse> toReturn = new HashSet<SynchronousControlResponse>();
      for (int i = 0; i < responses.count(); i++) {
        Response r = responses.get(i);
        if (!r.isNone() && !r.isThrowable()) {
          SynchronousControlResponse sr = (SynchronousControlResponse) r.getData();
          toReturn.add(sr);
        } else {
          log.debug("Discarding response: %s", r.getStatus());
        }
      }
      
      log.debug("Returning %s responses", toReturn.size());
      return toReturn;
    }
    
    @Override
    public Set<SynchronousControlResponse> sendSynchronousRequests(
        String[] targetedNodes, SynchronousControlRequest[] requests, TimeValue timeout) throws InterruptedException,
        IOException {
      
      log.debug("Sending sync requests to %s target nodes", targetedNodes.length);
      
      List<ServerAddress> targetAddresses = new ArrayList<ServerAddress>();
      for (String targetedNode : targetedNodes) {
        ServerAddress addr = view.getAddressFor(targetedNode);
        if (addr != null) {
          targetAddresses.add(addr);
        } else {
          log.info("Could not resolve unicast address for node: %s", targetedNode);
        }
      }

      RespList responses = unicast.send(targetAddresses.toArray(
          new ServerAddress[targetAddresses.size()]), CONTROL_EVT, requests, timeout
      );

      Set<SynchronousControlResponse> toReturn = new HashSet<SynchronousControlResponse>();
      for (int i = 0; i < responses.count(); i++) {
        Response r = responses.get(i);
        if (!r.isNone() && !r.isThrowable()) {
          SynchronousControlResponse sr = (SynchronousControlResponse) r.getData();
          if (sr != null) {
            toReturn.add(sr);
          }
        } else {
          log.debug("Discarding response: %s", r.getStatus());
        }
      }
      
      log.debug("Returning %s responses", toReturn.size());
      return toReturn;
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
            return controller.onSynchronousRequest(evt.getNode(), evt.getUnicastAddress(), (SynchronousControlRequest) data);
          } else if (data instanceof ControlEvent) {
            controller.onEvent(evt.getNode(), evt.getUnicastAddress(), (ControlEvent) data);
          }
          
        } catch (IOException e) {
          log.error("Error caught while trying to process synchronous control request", e);
        }
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAsyncEvent(final RemoteEvent evt) {

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
        } catch (IOException e) {
          log.error("Error caught while trying to process event " + evt.getType(), e);
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(FORCE_RESYNC_EVT)) {
        try {
          Set<String> targetedNodes = (Set<String>) evt.getData();
          if (targetedNodes == null || targetedNodes.contains(EventChannel.this.broadcast.getNode())) {
            log.info("Received force resync event: proceeding to resync");
            resync();
          } else {
            log.info("Ignoring force resync event: node %s is not in targeted set: %s", broadcast.getNode(), targetedNodes);
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
        } catch (IOException e) {
          log.error("Error caught while trying to process event" + evt.getType(), e);
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(SHUTDOWN_EVT)) {
        view.removeDeadNode(evt.getNode());

        // ----------------------------------------------------------------------
        
      } else if (evt.getType().equals(LEAVE_EVT)) {
        view.removeLeavingNode(evt.getNode());

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(CONTROL_EVT)) {
        try {
          Object data = evt.getData();
          if (data instanceof ControlNotification) {
            controller.onNotification(evt.getNode(), evt.getUnicastAddress(), (ControlNotification) data);
          } else if (data instanceof GossipNotification) {
            controller.onGossipNotification(evt.getNode(), evt.getUnicastAddress(), (GossipNotification) data);
          } else if (data instanceof ControlEvent) {
            controller.onEvent(evt.getNode(), evt.getUnicastAddress(), (ControlEvent) data);
          } else {
            log.warning("Undnown event type: %s", data.getClass().getName());
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

  private void init(Conf props) {
    listener = new ChannelEventListener();
    consumer.registerAsyncListener(PUBLISH_EVT, listener);
    consumer.registerAsyncListener(FORCE_RESYNC_EVT, listener);
    consumer.registerAsyncListener(DISCOVER_EVT, listener);
    consumer.registerAsyncListener(SHUTDOWN_EVT, listener);
    consumer.registerAsyncListener(LEAVE_EVT, listener);
    consumer.registerAsyncListener(MASTER_BROADCAST, listener);
    consumer.registerAsyncListener(MASTER_BROADCAST_ACK, listener);
    consumer.registerAsyncListener(CONTROL_EVT, listener);
    try {
      consumer.registerSyncListener(CONTROL_EVT, listener);
    } catch (ListenerAlreadyRegisteredException e) {
      throw new IllegalStateException("Could not register sync event listener", e);
    }

    this.publishThreadCount = props.getIntProperty(
        Consts.MCAST_CHANNEL_PUBLISH_THREAD_COUNT, DEFAULT_PUBLISH_THREAD_COUNT);
    this.publishQueueSize = props.getIntProperty(
        Consts.MCAST_CHANNEL_PUBLISH_QUEUE_SIZE, DEFAULT_PUBLISH_QUEUE_SIZE);
    
    TimeValue controlThreadInterval      = props.getTimeProperty(
        Consts.MCAST_CONTROL_THREAD_INTERVAL, Defaults.DEFAULT_CONTROL_THREAD_INTERVAL
    );
    TimeValue heartbeatTimeout           = props.getTimeProperty(
        Consts.MCAST_HEARTBEAT_TIMEOUT, Defaults.DEFAULT_HEARTBEAT_TIMEOUT
    );

    int       healthCheckDelegateCount   = props.getIntProperty(
        Consts.MCAST_HEALTHCHECK_DELEGATE_COUNT, Defaults.DEFAULT_HEALTCHCHECK_DELEGATE_COUNT
    );
    TimeValue healthCheckDelegateTimeOut = props.getTimeProperty(
        Consts.MCAST_HEALTHCHECK_DELEGATE_TIMEOUT, Defaults.DEFAULT_HEALTCHCHECK_DELEGATE_TIMEOUT
    );
    boolean   gossipEnabled              = props.getBooleanProperty(Consts.MCAST_GOSSIP_ENABLED, true);
    TimeValue gossipInterval             = props.getTimeProperty(Consts.MCAST_GOSSIP_INTERVAL, Defaults.DEFAULT_GOSSIP_INTERVAL);
    
    this.startDelayRange                 = props.getTimeRangeProperty(
        Consts.MCAST_CHANNEL_START_DELAY, Defaults.DEFAULT_CHANNEL_START_DELAY
    );
    this.publishIntervalRange            = props.getTimeRangeProperty(
        Consts.MCAST_CHANNEL_PUBLISH_INTERVAL, Defaults.DEFAULT_CHANNEL_PUBLISH_INTERVAL
    );
    this.controlBatchSize                 = props.getIntProperty(
        Consts.MCAST_CONTROL_SPLIT_SIZE, Defaults.DEFAULT_CONTROL_SPLIT_SIZE
    );

    log.debug("Control thread interval %s", controlThreadInterval);
    log.debug("Heartbeat timeout set to %s", heartbeatTimeout);
    log.debug("Health check delegate node count set to %s", healthCheckDelegateCount);
    log.debug("Health check delegate timeout set to %s", healthCheckDelegateTimeOut);
    log.debug("Gossip enabled (SHOULD BE DISABLED FOR TESTING ONLY): %s", gossipEnabled);
    log.debug("Gossip interval %s", gossipInterval);

    ControllerConfiguration config = new ControllerConfiguration();
    config.setGossipEnabled(gossipEnabled);
    config.setGossipInterval(gossipInterval);
    config.setHealthCheckDelegateCount(healthCheckDelegateCount);
    config.setHealthCheckDelegateTimeout(healthCheckDelegateTimeOut);
    config.setHeartbeatTimeout(heartbeatTimeout);
    
    config.setAutoBroadcastInterval(props.getTimeProperty(Consts.MCAST_AUTO_BROADCAST_INTERVAL, Defaults.DEFAULT_AUTO_BROADCAST_INTERVAL));
    config.setAutoBroadcastEnabled(props.getBooleanProperty(Consts.MCAST_AUTO_BROADCAST_ENABLED, true));
    config.setAutoBroadcastThreshold(props.getIntProperty(Consts.MCAST_AUTO_BROADCAST_THRESHOLD, Defaults.DEFAULT_AUTO_BROADCAST_THRESHOLD));
    
    controller = new EventChannelController(createClock(), config, new ChannelCallbackImpl());

    startTimer(controlThreadInterval);
  }

  protected void startTimer(TimeValue controlThreadInterval) {
    heartbeatTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (state == State.STARTED) {
          controller.checkStatus();
        }
      }
    }, startDelayRange.getRandomTime().getValueInMillis(), controlThreadInterval.getValueInMillis());
  }

  protected SysClock createClock() {
    return SysClock.RealtimeClock.getInstance();
  }
  
  // =========================================================================
  
  public static final class BroadcastData {
    
    private int currentNumberOfNodes;
    
    /**
     * Meant for externalization only.
     */
    public BroadcastData() {
    }
    
    BroadcastData(int currentNumberOfNodes) {
      this.currentNumberOfNodes = currentNumberOfNodes;
    }
    
    /**
     * @return the current number of nodes.
     */
    int getCurrentNumberOfNodes() {
      return currentNumberOfNodes;
    }
    
  }

}
