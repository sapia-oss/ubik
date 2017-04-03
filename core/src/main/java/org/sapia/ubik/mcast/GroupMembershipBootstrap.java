package org.sapia.ubik.mcast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.bou.BroadcastOverUnicastDispatcher;
import org.sapia.ubik.mcast.group.GroupMembershipListener;
import org.sapia.ubik.mcast.group.GroupMembershipService;
import org.sapia.ubik.mcast.group.GroupMembershipServiceFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Serialization;
import org.sapia.ubik.util.TimeValue;

/**
 * An instance of this class internally uses a {@link GroupMembershipService} to discover the members in a given domain/cluster.
 * It creates an {@link EventChannel} whose {@link BroadcastDispatcher} will in fact be an instance of the {@link BroadcastOverUnicastDispatcher}
 * (meaning in fact that broadcast will not depend on IP multicast or some pub/sub technology, but rather on a {@link UnicastDispatcher} 
 * (also created internally by this instance).
 * 
 * @author yduchesne
 *
 */
public class GroupMembershipBootstrap {

  private Category log = Log.createCategory(getClass());
  
  private static final String BOOTSTRAP_EVENT = "boostrap-event"; 
  
  private EventConsumer          consumer;
  private Conf                   config  = Conf.newInstance().addSystemProperties();
  private GroupMembershipService groupMemberShipService;
  private UnicastDispatcher      unicast;
  private EventChannel           eventChannel;
  
  private List<RemoteEvent>      deferredEvents = Collections.synchronizedList(new ArrayList<RemoteEvent>());
  
  private volatile boolean started;
  
  /**
   * @param consumer the {@link EventConsumer} instance to encapsulate.
   * @param config   the {@link Conf} instance to use for retrieve this instance's configuration.
   */
  public GroupMembershipBootstrap(EventConsumer consumer, Conf config) {
    this.consumer = consumer;
    this.config   = config;
  }
  
  /**
   * @param domain the domain name corresponding to the domain/cluster to join.
   * @param config   the {@link Conf} instance to use for retrieve this instance's configuration.
   * @throws IOException if an I/O error internally occurs.
   */
  public GroupMembershipBootstrap(String domain, Conf config) throws IOException {
    this(new EventConsumer(domain,
        config.getIntProperty(Consts.MCAST_CHANNEL_CONSUMER_THREAD_COUNT, Defaults.DEFAULT_CHANNEL_CONSUMER_THREAD_COUNT),
        config.getIntProperty(Consts.MCAST_CHANNEL_CONSUMER_QUEUE_SIZE, Defaults.DEFAULT_CHANNEL_CONSUMER_QUEUE_SIZE)), config);
  }
  
  /**
   * @return this instance's {@link EventChannel}.
   */
  public EventChannel getEventChannel() {
    Assertions.illegalState(eventChannel == null, "EventChannel not initialized (invoke this instance's start method before anything else)");
    return eventChannel;
  }
  
  /**
   * Starts this instance with a given initial remote event, which will be dispatched to other nodes at startup.
   * 
   * @param initialEvent an initial {@link RemoteEvent} to publish (which could hold any specific information
   * useful during the discovery phase).
   * @param initialEventListener an {@link AsyncEventListener} to register in order to be notified of initial events coming from
   * other members.
   * @throws IOException if an I/O error occurs.
   */
  public void startWith(RemoteEvent initialEvent, AsyncEventListener initialEventListener) throws IOException {
    Assertions.illegalState(started, "Instance already started");
    int controlSplitSize                          = config.getIntProperty(Consts.MCAST_CONTROL_SPLIT_SIZE, Defaults.DEFAULT_CONTROL_SPLIT_SIZE);
    GroupMembershipService groupMembershipService = GroupMembershipServiceFactory.createGroupMemberShipService(config);
    UnicastDispatcher      unicast                = DispatcherFactory.createUnicastDispatcher(consumer, config);
    
    start(initialEvent.getCopy(consumer.getDomainName().toString()), initialEventListener, groupMembershipService, unicast, controlSplitSize);
  }

  /**
   * @throws IOException if an I/O error occurs.
   */
  public void start() throws IOException {
    startWith(new RemoteEvent(consumer.getDomainName().toString(), BOOTSTRAP_EVENT , BOOTSTRAP_EVENT), new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        // noop
      }
    });
  }
  
  /**
   * Closes this instance (internally releases system resources).
   */
  public synchronized void close() {
    if (started) {
      if (groupMemberShipService != null) {
        groupMemberShipService.close();
      }
      if (unicast != null) {
        unicast.close();
      }
      if (eventChannel != null) {
        eventChannel.close();
      }
      if (consumer != null) {
        consumer.stop();
      }
      started = false;
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted
 
  // visible for testing
  protected synchronized void start(RemoteEvent initialEvent, AsyncEventListener initialEventListener,
      GroupMembershipService groupMemberShipService, UnicastDispatcher unicast, int controlSplitSize) throws IOException {
    log.info("Bootstrapping group membership");
    
    this.groupMemberShipService = groupMemberShipService;
    this.unicast                = unicast;
    
    BroadcastDispatcher    broadcast = new BroadcastOverUnicastDispatcher(new BroadcastOverUnicastDispatcher.ViewCallback() {
      @Override
      public void register(String eventType, AsyncEventListener listener) {
        consumer.registerAsyncListener(eventType, listener);
      }
      
      @Override
      public void notifyAsyncListeners(RemoteEvent event) {
        consumer.onAsyncEvent(event);
      }
      
      @Override
      public String getNode() {
        return consumer.getNode();
      }
      
      @Override
      public DomainName getDomainName() {
        return consumer.getDomainName();
      }
      
      @Override
      public ServerAddress getAddressFor(String node) {
        if (eventChannel != null) {
          return eventChannel.getView().getAddressFor(node);
        }
        return null;
      }
      @Override
      public Set<String> getOtherNodes() {
        return eventChannel.getView().getNodesAsSet();
      }
    }, unicast, controlSplitSize);
    
    unicast.start();
    initialEvent.setNode(consumer.getNode()).setUnicastAddress(unicast.getAddress());
    groupMemberShipService.start();    
    groupMemberShipService.joinGroup(
        consumer.getDomainName().get(0), 
        consumer.getNode(), 
        Serialization.serialize(initialEvent), 
        new DiscoveryGroupMembershipListener()
    );
    
    eventChannel = new EventChannel(consumer, new UnicastDispatcherWrapper(unicast), broadcast);
    eventChannel.registerAsyncListener(initialEvent.getType(), initialEventListener);
    eventChannel.start();
    started = true;
    
    if (!deferredEvents.isEmpty()) {
      for (RemoteEvent evt : deferredEvents) {
        log.debug("Dispatching deferred event: %s", evt.getType());
        consumer.onAsyncEvent(evt);
      }
      deferredEvents.clear();
    }
  }
  
  // --------------------------------------------------------------------------
  // Listens for new members
  
  private class DiscoveryGroupMembershipListener implements GroupMembershipListener {
  
    @Override
    public void onMemberDiscovered(String memberId, byte[] payload) {
      try {
        RemoteEvent initialEvent = (RemoteEvent) Serialization.deserialize(payload);
        log.debug("Discovered new member %s. Got initial event: %s", memberId, initialEvent.getType());
        
        // simulating publish event
        RemoteEvent discoEvent = new RemoteEvent(
            consumer.getDomainName().toString(), 
            EventChannel.PUBLISH_EVT, 
            initialEvent.getUnicastAddress()
          )
          .setUnicastAddress(initialEvent.getUnicastAddress())
          .setNode(initialEvent.getNode());
        
        if (started) {
          consumer.onAsyncEvent(discoEvent);
          consumer.onAsyncEvent(initialEvent);
        } else {
          deferredEvents.add(discoEvent);
          deferredEvents.add(initialEvent);
        }
      } catch (Exception e) {
        Log.error("Could not deserialize discovery payload", e);
      }
    }
    
    @Override
    public void onMemberLeft(String memberId) {
      // event channel should have been instantiated at this point.
      if (eventChannel != null) {
        eventChannel.getView().removeLeavingNode(memberId);
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // Wraps the UnicastDispatcher that the DiscoveryBootstrap initially acquires
  // so that life-cycle methods have no effect (all other calls delegated to
  // the original dispatcher, kept internally).
  
  private class UnicastDispatcherWrapper implements UnicastDispatcher {
    
    private UnicastDispatcher delegate;
    
    private UnicastDispatcherWrapper(UnicastDispatcher delegate) {
      this.delegate = delegate;
    }
    
    @Override
    public ServerAddress getAddress() throws IllegalStateException {
      return delegate.getAddress();
    }
    
    @Override
    public boolean dispatch(ServerAddress addr, String type, Object data) throws IOException {
      return delegate.dispatch(addr, type, data);
    }
    
    @Override
    public RespList send(List<ServerAddress> addresses, String type, Object data, TimeValue timeout)
        throws IOException, InterruptedException {
      return delegate.send(addresses, type, data, timeout);
    }
    
    @Override
    public Response send(ServerAddress addr, String type, Object data, TimeValue timeout) throws IOException {
      return delegate.send(addr, type, data, timeout);
    }
    
    @Override
    public RespList send(ServerAddress[] addresses, String type, Object[] data, TimeValue timeout)
        throws IOException, InterruptedException {
      return delegate.send(addresses, type, data, timeout);
    }
    
    // ------------------------------------------------------------------------
    // We do not want the EventChannel instance to handle this instance's
    // lifecycle

    @Override
    public void initialize(EventConsumer consumer, Conf config) {
      // noop (makes no sense outside of the DispatcherFactory)
    }
    
    @Override
    public void close() {
      // noop
    }
    
    @Override
    public void start() {
      // noop
    }
  }
}
