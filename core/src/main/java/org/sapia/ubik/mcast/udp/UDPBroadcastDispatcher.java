package org.sapia.ubik.mcast.udp;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.server.MulticastServer;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Assertions;

/**
 * Dispatches objects using a multicast channel.
 * 
 * @author Yanick Duchesne
 */
public class UDPBroadcastDispatcher implements BroadcastDispatcher {

  private static Category log = Log.createCategory(UDPBroadcastDispatcher.class);
  private String node;
  private String domain;
  private BroadcastServer server;
  private int bufsz = Defaults.DEFAULT_UDP_PACKET_SIZE;
  private UDPMulticastAddress address;

  public UDPBroadcastDispatcher(EventConsumer cons, String mcastHost, int mcastPort, int ttl) throws IOException {
    server = new BroadcastServer(cons, mcastHost, mcastPort, ttl);
    server.setBufsize(bufsz);
    node = cons.getNode();
    domain = cons.getDomainName().toString();
    address = new UDPMulticastAddress(mcastHost, mcastPort);
  }

  /**
   * Sets this instance buffer size. This size is used to create the byte arrays
   * that store the data of incoming UDP datagrams.
   * <p>
   * The size should be large enough to hold the data of incoming datagrams.
   * 
   * @param size
   *          a buffer size - corresponding to the size of expected UDP
   *          datagrams.
   */
  public void setBufsize(int size) {
    bufsz = size;
    server.setBufsize(size);
  }

  /**
   * Returns the node identifier of this instance.
   * 
   * @return this instance's node identifier.
   */
  public String getNode() {
    return node;
  }

  /**
   * Starts this instance.
   */
  public void start() {
    Assertions.illegalState(server == null, "Instance was closed; cannot be started again");
    server.start();
  }

  /**
   * Closes this instance, which should thereafter not be used.
   */
  public synchronized void close() {
    if (server != null) {
      server.close();
      server = null;
    }
  }

  /**
   * @see BroadcastDispatcher#dispatch(ServerAddress, boolean, String, Object)
   */
  public void dispatch(ServerAddress unicastAddr, boolean alldomains, String evtType, Object data) throws IOException {
    RemoteEvent evt;

    if (alldomains) {
      evt = new RemoteEvent(null, evtType, data).setNode(node);
    } else {
      evt = new RemoteEvent(domain, evtType, data).setNode(node);
    }
    evt.setUnicastAddress(unicastAddr);

    if (server != null) {
      server.send(McastUtil.toBytes(evt, bufsz));
    }
  }

  /**
   * @see BroadcastDispatcher#dispatch(ServerAddress, String, String, Object)
   */
  public void dispatch(ServerAddress unicastAddr, String domain, String evtType, Object data) throws IOException {
    RemoteEvent evt;

    log.debug("Sending event bytes for: %s", evtType);
    evt = new RemoteEvent(domain, evtType, data).setNode(node);
    evt.setUnicastAddress(unicastAddr);

    if (server != null) {
      server.send(McastUtil.toBytes(evt, bufsz));
    }
  }

  /**
   * @see BroadcastDispatcher#getMulticastAddress()
   */
  @Override
  public MulticastAddress getMulticastAddress() {
    return address;
  }

  /*
   * //////////////////////////////////////////////////////////////// INNER
   * CLASSES ////////////////////////////////////////////////////////////////
   */

  public static class UDPMulticastAddress implements MulticastAddress {

    static final long serialVersionUID = 1L;

    public static final String TRANSPORT = "upd/broadcast";

    private String mcastAddress;
    private int mcastPort;

    public UDPMulticastAddress(String mcastAddress, int mcastPort) {
      this.mcastAddress = mcastAddress;
      this.mcastPort = mcastPort;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof UDPMulticastAddress) {
        UDPMulticastAddress other = (UDPMulticastAddress) obj;
        return other.mcastAddress.equals(mcastAddress) && other.mcastPort == mcastPort;
      }
      return false;
    }

    @Override
    public String getTransportType() {
      return TRANSPORT;
    }

    @Override
    public Map<String, String> toParameters() {
      Map<String, String> params = new HashMap<String, String>();
      params.put(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UDP);
      params.put(Consts.MCAST_ADDR_KEY, mcastAddress);
      params.put(Consts.MCAST_PORT_KEY, Integer.toString(mcastPort));
      return params;
    }

  }

  /**
   * @author Yanick Duchesne
   */
  private static class BroadcastServer extends MulticastServer {

    EventConsumer consumer;

    private BroadcastServer(EventConsumer consumer, String mcastAddress, int mcastPort, int ttl) throws IOException {
      super("mcast.BroadcastServer", mcastAddress, mcastPort, ttl);
      this.consumer = consumer;
    }

    protected void handle(DatagramPacket pack, MulticastSocket sock) {
      try {
        consumer.onAsyncEvent((RemoteEvent) McastUtil.fromDatagram(pack));
      } catch (EOFException e) {
        log.warning("Could not deserialize remote event, packet size may be too short " + this.bufSize());

      } catch (Exception e) {
        log.error("Could not deserialize remote event", e);
      }
    }
  }
}
