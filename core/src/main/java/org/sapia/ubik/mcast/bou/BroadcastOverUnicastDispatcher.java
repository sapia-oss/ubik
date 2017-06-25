package org.sapia.ubik.mcast.bou;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.control.SplitteableMessage;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Base64;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Serialization;

/**
 * Implements the {@link BroadcastDispatcher} interface over the {@link UnicastDispatcher} interface.
 * 
 * @author yduchesne
 *
 */
public class BroadcastOverUnicastDispatcher implements AsyncEventListener, BroadcastDispatcher  {
  
  // --------------------------------------------------------------------------
  // Abstracts the cluster view, and other low-level details. 
  
  public interface ViewCallback {
    
    public ServerAddress getAddressFor(String node);
    
    public String getNode();
    
    public DomainName getDomainName();
    
    public void register(String eventType, AsyncEventListener listener);
    
    public void notifyAsyncListeners(RemoteEvent event);
    
    public Set<String> getOtherNodes();
  }

  // Main impl.
  
  private static final String BROADCAST_EVT = "BROADCAST_OVER_UNICAST_EVT";

  private Category log = Log.createCategory(getClass());
  
  private ViewCallback      view;
  private UnicastDispatcher delegate;
  private BroadcastOverUnicastMulticastAddress address;
  
  private int controlBatchSize;
  
  public BroadcastOverUnicastDispatcher(ViewCallback view, UnicastDispatcher delegate, int controlBatchSize) {
    Assertions.illegalState(delegate instanceof BroadcastOverUnicastMulticastAddress, "Delegate cannot be instance of this instance's class");
    this.view             = view;
    this.delegate         = delegate;
    this.controlBatchSize = controlBatchSize;
    view.register(BROADCAST_EVT, this);
    this.address          = new BroadcastOverUnicastMulticastAddress(delegate.getAddress());
  }
  
  // --------------------------------------------------------------------------
  // AyncEventListener interface
  
  @Override
  public void onAsyncEvent(RemoteEvent evt) {
    try {
      log.debug("Received remote event %s", evt.getType());
      BroadcastOverUnicastMessage msg = (BroadcastOverUnicastMessage) evt.getData();
      view.notifyAsyncListeners(msg.getEvent());
      sendBroadcast(msg);
    } catch (IOException e) {
      log.error("Could not deserialize remote event payload", e);
    }
  }
  
  // --------------------------------------------------------------------------
  // BroadcastDispatcher interface
  
  @Override
  public MulticastAddress getMulticastAddress() {
    return address;
  }
  
  @Override
  public MulticastAddress getMulticastAddressFrom(Conf props) {
    try {
      byte[] payload = Base64.decode(props.getNotNullProperty(Consts.BROADCAST_UNICAST_ADDRESS));
      return new BroadcastOverUnicastMulticastAddress((ServerAddress) Serialization.deserialize(payload));
    } catch (Exception e) {
      throw new IllegalStateException("Could not deserialize address", e);
    }
  }
  
  @Override
  public void dispatch(ServerAddress unicastAddr, boolean alldomains, String type, Object data) throws IOException {
    dispatch(unicastAddr, null, type, data);
  }
  
  @Override
  public void dispatch(ServerAddress unicastAddr, String domain, String evtType, Object data) throws IOException {
    log.debug("Dispatching event for: %s", evtType);

    RemoteEvent evt;

    if (domain == null) {
      evt = new RemoteEvent(null, evtType, data).setNode(view.getNode());
    } else {
      evt = new RemoteEvent(domain, evtType, data).setNode(view.getNode());
    }
    evt.setUnicastAddress(unicastAddr);
    
    sendBroadcast(new BroadcastOverUnicastMessage(evt, view.getOtherNodes()));
  }
  
  @Override
  public void initialize(DispatcherContext context) {
    // noop
  }
  
  public void addConnectionStateListener(org.sapia.ubik.net.ConnectionStateListener listener) {
    // noop
  }
  
  @Override
  public void removeConnectionStateListener(ConnectionStateListener listener) {
    // noop
  }
 
  @Override
  public String getNode() {
    return view.getNode();
  }
  
  @Override
  public void start() {
    // noop
  }
  
  @Override
  public void close() {
    // noop
  }
  
  // --------------------------------------------------------------------------
  // Restricted

  void sendBroadcast(SplitteableMessage msg) {
    msg.getTargetedNodes().remove(view.getNode());
    if (!msg.getTargetedNodes().isEmpty()) {
      log.debug("Sending broadcast message %s to nodes: %s", msg.getClass().getSimpleName(), msg.getTargetedNodes());
      List<SplitteableMessage> splits = msg.split(controlBatchSize);
      log.debug("Got %s splits", splits.size());
      for (SplitteableMessage toSend : splits) {
        log.debug("Got %s targets in split", toSend.getTargetedNodes());
        ServerAddress address = null;
        while (address == null && !toSend.getTargetedNodes().isEmpty()) {
          try {
            String next = toSend.getTargetedNodes().iterator().next();
            toSend.getTargetedNodes().remove(next);
            address = view.getAddressFor(next);
            if (address != null) {
              log.debug("Sending broadcast message %s with %s targeted nodes to next node %s (%s)", toSend.getClass().getSimpleName(), toSend
                  .getTargetedNodes().size(), next, address);
              delegate.dispatch(address, BROADCAST_EVT, toSend);
            } else {
              log.debug("Could not resolve address for node %s", next);
            }
            Thread.yield();
            break;
          } catch (Exception e) {
            log.debug("Could not send broadcast message to %s", e, address);
            address = null;
          }
        }
      }
    }
  }
  
}
