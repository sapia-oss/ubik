package org.sapia.ubik.mcast.avis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.avis.client.Elvin;
import org.avis.client.GeneralNotificationEvent;
import org.avis.client.GeneralNotificationListener;
import org.avis.client.Notification;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Base64;

/**
 * Implements a {@link BroadcastDispatcher} on top of the Avis group
 * communication framework.
 * 
 * @author yduchesne
 * 
 */
public class AvisBroadcastDispatcher implements BroadcastDispatcher {

  private static final String NOTIF_TYPE = "ubik.broadcast.avis";
  private static final String ANY_DOMAIN = "*";
  public static final int DEFAULT_BUFSZ = 1024;

  private Category log = Log.createCategory(getClass());
  private EventConsumer consumer;
  private String domain;
  private Elvin elvinConnection;
  private AvisAddress address;
  private int bufsize;

  public AvisBroadcastDispatcher(EventConsumer consumer, String avisUrl) throws IOException {
    this.consumer = consumer;
    domain = consumer.getDomainName().toString();
    elvinConnection = new Elvin(avisUrl);
    address = new AvisAddress(avisUrl);
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

    RemoteEvent evt;

    if (alldomains) {
      evt = new RemoteEvent(null, evtType, data).setNode(consumer.getNode());
      evt.setUnicastAddress(unicastAddr);
      elvinConnection.send(createNotification(evt, ANY_DOMAIN));

    } else {
      evt = new RemoteEvent(domain, evtType, data).setNode(consumer.getNode());
      evt.setUnicastAddress(unicastAddr);
      elvinConnection.send(createNotification(evt, domain));

    }
  }

  @Override
  public void dispatch(ServerAddress unicastAddr, String domain, String evtType, Object data) throws IOException {
    log.debug("Sending event bytes for: %s", evtType);
    RemoteEvent evt = new RemoteEvent(domain, evtType, data).setNode(consumer.getNode());
    evt.setUnicastAddress(unicastAddr);
    elvinConnection.send(createNotification(evt, domain));
  }

  @Override
  public void start() {
    try {
      elvinConnection.subscribe(String.format("(begins-with(Domain, \"%s\") || Domain == '*') && Type == '%s'", consumer.getDomainName().get(0),
          NOTIF_TYPE));
      elvinConnection.addNotificationListener(new GeneralNotificationListener() {
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
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not start", e);
    }
  }

  @Override
  public void close() {
    elvinConnection.close();
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
