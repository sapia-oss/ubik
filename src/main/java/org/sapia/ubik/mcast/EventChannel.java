package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Log;


/**
 * An instance of this class represents a node in a given logical event channel. Instances of this
 * class are logically grouped on a per-domain basis. Remote events are sent/dispatched to other
 * instances of this class through the network.
 * <p>
 * A given <code>EventChannel</code> instance will only send/received events to/from other instances
 * of the same domain.
 *
 * @see org.sapia.ubik.mcast.DomainName
 * @see org.sapia.ubik.mcast.RemoteEvent
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class EventChannel {
  static final String  DISCOVER_EVT    = "ubik/mcast/discover";
  static final String  PUBLISH_EVT     = "ubik/mcast/publish";
  static final String  HEARTBEAT_EVT   = "ubik/mcast/heartbeat";
  private BroadcastDispatcher  _broadcast;
  private UnicastDispatcher    _unicast;
  private EventConsumer        _consumer;
  private ChannelEventListener _listener;
  private View                 _view           = new View(30000);
  private ServerAddress        _address;
  private List<DiscoveryListener> _discoListeners = new ArrayList<DiscoveryListener>();
  private boolean              _started;
  private boolean              _closed;

  /**
   * Constructor for EventChannel. For point-to-point communication, this instance will
   * open a UDP server on a random port.
   *
   * @param domain the domain name of this instance.
   * @param mcastHost the multicast address that this instance will use to broadcast remote events.
   * @param mcastPort the multicast port that this instance will use to broadcast remote events.
   *
   * @see DomainName
   */
  public EventChannel(String domain, String mcastHost, int mcastPort)
    throws IOException {
    _consumer    = new EventConsumer(domain);
    _broadcast   = new BroadcastDispatcherImpl(_consumer, mcastHost, mcastPort);
    _unicast     = new UDPUnicastDispatcher(10000, _consumer);
    init();
  }

  /**
   * Constructor for EventChannel.
   *
   * @param domain the domain name of this instance.
   * @param mcastHost the multicast address that this instance will use to broadcast remote events.
   * @param mcastPort the multicast port that this instance will use to broadcast remote events.
   * @param unicastPort the port of the UDP server that this instance encapsulates, and that is
   * used for point-to-point communication.
   *
   * @see DomainName
   */
  public EventChannel(String domain, String mcastHost, int mcastPort,
    int unicastPort) throws IOException {
    _consumer    = new EventConsumer(domain);
    
    String soTimeoutProp = System.getProperty(Consts.MCAST_HEARTBEAT_INTERVAL);
    int soTimeout = 20000;
    if(soTimeoutProp != null){
      try{
        soTimeout = Integer.parseInt(soTimeoutProp);
      }catch(NumberFormatException e){
        // use default
      }
    }
    _unicast     = new UDPUnicastDispatcher(soTimeout, unicastPort, _consumer);
    _broadcast   = new BroadcastDispatcherImpl(_consumer, mcastHost, mcastPort);
    init();    
  }

  /**
   * Returns this instance's domain name.
   *
   * @return a <code>DomainName</code>.
   */
  public DomainName getDomainName() {
    return _consumer.getDomainName();
  }

  /**
   * Returns this instance's multicast address.
   *
   * @return a host address as a string.
   */
  public String getMulticastHost() {
    return _broadcast.getMulticastAddress();
  }

  /**
   * Returns this instance's multicast port.
   *
   * @return a port.
   */
  public int getMulticastPort() {
    return _broadcast.getMulticastPort();
  }

  /**
   * Starts this instances. This method should be called after instantiating this instance, prior to start
   * receiving/sending remote events.
   *
   * @throws IOException if an IO problem occurs starting this instance.
   */
  public void start() throws IOException {
    _listener = new ChannelEventListener(this);
    _consumer.registerAsyncListener(PUBLISH_EVT, _listener);
    _consumer.registerAsyncListener(DISCOVER_EVT, _listener);
    _consumer.registerAsyncListener(HEARTBEAT_EVT, _listener);
    _unicast.setSoTimeoutListener(_listener);
    _unicast.setBufsize(2000);
    _broadcast.start();
    _unicast.start();
    _address = _unicast.getAddress();
    _broadcast.dispatch(_unicast.getAddress(), false, PUBLISH_EVT, _address);
    _started = true;
  }

  /**
   * @return <code>true</code> if the <code>start()</code> method was called on this instance.
   *
   * @see #start()
   */
  public boolean isStarted() {
    return _started;
  }

  /**
   * Closes this instance.
   */
  public void close() {
    _broadcast.close();
    _unicast.close();
    _closed = true;
  }

  /**
   * @return <code>true</code> if the <code>close()</code> method was called on this instance.
   *
   * @see #close()
   */
  public boolean isClosed() {
    return _closed;
  }

  /**
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(boolean, String, Object)
   */
  public void dispatch(boolean alldomains, String type, Object data)
    throws IOException {
    _broadcast.dispatch(_unicast.getAddress(), alldomains, type, data);
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#dispatch(ServerAddress, String, Object)
   */
  public void dispatch(ServerAddress addr, String type, Object data)
    throws IOException {
    _unicast.dispatch(addr, type, data);
  }

  /**
   * Dispatches the given data to all nodes in this instance's domain.
   *
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#dispatch(String, String, Object)
   */
  public void dispatch(String type, Object data) throws IOException {
    if(Log.isDebug()){
      Log.debug(getClass(), "Sending event " + type + " - " + data);
    }
    _broadcast.dispatch(_unicast.getAddress(), _consumer.getDomainName().toString(), type, data);
  }

  /**
   * Adds the given discovery listener to this instance.
   *
   * @param listener a <code>DiscoveryListener</code>.
   */
  public void addDiscoveryListener(DiscoveryListener listener) {
    _discoListeners.add(listener);
  }

  /**
   * Synchronously sends a remote event to the node corresponding to the given <code>ServerAddress</code>,
   * and returns the corresponding response.
   *
   * @param addr the <code>ServerAddress</code> of the node to which to send the remote event.
   * @param type the "logical type" of the remote event.
   * @param data the data to encapsulate in the remote event.
   *
   * @return the <code>Response</code> corresponding to this call.
   *
   * @see RemoteEvent
   */
  public Response send(ServerAddress addr, String type, Object data)
    throws IOException, TimeoutException {
    return _unicast.send(addr, type, data);
  }

  /**
   * Synchronously sends a remote event to all the nodes corresponding to the given <code>ServerAddress</code>,
   * and returns the corresponding responses.
   * <p>
   *
   * @param type the "logical type" of the remote event.
   * @param data the data to encapsulate in the remote event.
   *
   * @see org.sapia.ubik.mcast.RemoteEvent
   */
  public RespList send(String type, Object data) throws IOException {
    return _unicast.send(_view.getHosts(), type, data);
  }

  /**
   * Registers a listener of asynchronous remote events of the given type.
   *
   * @param type the logical type of the remotee events to listen for.
   * @param listener an <code>AsyncEventListener</code>.
   */
  public synchronized void registerAsyncListener(String type,
    AsyncEventListener listener) {
    _consumer.registerAsyncListener(type, listener);
  }

  /**
   * Registers a listener of synchronous remote events of the given type.
   *
   * @param type the logical type of the remotee events to listen for.
   * @param listener a <code>SyncEventListener</code>.
   *
   * @throws ListenerAlreadyRegisteredException if a listener has already been
   * registered for the given event type.
   */
  public synchronized void registerSyncListener(String type,
    SyncEventListener listener) throws ListenerAlreadyRegisteredException {
    _consumer.registerSyncListener(type, listener);
  }

  /**
   * Unregisters the given listener from this instance.
   *
   * @param listener a <code>ASyncEventListener</code>.
   */
  public synchronized void unregisterListener(AsyncEventListener listener) {
    _consumer.unregisterListener(listener);
  }

  /**
   * Returns this instance's "view".
   *
   * @return a <code>View</code>.
   */
  public View getView() {
    return _view;
  }

  /**
   * @see EventConsumer#containsAsyncListener(AsyncEventListener)
   */
  public synchronized boolean containsAsyncListener(AsyncEventListener listener) {
    return _consumer.containsAsyncListener(listener);
  }

  /**
   * @see EventConsumer#containsSyncListener(SyncEventListener)
   */
  public synchronized boolean containsSyncListener(SyncEventListener listener) {
    return _consumer.containsSyncListener(listener);
  }

  /**
   * @see BroadcastDispatcher#setBufsize(int)
   * @see UnicastDispatcher#setBufsize(int)
   */
  public void setBufsize(int size) {
    _broadcast.setBufsize(size);
    _unicast.setBufsize(size);
  }

  /**
   * @see BroadcastDispatcher#getNode()
   */
  public String getNode() {
    return _broadcast.getNode();
  }

  public static class ChannelEventListener implements AsyncEventListener,
    SocketTimeoutListener {
    private EventChannel _owner;

    ChannelEventListener(EventChannel channel) {
      _owner = channel;
    }

    /**
     * @see org.sapia.ubik.mcast.SocketTimeoutListener#handleSoTimeout()
     */
    public void handleSoTimeout() {
      _owner._view.removeDeadHosts();

      List<ServerAddress> siblings = _owner._view.getHosts();

      if (_owner._address != null) {
        for (int i = 0; i < siblings.size(); i++) {
          try {
            _owner.dispatch((ServerAddress) siblings.get(i), HEARTBEAT_EVT,
              _owner._address);
          } catch (IOException e) {
            e.printStackTrace();

            break;
          }
        }
      }
    }

    /**
     * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
     */
    public void onAsyncEvent(RemoteEvent evt) {
      if (evt.getType().equals(DISCOVER_EVT)) {
        ServerAddress addr;

        try {
          addr = (ServerAddress) evt.getData();
          if(addr == null){
            return;
          }
          _owner._view.addHost(addr, evt.getNode());

          List<DiscoveryListener> listeners = _owner._discoListeners;

          for (int i = 0; i < listeners.size(); i++) {
            ((DiscoveryListener) listeners.get(i)).onDiscovery(addr, evt);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (evt.getType().equals(PUBLISH_EVT)) {
        try {
          ServerAddress addr = (ServerAddress) evt.getData();
          if(addr == null){
            return;
          }          

          _owner._view.addHost(addr, evt.getNode());
          _owner.dispatch(false, DISCOVER_EVT, _owner._address);

          List<DiscoveryListener> listeners = _owner._discoListeners;

          for (int i = 0; i < listeners.size(); i++) {
            ((DiscoveryListener) listeners.get(i)).onDiscovery(addr, evt);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (evt.getType().equals(HEARTBEAT_EVT)) {
        try {
          ServerAddress addr = (ServerAddress) evt.getData();

          _owner._view.heartbeat(addr, evt.getNode());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private void init(){
    String bufsizeStr = System.getProperty(Consts.MCAST_BUFSIZE_KEY);
    if(bufsizeStr != null){
      try{
        int buf = Integer.parseInt(bufsizeStr);
        if(buf > 0){
          _broadcast.setBufsize(buf);
          _unicast.setBufsize(buf);
        }
      }catch(NumberFormatException e){
        // use default;
      }
    }
    
    String heartBeatTimeout = System.getProperty(Consts.MCAST_HEARTBEAT_TIMEOUT);
    if(heartBeatTimeout != null){
      try{
        _view.setTimeout(Long.parseLong(heartBeatTimeout));
      }catch(NumberFormatException e){
        // use default;
      }
    }
  }
}
