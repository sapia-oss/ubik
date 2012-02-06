package org.sapia.ubik.mcast;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.udp.UDPBroadcastDispatcher;
import org.sapia.ubik.mcast.udp.UDPUnicastDispatcher;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;


/**
 * An instance of this class represents a node in a given logical event channel. Instances of this
 * class are logically grouped on a per-domain basis. Remote events are sent/dispatched to other
 * instances of this class through the network.
 * <p>
 * An instance of this class will only send/received events to/from other instances
 * of the same domain.
 *
 * @see org.sapia.ubik.mcast.DomainName
 * @see org.sapia.ubik.mcast.RemoteEvent
 *
 * @author Yanick Duchesne
 */
public class EventChannel {
  
  static final String  DISCOVER_EVT    = "ubik/mcast/discover";
  static final String  PUBLISH_EVT     = "ubik/mcast/publish";
  static final String  HEARTBEAT_EVT   = "ubik/mcast/heartbeat";
  
  private Category                log            = Log.createCategory(getClass());
  private Timer                   heartbeatTimer = new Timer("Ubik.EventChannel.Timer", true);
  private BroadcastDispatcher     broadcast;
  private UnicastDispatcher       unicast;
  private EventConsumer           consumer;
  private ChannelEventListener    listener;
  private View                    view           = new View(Defaults.DEFAULT_HEARTBEAT_TIMEOUT);
  private ServerAddress           address;
  private List<SoftReference<DiscoveryListener>> discoListeners = Collections.synchronizedList(new ArrayList<SoftReference<DiscoveryListener>>());
  
  private volatile State          state          = State.CREATED;
  /**
   * Creates an instance of this class that will use IP multicast.
   *
   * @param domain the domain name of this instance.
   * @param mcastHost the multicast address that this instance will use to broadcast remote events.
   * @param mcastPort the multicast port that this instance will use to broadcast remote events.
   * @throws IOException if a problem occurs creating this instance.
   * @see DomainName
   */
  public EventChannel(String domain, String mcastHost, int mcastPort)
    throws IOException {
    consumer    = new EventConsumer(domain);
    broadcast   = new UDPBroadcastDispatcher(consumer, mcastHost, mcastPort);
    unicast     = new UDPUnicastDispatcher(consumer);
    init(new Props().addSystemProperties());
  }
  
  /**
   * Creates an instance of this class that will use the given properties to configures
   * its internal unicast and broadcast dispatchers.
   * 
   * @param domain the domain name of this instance.
   * @param config the {@link Properties} containing unicast and multicast configuration.
   * @throws IOException if a problem occurs creating this instance.
   * @see UnicastDispatcher
   * @see BroadcastDispatcher
   */
  public EventChannel(String domain, Props config) throws IOException {
    consumer  = new EventConsumer(domain);
    unicast   = UnicastDispatcherFactory.createUnicastDispatcher(consumer, config);
    broadcast = UnicastDispatcherFactory.createBroadcastDispatcher(consumer, config);
    init(config);
  }
  
  /**
   * @param consumer the {@link EventConsumer} that the event channel will use.
   * @param unicast the {@link UnicastDispatcher} that the event channel will use.
   * @param broadcast the {@link BroadcastDispatcher} that the event channel will use.
   */
  public EventChannel(EventConsumer consumer, UnicastDispatcher unicast, BroadcastDispatcher broadcast) {
    this.consumer  = consumer;
    this.unicast   = unicast;
    this.broadcast = broadcast;
    init(new Props().addSystemProperties());
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
   * Starts this instances. This method should be called after instantiating this instance, prior to start
   * receiving/sending remote events.
   *
   * @throws IOException if an IO problem occurs starting this instance.
   */
  public synchronized void start() throws IOException {
    if(state == State.CREATED) {
      broadcast.start();
      unicast.start();
      address = unicast.getAddress();
      broadcast.dispatch(unicast.getAddress(), false, PUBLISH_EVT, address);
      state = State.STARTED;
    }
  }

  /**
   * Closes this instance.
   */
  public synchronized void close() {
    if(state == State.STARTED) {
      heartbeatTimer.cancel();
      broadcast.close();
      unicast.close();
      state = State.CLOSED;
    }
  }
  
  /**
   * @return <code>true</code> if the {@link #start()} method was called on this instance.
   *
   * @see #start()
   */
  public boolean isStarted() {
    return state == State.STARTED;
  }

  /**
   * @return <code>true</code> if the {@link #close()} method was called on this instance.
   *
   * @see #close()
   */
  public boolean isClosed() {
    return state == State.CLOSED;
  }

  /**
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(boolean, String, Object)
   */
  public void dispatch(boolean alldomains, String type, Object data)
    throws IOException {
    broadcast.dispatch(unicast.getAddress(), alldomains, type, data);
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#dispatch(ServerAddress, String, Object)
   */
  public void dispatch(ServerAddress addr, String type, Object data)
    throws IOException {
    unicast.dispatch(addr, type, data);
  }

  /**
   * Dispatches the given data to all nodes in this instance's domain.
   *
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(String, String, Object)
   */
  public void dispatch(String type, Object data) throws IOException {
    log.debug("Sending event %s - %s", type, data);
    broadcast.dispatch(unicast.getAddress(), consumer.getDomainName().toString(), type, data);
  }


  /**
   * Synchronously sends a remote event to the node corresponding to the given {@link ServerAddress},
   * and returns the corresponding response.
   *
   * @param addr the {@link ServerAddress} of the node to which to send the remote event.
   * @param type the "logical type" of the remote event.
   * @param data the data to encapsulate in the remote event.
   *
   * @return the {@link Response} corresponding to this call.
   *
   * @see RemoteEvent
   */
  public Response send(ServerAddress addr, String type, Object data)
    throws IOException, TimeoutException {
    return unicast.send(addr, type, data);
  }
  
  /**
   * @see UnicastDispatcher#send(List, String, Object)
   */
  public RespList send(List<ServerAddress> addresses, String type, Object data) 
    throws IOException, TimeoutException, InterruptedException {
    return unicast.send(addresses, type, data);
  }

  /**
   * Synchronously sends a remote event to all this instance's nodes and returns the corresponding responses.
   *
   * @see UnicastDispatcher#send(List, String, Object)
   */
  public RespList send(String type, Object data) throws IOException, InterruptedException {
    return unicast.send(view.getHosts(), type, data);
  }

  /**
   * Registers a listener of asynchronous remote events of the given type.
   *
   * @param type the logical type of the remote events to listen for.
   * @param listener an {@link AsyncEventListener}.
   */
  public synchronized void registerAsyncListener(String type,
    AsyncEventListener listener) {
    consumer.registerAsyncListener(type, listener);
  }

  /**
   * Registers a listener of synchronous remote events of the given type.
   *
   * @param type the logical type of the remote events to listen for.
   * @param listener a {@link SyncEventListener}.
   *
   * @throws ListenerAlreadyRegisteredException if a listener has already been
   * registered for the given event type.
   */
  public synchronized void registerSyncListener(String type,
    SyncEventListener listener) throws ListenerAlreadyRegisteredException {
    consumer.registerSyncListener(type, listener);
  }

  /**
   * Unregisters the given listener from this instance.
   *
   * @param listener an {@link AsyncEventListener}.
   */
  public synchronized void unregisterAsyncListener(AsyncEventListener listener) {
    consumer.unregisterListener(listener);
  }
  
  /**
   * Unregisters the given listener from this instance.
   *
   * @param listener an {@link SyncEventListener}.
   */
  public synchronized void unregisterSyncListener(SyncEventListener listener) {
    consumer.unregisterListener(listener);
  }
  
  /**
   * Adds the given listener to this instance.
   * 
   * @see View#addEventChannelStateListener(EventChannelStateListener)
   */
  public synchronized void addEventChannelStateListener(EventChannelStateListener listener){
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
   * @param listener a {@link DiscoveryListener}.
   */
  public void addDiscoveryListener(DiscoveryListener listener) {
    discoListeners.add(new SoftReference<DiscoveryListener>(listener));
  }
  
  /**
   * Removes the given discovery listener from this instance.
   * @param listener a {@link DiscoveryListener}.
   * @return <code>true</code> if the removal occurred.
   */
  public boolean removeDiscoveryListener(DiscoveryListener listener) {
    synchronized(discoListeners) {
      for(int i = 0; i < discoListeners.size(); i++) {
        SoftReference<DiscoveryListener> listenerRef = discoListeners.get(i);
        DiscoveryListener registered = listenerRef.get();
        if(registered != null && registered.equals(listener)) {
          discoListeners.remove(i);
          return true;
        }
      }
    }
    return false;
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
  
  private enum State {
    CREATED, STARTED, CLOSED;
  }

  private class ChannelEventListener implements AsyncEventListener {

    private void checkDeadHosts() {
      view.removeDeadHosts();

      List<ServerAddress> siblings = view.getHosts();

      log.debug("Sending heartbeat to other nodes: %s", siblings);
      if (address != null) {
        for (int i = 0; i < siblings.size(); i++) {
          try {
            log.debug("Sending heartbeat from %s to %s", consumer.getNode(), address);
            dispatch((ServerAddress) siblings.get(i), HEARTBEAT_EVT, address);
          } catch (IOException e) {
            log.warning("Exception caught while trying to dispatch heartbeat event to host (may be down): " + address, e);
            break;
          }
        }
      }
    }

    /**
     * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
     */
    public void onAsyncEvent(RemoteEvent evt) {
      
      // ----------------------------------------------------------------------
      
      if (evt.getType().equals(DISCOVER_EVT)) {
        ServerAddress addr;

        try {
          addr = (ServerAddress) evt.getData();
          if(addr == null){
            return;
          }
          if(view.addHost(addr, evt.getNode())) {
            notifyDiscoListeners(addr, evt);
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process discovery event", e);
        }
      
      // ----------------------------------------------------------------------
        
      } else if (evt.getType().equals(HEARTBEAT_EVT)) {
        try {
          ServerAddress addr = (ServerAddress) evt.getData();

          view.heartbeat(addr, evt.getNode());
        } catch (IOException e) {
          log.error("Error caught while trying to process heartbeat event", e);
        }
        
      // ----------------------------------------------------------------------
        
      } else {
        try {
          ServerAddress addr = (ServerAddress) evt.getData();
          if(addr == null){
            return;
          }          

          if(view.addHost(addr, evt.getNode())){
            dispatch(false, DISCOVER_EVT, address);
            notifyDiscoListeners(addr, evt);
          }
        } catch (IOException e) {
          log.error("Error caught while trying to process event " + evt.getType(), e);
        }
      }
    }
  }
  
  private void notifyDiscoListeners(ServerAddress addr, RemoteEvent evt) {
    synchronized(discoListeners) {
      for (int i = 0; i < discoListeners.size(); i++){
        SoftReference<DiscoveryListener> listenerRef = discoListeners.get(i);
        DiscoveryListener listener = listenerRef.get();
        if(listener != null) {
          listener.onDiscovery(addr, evt);
        } else {
          discoListeners.remove(i--);
        } 
      }
    }
  }
  
  private void init(Props props){
    listener = new ChannelEventListener();
    consumer.registerAsyncListener(PUBLISH_EVT,   listener);
    consumer.registerAsyncListener(DISCOVER_EVT,  listener);
    consumer.registerAsyncListener(HEARTBEAT_EVT, listener);
    


    long heartbeatTimeout  = props.getLongProperty(Consts.MCAST_HEARTBEAT_TIMEOUT, Defaults.DEFAULT_HEARTBEAT_TIMEOUT);
    long heartbeatInterval = props.getLongProperty(Consts.MCAST_HEARTBEAT_INTERVAL, Defaults.DEFAULT_HEARTBEAT_INTERVAL);
    
    log.debug("Heartbeat timeout set to %s",  heartbeatTimeout);
    log.debug("Heartbeat interval set to %s", heartbeatInterval);
    
    view.setTimeout(heartbeatTimeout);
    heartbeatTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            if(state == State.STARTED) {
              listener.checkDeadHosts();
            }
          }
        }, 0, 
        heartbeatInterval
    );
    
  }
}
