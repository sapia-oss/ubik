package org.sapia.ubik.mcast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.mina.util.ConcurrentHashSet;
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
import org.sapia.ubik.mcast.udp.UDPBroadcastDispatcher;
import org.sapia.ubik.mcast.udp.UDPUnicastDispatcher;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.SoftReferenceList;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.TimeRange;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.UbikMetrics;
import org.sapia.ubik.util.throttle.NullThrottle;

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
 * @author yduchesne
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
  
  private static final int               DEFAULT_MAX_PUB_ATTEMPTS = 3;
  private static final TimeValue         DEFAULT_READ_TIMEOUT     = TimeValue.createMillis(10000);
  private static final Set<EventChannel> CHANNELS_BY_DOMAIN       = new ConcurrentHashSet<EventChannel>();

  private Category log = Log.createCategory(getClass());
  private static boolean              eventChannelReuse = Conf.getSystemProperties()
      .getBooleanProperty(Consts.MCAST_REUSE_EXISTINC_CHANNELS, true);
  private BroadcastDispatcher         broadcast;
  private UnicastDispatcher           unicast;
  private EventConsumer               consumer;
  private ChannelEventListener        listener;
  private View                        view;
  private EventChannelController      controller;
  private int                         controlBatchSize;
  private ServerAddress               address;
  private volatile State              state                  = State.CREATED;
  private UbikMetrics                 metrics                = UbikMetrics.globalMetrics();  
  private int                         maxPublishAttempts     = DEFAULT_MAX_PUB_ATTEMPTS;
  private TimeRange                   startDelayRange;
  private TimeRange                   publishIntervalRange;
  private TimeValue                   defaultReadTimeout     = DEFAULT_READ_TIMEOUT;
  private ConnectionStateListenerList stateListeners         = new ConnectionStateListenerList();
  private Timer                       scheduler;
  private ExecutorService             asyncExecutor;
  private Timer                       publisher;
  
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
    consumer  = new EventConsumer(domain);
    
    DispatcherContext context = new DispatcherContext(consumer, new DispatcherContext.SelectorExecutorFactory() {
      @Override
      public ExecutorService getExecutor(String name) {
        return Threads.createIoInboundPool(name);
      }
    }).withConf(config);
    
    unicast   = DispatcherFactory.createUnicastDispatcher(context);
    broadcast = DispatcherFactory.createBroadcastDispatcher(context);
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

      asyncExecutor = Threads.createIoOutboundPool();
      
      final List<Runnable> pending = new ArrayList<>();
      
      broadcast.addConnectionStateListener(new ConnectionStateListener() {

        @Override
        public void onReconnected() {
          if (state == State.STARTED) {
            if (doResync()) stateListeners.notifyReconnected();
          } else {
            pending.add(new Runnable() {
              public void run() {
                if (doResync()) stateListeners.notifyReconnected();
              }
            });
          }
        }

        @Override
        public void onDisconnected() {
          stateListeners.notifyDisconnected();
        }

        @Override
        public void onConnected() {
          if (state == State.STARTED) {
            if (doResync()) stateListeners.notifyConnected();
          } else {
            pending.add(new Runnable() {
              public void run() {
                if (doResync()) stateListeners.notifyConnected();
              }
            });
          }
        }

        private boolean doResync() {
          resync();
          return true;
        }
      });

      broadcast.start();
      unicast.start();
      address = unicast.getAddress();
      synchronized (CHANNELS_BY_DOMAIN) {
        CHANNELS_BY_DOMAIN.add(this);
      }
      state   = State.STARTED;
      
      for (Runnable p : pending) {
        p.run();
      }
      pending.clear();
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
      view.clearView();
      consumer.changeDomain(newDomain);
      CHANNELS_BY_DOMAIN.add(this); 
      resync();
    }
  }
  
  /**
   * Forces a resync of this instance with the cluster.
   */
  public synchronized void resync() {
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
    log.info("Performing resync: clearing view and publishing presence to cluster");
    view.clearView();
    metrics.incrementCounter("eventChannel.resync");
    publisher.schedule(new TimerTask() {
      private Runnable task = doCreateTaskForPublishBroadcastEvent(maxPublishAttempts);
      @Override
      public void run() {
        try {
          task.run();
        } catch (Exception e) {
          log.warning("System error running broadcast timer task", e);
        }
      }
    }, publishIntervalRange.getRandomTime().getValueInMillis());
  }

  /**
   * Closes this instance.
   */
  public synchronized void close() {
    if (state == State.STARTED) {
      synchronized (CHANNELS_BY_DOMAIN) {
        CHANNELS_BY_DOMAIN.remove(this);
      }
      try {
        this.broadcast.dispatch(unicast.getAddress(), this.getDomainName().toString(), SHUTDOWN_EVT, "SHUTDOWN");
      } catch (IOException e) {
        log.info("Could not send shutdown event", e, new Object[] {});
      }
      consumer.stop();
      scheduler.cancel();
      asyncExecutor.shutdown();
      publisher.cancel();
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
    synchronized (CHANNELS_BY_DOMAIN) {
      return Collects.convertAsSet(CHANNELS_BY_DOMAIN, new Func<EventChannelRef, EventChannel>() {
        @Override
        public EventChannelRef call(EventChannel c) {
          return new EventChannelRefImpl(c, false);
        }
      });
    }
  }

  /**
   * @param condition a {@link Condition}.
   * @return the {@link EventChannelRef} matching the given condition, or <code>null</code>
   * if <code>null</code> if no such match occurs.
   */
  public static synchronized EventChannelRef selectActiveChannel(Condition<EventChannel> condition) {
    synchronized (CHANNELS_BY_DOMAIN) {
      if (eventChannelReuse) {
        for (EventChannel c : CHANNELS_BY_DOMAIN) {
          if (condition.apply(c)) {
            return new EventChannelRefImpl(c, false);
          }
        }
      }
      return null;
    }
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
    log.debug("Broadcasting async event %s to all domains - %s", type, data);
     return asyncExecutor.<Void>submit(() -> {
        try {
          broadcast.dispatch(unicast.getAddress(), alldomains, type, data);
        } catch (Exception e) {
          log.warning("Could not broadcast async event %s to all domains (%s)", e, type, data);
          throw new IllegalStateException("System error dispatching event", e);
        }
        return null;
      });
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#dispatch(ServerAddress, String,
   *      Object)
   */
  public Future<Void> dispatch(ServerAddress addr, String type, Object data) {
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
    log.debug("Sending async event %s - %s", type, data);
    return asyncExecutor.<Void>submit(() -> {
      try {
        unicast.dispatch(addr, type, data);
      } catch (Exception e) {
        log.warning("Could not send async event %s to %s (%s)", e, type, addr, data);
        throw new IllegalStateException("System error dispatching event", e);
      }
      return null;
    });
  }
  
  /**
   * @param addresses the {@link Collection} of unicast addresss to which to dispatch the remote
   *                  event
   * @param type      a remote event type.
   * @param data      the event payload.
   * @return the {@link List} of {@link Future} on which the caller may block.
   */
  public List<Future<Void>> dispatch(Collection<ServerAddress> addresses, String type, Object data) {
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
    log.debug("Sending async event %s - %s", type, data);
    List<Future<Void>> results = new ArrayList<>(addresses.size());
    
    for (final ServerAddress addr : addresses) {
      Future<Void> result =  asyncExecutor.<Void>submit(() -> {
        try {
          unicast.dispatch(addr, type, data);
        } catch (Exception e) {
          log.warning("Could not send async event %s to %s (%s)", e, type, addr, data);
          throw new IllegalStateException("System error dispatching event", e);
        }
        return null;
      });
      results.add(result);
    }
    
    return results;
  }

  /**
   * Dispatches the given data to all nodes in this instance's domain.
   *
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(String, String,
   *      Object)
   */
  public Future<Void> dispatch(String type, Object data) {
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
    log.debug("Broadcasting async event %s - %s", type, data);
    return asyncExecutor.<Void>submit(() -> {
      try {
        broadcast.dispatch(unicast.getAddress(), consumer.getDomainName().toString(), type, data);
      } catch (Exception e) {
        log.warning("Could not broadcast async event %s (%s)", e, type, data);
        throw new IllegalStateException("System error dispatching event", e);
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
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
  
  /**
   * Closes the statically cached event channels, and clears the cache.
   */
  public static void closeCachedChannels() {
    synchronized (CHANNELS_BY_DOMAIN) {
      List<EventChannel> channels = new ArrayList<EventChannel>(CHANNELS_BY_DOMAIN);
      for (EventChannel ec : channels) {
        ec.close();
        CHANNELS_BY_DOMAIN.remove(ec);
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

  // --------------------------------------------------------------------------
  // Restricted

  EventChannelController getController() {
    return controller;
  }

  void sendControlMessage(SplitteableMessage msg) {
    asyncExecutor.execute(() -> {
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
    asyncExecutor.execute(() -> {
      List<NodeInfo> candidates = controller.getContext().getEventChannel().getView(NodeInfo.NORMAL_NODES_FILTER);        
      Collections.shuffle(candidates);
      
      // Define number of nodes to send gossip to
      int minNodeCount = controller.getContext().getConfig().getGossipMinNodeCount();
      int stepLevelNodeCount = (int) Math.round(Math.log(candidates.size()));
      int gossipNodeCount = Math.max(minNodeCount, stepLevelNodeCount);
      
      int counter = 0;
      log.debug("Sending gossip notification: %s (to %s of %s candidates)", msg, gossipNodeCount, candidates.size());
      for (NodeInfo c : candidates) {
        try {
          log.debug("Sending gossip message to : %s", c);
          metrics.incrementCounter("eventChannel.gossipMessage");
          if (unicast.dispatch(c.getAddr(), CONTROL_EVT, msg)) {
            counter++;
            if (counter >= gossipNodeCount) {
              break;
            }
          } else {
            log.warning("Node %s deemed suspect on gossip dispatch failure", c.getAddr());
            c.suspect();
          }
         
        } catch (Exception e) {
          log.warning("System error sending gossip message to node %s", e, c.getAddr());
          c.suspect();
        }
      }
    });
  }
  
  private TimerTask doCreateTaskForPublishBroadcastEvent(final int maxAttempts) {
    Assertions.illegalState(state != State.STARTED, "Event channel not started");
    return new TimerTask() {
      int attempt = 0;
      @Override
      public void run() {
        if (attempt >= maxAttempts) {
          cancel();
        } else {
          log.info("Publishing presence of this node (%s) to cluster (attempt count = %s)", address, attempt);
          try {
            metrics.incrementCounter("eventChannelController.publishPresence");
            broadcast.dispatch(address, false, PUBLISH_EVT, address);
          } catch (Exception e) {
            log.warning("Error publishing presence to cluster", e);
          }
          attempt++;
        }
      }
    };
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
    public void cleanDeadNodes(long gracePeriodMillis) {
      view.cleanupDeadNodeList(gracePeriodMillis);
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
      return asyncExecutor.<Void>submit(() -> {
        try {
          broadcast.dispatch(getUnicastAddress(), false, CONTROL_EVT, event);
        } catch (Exception e) {
          log.warning("Could not broadcast async control event %s (%s)", e, CONTROL_EVT, event);
          throw new IllegalStateException("System error dispatching control event", e);
        }
        return null;
      });
    }
    
    @Override
    public Future<Void> sendUnicastEvent(final ServerAddress destination, final ControlEvent event) {
      return asyncExecutor.<Void>submit(() -> {
        try {
          NodeInfoWrapper wrapper = view.getWrapperFor(destination);
          if (wrapper != null) {
            if (wrapper.getThrottle().tryAcquire()) {
              unicast.dispatch(destination, CONTROL_EVT, event);
            } else {
              log.warning("Throttling limit reached, could not contact node %s at %s", wrapper.getNodeInfo().getNode(), destination);
            }
          } else {
            log.warning("Node corresponding to address %s not in view", destination);
          }
        } catch (IOException e) {
          log.error("Could not dispatch control event", e);
        } catch (Exception e) {
          log.warning("Could not send async event %s to %s (%s)", e, CONTROL_EVT, destination, event);
          throw new IllegalStateException("System error dispatching control event", e);
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
        NodeInfoWrapper wrapper = view.getWrapperFor(targetedNode);
        if (wrapper != null) {
          if (wrapper.getThrottle().tryAcquire()) {
            targetAddresses.add(wrapper.getNodeInfo().getAddr());
          } else {
            log.warning("Throttling limit reached, could not contact node %s at %s", 
                wrapper.getNodeInfo().getNode(), 
                wrapper.getNodeInfo().getAddr());
          }
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
        NodeInfoWrapper wrapper = view.getWrapperFor(targetedNode);
        if (wrapper != null) {
          if (wrapper.getThrottle().tryAcquire()) {
            targetAddresses.add(wrapper.getNodeInfo().getAddr());
          } else {
            log.warning("Throttling limit reached, could not contact node %s at %s", 
                wrapper.getNodeInfo().getNode(), 
                wrapper.getNodeInfo().getAddr());
          }
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
            metrics.incrementCounter("eventChannel.syncEvent.onControlRequest");
            return controller.onSynchronousRequest(evt.getNode(), evt.getUnicastAddress(), (SynchronousControlRequest) data);
          } else if (data instanceof ControlEvent) {
            metrics.incrementCounter("eventChannel.syncEvent.onControlEvent");
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

          metrics.incrementCounter("eventChannel.asyncEvent.onPublish");
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
          metrics.incrementCounter("eventChannel.asyncEvent.onForceResync");
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
          metrics.incrementCounter("eventChannel.asyncEvent.onDiscovery");
          if (view.addHost(addr, evt.getNode())) {
            notifyDiscoListeners(addr, evt);
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process event" + evt.getType(), e);
        }

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(SHUTDOWN_EVT)) {
        metrics.incrementCounter("eventChannel.asyncEvent.onShutdown");
        view.removeLeavingNode(evt.getNode());

        // ----------------------------------------------------------------------
        
      } else if (evt.getType().equals(LEAVE_EVT)) {
        metrics.incrementCounter("eventChannel.asyncEvent.onLeave");
        view.removeLeavingNode(evt.getNode());

        // ----------------------------------------------------------------------

      } else if (evt.getType().equals(CONTROL_EVT)) {
        try {
          Object data = evt.getData();
          if (data instanceof ControlNotification) {
            metrics.incrementCounter("eventChannel.asyncEvent.onControlNotif");
            controller.onNotification(evt.getNode(), evt.getUnicastAddress(), (ControlNotification) data);
          } else if (data instanceof GossipNotification) {
            metrics.incrementCounter("eventChannel.asyncEvent.onGossipNotif");
            controller.onGossipNotification(evt.getNode(), evt.getUnicastAddress(), (GossipNotification) data);
          } else if (data instanceof ControlEvent) {
            metrics.incrementCounter("eventChannel.asyncEvent.onControlEvent");
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
    int throttleThreshold = props.getIntProperty(Consts.MCAST_THROTTLE_THRESHOLD, Defaults.DEFAULT_THROTTLE_THRESHOLD);
    ThrottleFactory throttleFactory = throttleThreshold <= 0 ? () -> 
      new NullThrottle() : 
      new ThrottleFactory.RateThrottleFactory(throttleThreshold, TimeUnit.MILLISECONDS);
      
    view = new View(consumer.getNode(), throttleFactory);
    
    scheduler = new Timer("EventChannelScheduler");
    publisher = new Timer("EventChannelPublisher");
      
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
    
    controller = new EventChannelController(createClock(), config, new ChannelCallbackImpl(), metrics);

    startTimer(controlThreadInterval);
  }

  protected void startTimer(TimeValue controlThreadInterval) {
    scheduler.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          if (state == State.STARTED) {
            controller.checkStatus();
          }
        } catch (Exception e) {
          log.warning("System error running scheduler timer task", e);
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
