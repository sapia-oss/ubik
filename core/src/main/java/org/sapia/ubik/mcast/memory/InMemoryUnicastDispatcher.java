package org.sapia.ubik.mcast.memory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements an in-memory {@link UnicastDispatcher}.
 * 
 * @author yduchesne
 * 
 */
public class InMemoryUnicastDispatcher implements UnicastDispatcher {

  private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
  
  private int instanceId = INSTANCE_COUNTER.incrementAndGet();
  private Category                log     = Log.createCategory(getClass().getName() + "#" + instanceId);
  private InMemoryDispatchChannel channel = InMemoryDispatchChannel.getInstance();
  private InMemoryUnicastAddress  address = new InMemoryUnicastAddress();
  private EventConsumer           consumer;

  public InMemoryUnicastDispatcher() {
  }
  
  @Override
  public void initialize(DispatcherContext context) {
    this.consumer = context.getConsumer();
  }
  
  @Override
  public void start() {
    Assertions.illegalState(consumer == null, "EventConsumer not set");
    channel.registerDispatcher(this);
  }

  @Override
  public void close() {
    channel.unregisterDispatcher(this);
  }

  EventConsumer getConsumer() {
    Assertions.illegalState(consumer == null, "EventConsumer not set");
    return consumer;
  }

  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    return address;
  }

  @Override
  public boolean dispatch(ServerAddress addr, String type, Object data) throws IOException {
    RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(addr);
    doSend((InMemoryUnicastAddress) addr, evt, false);
    return true;
  }

  @Override
  public RespList send(List<ServerAddress> addresses, String type, Object data, TimeValue timeout) throws IOException {
    RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(getAddress());

    InMemoryUnicastAddress current;
    RespList resps = new RespList(addresses.size());
    Response resp;

    for (int i = 0; i < addresses.size(); i++) {
      current = (InMemoryUnicastAddress) addresses.get(i);
      resp = (Response) doSend(current, evt, true);
      resps.addResponse(resp);
    }
    return resps;
  }
  
  @Override
  public RespList send(ServerAddress[] addresses, String type, Object[] data, TimeValue timeout)
      throws IOException, InterruptedException {
    
    RespList resps = new RespList(addresses.length);

    for (int i = 0; i < addresses.length; i++) {
      RemoteEvent evt = new RemoteEvent(null, type, data[i]).setNode(consumer.getNode()).setSync();
      Response resp = (Response) doSend((InMemoryUnicastAddress) addresses[i], evt, true);
      resps.addResponse(resp);
    }
    return resps;
  }

  @Override
  public Response send(ServerAddress addr, String type, Object data, TimeValue timeout) throws IOException {
    RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(addr);
    return (Response) doSend((InMemoryUnicastAddress) addr, evt, true);
  }

  private Object doSend(InMemoryUnicastAddress destination, RemoteEvent toSend, boolean synchro) {

    if (synchro) {
      try {
        log.debug("Sending sync to %s, event type: %s", destination, toSend.getType());
        return channel.sendSync(destination, toSend);
      } catch (InterruptedException e) {
        throw new IllegalStateException("Thread interrupted while waiting for sync response");
      }
    } else {
      log.debug("Sending async to %s, event type: %s", destination, toSend.getType());
      channel.sendASync(destination, toSend);
      return null;
    }
  }

  // --------------------------------------------------------------------------

  public static class InMemoryUnicastAddress implements ServerAddress {

    static final long serialVersionUID = 1L;

    public static final String TRANSPORT = "mem/unicast";

    private String node = UUID.randomUUID().toString();

    @Override
    public String getTransportType() {
      return TRANSPORT;
    }

    @Override
    public int hashCode() {
      return node.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof InMemoryUnicastAddress) {
        InMemoryUnicastAddress other = (InMemoryUnicastAddress) obj;
        return other.node.equals(node);
      }
      return false;
    }

    public String toString() {
      return String.format("[%s]", node);
    }
  }

}
