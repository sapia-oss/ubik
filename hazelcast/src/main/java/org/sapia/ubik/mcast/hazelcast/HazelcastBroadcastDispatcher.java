package org.sapia.ubik.mcast.hazelcast;

import java.io.IOException;

import org.sapia.ubik.ioc.BeanLookup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Conf;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * Implements the {@link BroadcastDispatcher} interface on top of a Hazelcast {@link ITopic}.
 * <p>
 * An instance of this class accesses the {@link HazelcastInstance} that must have previously
 * created and set on the {@link Singleton} class, or have been made available through a {@link BeanLookup}
 * registered with the {@link Hub} class (the lookup mechanism is used first).
 * <p>
 * Setting that instance is the responsibility of applications, and should be done prior to
 * using Ubik in code.
 *
 * @author yduchesne
 *
 */
public class HazelcastBroadcastDispatcher implements BroadcastDispatcher {

  private static Category log = Log.createCategory(HazelcastBroadcastDispatcher.class);
  
  private EventConsumer               consumer;
  private ITopic<MessagePayload>      topic;
  private HazelcastMulticastAddress   address;
  private ConnectionStateListenerList listeners = new ConnectionStateListenerList();

  public HazelcastBroadcastDispatcher() {
  }
  
  @Override
  public void initialize(DispatcherContext context) {
    this.consumer    = context.getConsumer();
    String topicName = context.getConf().getNotNullProperty(Consts.BROADCAST_HAZELCAST_TOPIC);
    HazelcastInstance instance = Hub.getBean(HazelcastInstance.class);
    if (instance == null) {
       instance = Singleton.get();
    }
    this.topic   = instance.getTopic(topicName);
    this.address = new HazelcastMulticastAddress(topic.getName());
  }
  
  @Override
  public MulticastAddress getMulticastAddressFrom(Conf props) {
    return new HazelcastMulticastAddress(props.getNotNullProperty(Consts.BROADCAST_HAZELCAST_TOPIC));
  }
 
  @Override
  public void start() {
    topic.addMessageListener(new MessageListener<MessagePayload>() {

      @Override
      public void onMessage(Message<MessagePayload> msg) {
        MessagePayload payload = msg.getMessageObject();
        try {
          consumer.onAsyncEvent(payload.getRemoteEvent());
        } catch (Exception e) {
          log.error("Error processing incoming message payload", e);
        }
      }
    });
    listeners.notifyConnected();
  }

  @Override
  public void close() {
    try {
      topic.destroy();
    } catch (RuntimeException e) {
      // noop
    }
  }

  @Override
  public void dispatch(ServerAddress unicastAddr, boolean alldomains,
      String evtType, Object data) throws IOException {
    RemoteEvent evt;

    if (alldomains) {
      evt = new RemoteEvent(null, evtType, data).setNode(consumer.getNode());
    } else {
      evt = new RemoteEvent(consumer.getDomainName().toString(), evtType, data).setNode(consumer.getNode());
    }
    evt.setUnicastAddress(unicastAddr);

    topic.publish(new MessagePayload(evt));
  }

  @Override
  public void dispatch(ServerAddress unicastAddr, String domain, String evtType,
      Object data) throws IOException {
    RemoteEvent evt;

    log.debug("Sending event bytes for: %s", evtType);
    evt = new RemoteEvent(domain, evtType, data).setNode(consumer.getNode());
    evt.setUnicastAddress(unicastAddr);

    topic.publish(new MessagePayload(evt));
  }

  @Override
  public MulticastAddress getMulticastAddress() {
    return address;
  }

  @Override
  public String getNode() {
    return consumer.getNode();
  }

  @Override
  public void removeConnectionStateListener(ConnectionStateListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void addConnectionStateListener(ConnectionStateListener listener) {
    listeners.add(listener);
  }

}
