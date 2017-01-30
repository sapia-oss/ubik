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
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ConnectionStateListenerList;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;

/**
 * Dispatches objects using a multicast channel.
 *
 * @author Yanick Duchesne
 */
public class UDPBroadcastDispatcher implements BroadcastDispatcher {

  private static final int DEFAULT_MCAST_UDP_HANDLER_COUNT = 3;
  
  private static Category     log       = Log.createCategory(UDPBroadcastDispatcher.class);
  private EventConsumer       consumer;
  private BroadcastServer     server;
  private int                 bufsz     = Defaults.DEFAULT_UDP_PACKET_SIZE;
  private UDPMulticastAddress address;
  private ConnectionStateListenerList stateListeners = new ConnectionStateListenerList();

  public UDPBroadcastDispatcher() {
  }
  
  @Override
  public void initialize(EventConsumer consumer, Conf config) {
    this.consumer = consumer;
    String mcastHost = config.getProperty(Consts.MCAST_ADDR_KEY, Consts.DEFAULT_MCAST_ADDR);
    int    mcastPort = config.getIntProperty(Consts.MCAST_PORT_KEY, Consts.DEFAULT_MCAST_PORT);
    
    try {
      server   = new BroadcastServer(
          consumer, 
          mcastHost, 
          mcastPort, 
          config.getIntProperty(Consts.MCAST_HANDLER_COUNT, DEFAULT_MCAST_UDP_HANDLER_COUNT),
          config.getIntProperty(Consts.MCAST_TTL, Defaults.DEFAULT_TTL)
      );
    } catch (IOException e) {
      throw new IllegalStateException("Could not create UDP server", e);
    }
    server.setBufsize(config.getIntProperty(Consts.MCAST_BUFSIZE_KEY, Defaults.DEFAULT_UDP_PACKET_SIZE));
    address  = new UDPMulticastAddress(mcastHost, mcastPort);
  }
  
  @Override
  public MulticastAddress getMulticastAddressFrom(Conf props) {
    return new UDPBroadcastDispatcher.UDPMulticastAddress(
        props.getProperty(Consts.MCAST_ADDR_KEY, Consts.DEFAULT_MCAST_ADDR),
        props.getIntProperty(Consts.MCAST_PORT_KEY, Consts.DEFAULT_MCAST_PORT)
    );
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
  @Override
  public String getNode() {
    return consumer.getNode();
  }

  /**
   * Starts this instance.
   */
  @Override
  public void start() {
    Assertions.illegalState(consumer == null, "Consumer not set");
    Assertions.illegalState(server == null, "Instance was closed; cannot be started again");
    stateListeners.onConnected();
    server.start();

  }

  /**
   * Closes this instance, which should thereafter not be used.
   */
  @Override
  public synchronized void close() {
    if (server != null) {
      server.close();
      server = null;
    }
  }

  /**
   * @see BroadcastDispatcher#dispatch(ServerAddress, boolean, String, Object)
   */
  @Override
  public void dispatch(ServerAddress unicastAddr, boolean alldomains, String evtType, Object data) throws IOException {
    RemoteEvent evt;

    if (alldomains) {
      evt = new RemoteEvent(null, evtType, data).setNode(consumer.getNode());
    } else {
      evt = new RemoteEvent(consumer.getDomainName().toString(), evtType, data).setNode(consumer.getNode());
    }
    evt.setUnicastAddress(unicastAddr);

    if (server != null) {
      server.send(McastUtil.toBytes(evt, bufsz));
    }
  }

  /**
   * @see BroadcastDispatcher#dispatch(ServerAddress, String, String, Object)
   */
  @Override
  public void dispatch(ServerAddress unicastAddr, String domain, String evtType, Object data) throws IOException {
    RemoteEvent evt;

    log.debug("Sending event bytes for: %s", evtType);
    evt = new RemoteEvent(domain, evtType, data).setNode(consumer.getNode());
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

  @Override
  public void addConnectionStateListener(ConnectionStateListener listener) {
    stateListeners.add(listener);
  }

  @Override
  public void removeConnectionStateListener(ConnectionStateListener listener) {
    stateListeners.remove(listener);
  }
  
  // ==========================================================================

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

    private BroadcastServer(EventConsumer consumer, String mcastAddress, int mcastPort, int threadCount, int ttl) throws IOException {
      super("mcast.BroadcastServer", mcastAddress, mcastPort, threadCount, ttl);
      this.consumer = consumer;
    }

    @Override
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
