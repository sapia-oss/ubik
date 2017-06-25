package org.sapia.ubik.mcast.nats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ConnectionMonitor;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;

import io.nats.client.Message;
import io.nats.client.MessageHandler;

/**
 * Implements a {@link BroadcastDispatcher} over Nats.
 *
 * @author yduchesne
 *
 */
public class NatsBroadcastDispatcher implements BroadcastDispatcher {

  public enum ConnectionState {
    DOWN, UP
  }

  private static final String BASE_NATS_TOPIC = "ubik-broadcast";
  public static final int DEFAULT_BUFSZ              = 1024;
  public static final int DEFAULT_HANDLER_COUNT      = 5;
  public static final int DEFAULT_HANDLER_QUEUE_SIZE = 1000;

  private Category      log = Log.createCategory(getClass());
  private EventConsumer consumer;
  private NatsConnector connector;
  private NatsAddress    address;
  private int           bufsize;

  private ConnectionStateListenerList listeners = new ConnectionStateListenerList();
  private volatile ConnectionState    state     = ConnectionState.UP;
  private ConnectionMonitor           monitor;
  private ExecutorService             workers;
  private ExecutorService             senders;
  private String                      fullTopic;

  public NatsBroadcastDispatcher() {
  }
  
  @Override
  public void initialize(DispatcherContext context) {
    consumer       = context.getConsumer();
    workers        = context.getWorkerThreads();
    senders        = context.getIoOutboundThreads();
    bufsize        = context.getConf().getIntProperty(Consts.MCAST_BUFSIZE_KEY, NatsBroadcastDispatcher.DEFAULT_BUFSZ);
    String natsUrl = context.getConf().getNotNullProperty(Consts.BROADCAST_NATS_URL);
    connector      = new NatsConnector(natsUrl);
    address        = new NatsAddress(natsUrl);
    fullTopic      = topic(consumer.getDomainName().toString());
  }
  
  @Override
  public MulticastAddress getMulticastAddressFrom(Conf props) {
    String natsUrl = props.getProperty(Consts.BROADCAST_NATS_URL);
    return new NatsAddress(natsUrl);
  }
  
  public void setBufsize(int size) {
    this.bufsize = size;
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
  public void dispatch(ServerAddress unicastAddr, boolean alldomains, String evtType, Object data) throws IOException {
    if (state == ConnectionState.UP) {
      RemoteEvent evt;

      try {
        if (alldomains) {
          evt = new RemoteEvent(null, evtType, data).setNode(consumer.getNode());
          evt.setUnicastAddress(unicastAddr);
          senders.submit(new Runnable() {
            @Override
            public void run() {
              try {
                connector.getConnection().publish(BASE_NATS_TOPIC, createPayload(evt));
              } catch (IOException e) {
                watchConnection();
              }
            }
          });
          
        } else {
          evt = new RemoteEvent(fullTopic, evtType, data).setNode(consumer.getNode());
          evt.setUnicastAddress(unicastAddr);
          
          senders.submit(new Runnable() {
            @Override
            public void run() {
              try {
                connector.getConnection().publish(fullTopic, createPayload(evt));
              } catch (IOException e) {
                watchConnection();
              }
            }
          });
        }
      } catch (IOException e) {
        watchConnection();
      }
    } else {
      log.debug("Connection currently down: cannot dispatch");
    }
  }

  @Override
  public void dispatch(ServerAddress unicastAddr, String domain, String evtType, Object data) throws IOException {
    if (state == ConnectionState.UP) {
      log.debug("Sending event bytes for: %s", evtType);
      RemoteEvent evt = new RemoteEvent(domain, evtType, data).setNode(consumer.getNode());
      evt.setUnicastAddress(unicastAddr);
      try {
        connector.getConnection().publish(topic(domain), createPayload(evt));
      } catch (IOException e) {
        watchConnection();
      }
    } else {
      log.debug("Connection currently down: cannot dispatch");
    }
  }

  @Override
  public void start() {
    Assertions.illegalState(consumer == null, "EventConsumer not set");
    try {
      doConnect();
      listeners.onConnected();
    } catch (IOException e) {
      log.warning("I/O error caught: cannot connect to Avis router. Will attempt reconnecting", e);
      watchConnection();
    }
  }

  @Override
  public void close() {
    connector.disconnect();
    if (monitor != null) {
      monitor.stop();
    }
    workers.shutdown();
    senders.shutdown();
  }

  @Override
  public void addConnectionStateListener(ConnectionStateListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeConnectionStateListener(ConnectionStateListener listener) {
    listeners.remove(listener);
  }

  private synchronized void doConnect() throws IOException {
    connector.connect();
    
    MessageHandler handler = createMessageHandler();
    connector.getConnection().subscribe(fullTopic, handler);
    connector.getConnection().subscribe(BASE_NATS_TOPIC, handler);
 
    state = ConnectionState.UP;
  }

  private synchronized void watchConnection() {
    if (state == ConnectionState.UP) {
      listeners.onDisconnected();
      monitor = new ConnectionMonitor("NatsBroadcastDispatcher::" + consumer.getDomainName() + "::" + getNode(), 
        new ConnectionMonitor.ConnectionFacade() {
          @Override
          public void tryConnection() throws IOException {
            doConnect();
            log.debug("Reconnected to Nats daemon");
            monitor = null;
          }
        },
        this.listeners,
        Conf.getSystemProperties().getTimeProperty(
            Consts.MCAST_BROADCAST_MONITOR_INTERVAL,
            Defaults.DEFAULT_BROADCAST_MONITOR_INTERVAL
        ).getValueInMillis()
      );
      state = ConnectionState.DOWN;
    }
  }

  private byte[] createPayload(RemoteEvent evt) throws IOException {
    return McastUtil.toBytes(evt, bufsize);
  }
  
  private String topic(String domain) {
    return BASE_NATS_TOPIC + "-" + domain;
  }
  
  private MessageHandler createMessageHandler() {
    return new MessageHandler() {
      @Override
      public void onMessage(Message msg) {
        try {
          Object data = McastUtil.fromBytes(msg.getData());
          if (data instanceof RemoteEvent) {
            RemoteEvent evt = (RemoteEvent) data;
            log.debug("Received message %s from node %s", evt.getType(), evt.getNode());
            workers.submit(new Runnable() {
              @Override
              public void run() {
                consumer.onAsyncEvent((RemoteEvent) data);
              }
            });
          }
        } catch (Exception e) {
          log.error("Error handling notification %s" + msg.getSubject());
        }
      }
    };
  }

  // --------------------------------------------------------------------------

  public static class NatsAddress implements MulticastAddress {

    static final long serialVersionUID = 1L;

    public static final String TRANSPORT = "nats/broadcast";

    private String uuid = UUID.randomUUID().toString();
    private String natUrl;

    public NatsAddress(String natUrl) {
      this.natUrl = natUrl;
    }

    @Override
    public String getTransportType() {
      return TRANSPORT;
    }

    public String getUUID() {
      return uuid;
    }

    @Override
    public int hashCode() {
      return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof NatsAddress) {
        NatsAddress other = (NatsAddress) obj;
        return other.uuid.equals(uuid);
      }
      return false;
    }

    @Override
    public Map<String, String> toParameters() {
      Map<String, String> params = new HashMap<String, String>();
      params.put(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_NATS);
      params.put(Consts.BROADCAST_NATS_URL, natUrl);
      return params;
    }
  }

}
