package org.sapia.ubik.mcast.avis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.avis.client.GeneralNotificationEvent;
import org.sapia.ubik.mcast.avis.client.GeneralNotificationListener;
import org.sapia.ubik.mcast.avis.client.Notification;
import org.sapia.ubik.net.ConnectionMonitor;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Base64;
import org.sapia.ubik.util.Conf;

/**
 * Implements a {@link BroadcastDispatcher} on top of the Avis group
 * communication framework.
 *
 * @author yduchesne
 *
 */
public class AvisBroadcastDispatcher implements BroadcastDispatcher {

  public enum ConnectionState {
    DOWN, UP
  }

  private static final String NOTIF_TYPE = "ubik.broadcast.avis";
  private static final String ANY_DOMAIN = "*";
  public static final int DEFAULT_BUFSZ = 1024;

  private Category      log = Log.createCategory(getClass());
  private EventConsumer consumer;
  private AvisConnector connector;
  private AvisAddress   address;
  private int           bufsize;
  private ConnectionStateListenerList listeners = new ConnectionStateListenerList();
  private volatile ConnectionState    state     = ConnectionState.UP;
  private ConnectionMonitor           monitor;

  public AvisBroadcastDispatcher() {
  }
  
  @Override
  public void initialize(EventConsumer consumer, Conf config) {
    this.consumer = consumer;
    bufsize = config.getIntProperty(Consts.MCAST_BUFSIZE_KEY, AvisBroadcastDispatcher.DEFAULT_BUFSZ);
    String avisUrl = config.getNotNullProperty(Consts.BROADCAST_AVIS_URL);
    connector     = new AvisConnector(avisUrl);
    address       = new AvisAddress(avisUrl);
  }
  
  @Override
  public MulticastAddress getMulticastAddressFrom(Conf props) {
    String avisUrl = props.getProperty(Consts.BROADCAST_AVIS_URL);
    return new AvisBroadcastDispatcher.AvisAddress(avisUrl);
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
          connector.getConnection().send(createNotification(evt, ANY_DOMAIN));

        } else {
          evt = new RemoteEvent(consumer.getDomainName().toString(), evtType, data).setNode(consumer.getNode());
          evt.setUnicastAddress(unicastAddr);
          connector.getConnection().send(createNotification(evt, consumer.getDomainName().toString()));

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
        connector.getConnection().send(createNotification(evt, domain));
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
    connector.getConnection().subscribe(String.format("(begins-with(Domain, \"%s\") || Domain == '*') && Type == '%s'", consumer.getDomainName().get(0),
        NOTIF_TYPE));
    connector.getConnection().addNotificationListener(new GeneralNotificationListener() {
      @Override
      public void notificationReceived(GeneralNotificationEvent event) {
        try {
          log.debug("Received GeneralNotificationEvent: %s", event.notification);
          byte[] bytes = Base64.decode(((String) event.notification.get("Payload")).getBytes());
          Object data = McastUtil.fromBytes(bytes);
          if (data instanceof RemoteEvent) {
            consumer.onAsyncEvent((RemoteEvent) data);
          }
        } catch (Exception e) {
          log.error("Could not deserialize data", e);
        }
      }
    });

    state = ConnectionState.UP;
  }

  private synchronized void watchConnection() {
    if (state == ConnectionState.UP) {
      listeners.onDisconnected();
      monitor = new ConnectionMonitor("AvisBroadcastDispatcher::" + consumer.getDomainName() + "::" + getNode(), 
        new ConnectionMonitor.ConnectionFacade() {
          @Override
          public void tryConnection() throws IOException {
            doConnect();
            log.debug("Reconnected to Avis router");
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

  private Notification createNotification(RemoteEvent evt, String domain) throws IOException {
    Notification notification = new Notification();
    notification.set("Type", NOTIF_TYPE);
    notification.set("Payload", Base64.encodeBytes(McastUtil.toBytes(evt, bufsize)));
    notification.set("Domain", domain);
    notification.set("Node", consumer.getNode());
    return notification;
  }

  // --------------------------------------------------------------------------

  public static class AvisAddress implements MulticastAddress {

    static final long serialVersionUID = 1L;

    public static final String TRANSPORT = "avis/broadcast";

    private String uuid = UUID.randomUUID().toString();
    private String avisUrl;

    public AvisAddress(String avisUrl) {
      this.avisUrl = avisUrl;
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
      if (obj instanceof AvisAddress) {
        AvisAddress other = (AvisAddress) obj;
        return other.uuid.equals(uuid);
      }
      return false;
    }

    @Override
    public Map<String, String> toParameters() {
      Map<String, String> params = new HashMap<String, String>();
      params.put(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AVIS);
      params.put(Consts.BROADCAST_AVIS_URL, avisUrl);
      return params;
    }
  }

}
