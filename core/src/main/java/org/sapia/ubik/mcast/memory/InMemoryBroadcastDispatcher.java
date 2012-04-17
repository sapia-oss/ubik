package org.sapia.ubik.mcast.memory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;

/**
 * An in-memory {@link BroadcastDispatcher}. An instance of this class registers/unregisters itself with/from
 * the {@link InMemoryDispatchChannel} singleton at startup/shutdown.
 * 
 * @author yduchesne
 *
 */
public class InMemoryBroadcastDispatcher implements BroadcastDispatcher{
  
  private Category                 log       = Log.createCategory(getClass()); 
  private InMemoryDispatchChannel  channel   = InMemoryDispatchChannel.getInstance();
  private EventConsumer            consumer;
  private String                   domain;

  private InMemoryMulticastAddress address;
  
  /**
   * @param consumer the {@link EventConsumer} to notify of incoming remote events.
   */
  public InMemoryBroadcastDispatcher(EventConsumer consumer) {
    this.consumer = consumer;
    this.address  = new InMemoryMulticastAddress(UUID.randomUUID().toString());
    this.domain   = consumer.getDomainName().toString();
  }
  
  @Override
  public void start() {
    channel.registerDispatcher(this);
  }
  
  @Override
  public void close() {
    channel.unregisterDispatcher(this);
  }
  
  @Override
  public String getNode() {
    return consumer.getNode();
  }
  
  @Override
  public MulticastAddress getMulticastAddress() {
    return address;
  }
  
  @Override
  public void dispatch(
      ServerAddress unicastAddr, 
      boolean alldomains,
      String evtType, 
      Object data) throws IOException {
    
    log.debug("Dispatching event for: %s", evtType);

    RemoteEvent evt;

    if (alldomains) {
      evt = new RemoteEvent(null, evtType, data).setNode(consumer.getNode());
    } else {
      evt = new RemoteEvent(domain, evtType, data).setNode(consumer.getNode());
    }
    evt.setUnicastAddress(unicastAddr);
    channel.dispatch(this, evt);
  }
  
  @Override
  public void dispatch(
      ServerAddress unicastAddr, 
      String domain, 
      String evtType,
      Object data) throws IOException {
    
    log.debug("Dispatching event for: %s", evtType);

    RemoteEvent evt;

    evt = new RemoteEvent(domain, evtType, data).setNode(consumer.getNode());
    evt.setUnicastAddress(unicastAddr);
    channel.dispatch(this, evt);
  }
  
  EventConsumer getConsumer() {
    return consumer;
  }
  
  // --------------------------------------------------------------------------
  
  public static class InMemoryMulticastAddress implements MulticastAddress {
    
    static final long serialVersionUID = 1L;
    
    public static final String TRANSPORT  = "mem/broadcast";
    
    private String node;
    
    public InMemoryMulticastAddress(String node) {
      this.node = node;
    }    
    
    @Override
    public String getTransportType() {
      return TRANSPORT;
    }
    
    public String getNode() {
      return node;
    }
    
    @Override
    public int hashCode() {
      return node.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      if(obj instanceof InMemoryMulticastAddress) {
        InMemoryMulticastAddress other = (InMemoryMulticastAddress) obj;
        return other.node.equals(node);
      }
      return false;
    }
    
    @Override
    public Map<String, String> toParameters() {
      Map<String, String> params = new HashMap<String, String>();
      params.put(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
      params.put(Consts.BROADCAST_MEMORY_NODE, node);
      return params;
    }
    
    public String toString() {
      return String.format("[%s]", node);
    }
  }  

}
