package org.sapia.ubik.mcast;

import org.sapia.ubik.mcast.server.MulticastServer;

import java.io.*;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import org.sapia.ubik.rmi.server.Log;


/**
 * Dispatches objects using a multicast channel.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class BroadcastDispatcherImpl /*extends Thread*/
  implements BroadcastDispatcher {
  static final int        DEFAULT_BUFSZ = 5000;
  static final int        TTL       = 7;
  private boolean         _started;
  private String          _node;
  private String          _domain;
  private BroadcastServer _server;
  private EventConsumer   _consumer;
  private int             _bufsz    = DEFAULT_BUFSZ;

  public BroadcastDispatcherImpl(EventConsumer cons, String mcastHost,
    int mcastPort) throws IOException {
    _server = new BroadcastServer(cons, 15, mcastHost, mcastPort, TTL);
    _server.setBufsize(_bufsz);
    _consumer   = cons;
    _node       = cons.getNode();
    _domain     = cons.getDomainName().toString();
  }

  public BroadcastDispatcherImpl(String node, String domain, String mcastHost,
    int mcastPort, int ttl) throws IOException {
    this(new EventConsumer(node, domain), mcastHost, mcastPort);
  }

  public BroadcastDispatcherImpl(String domain, String mcastHost, int mcastPort)
    throws IOException {
    this(new EventConsumer(domain), mcastHost, mcastPort);
  }

  /**
   * Sets this instance buffer size. This size is used to create
   * the byte arrays that store the data of incoming UDP datagrams.
   * <p>
   * The size should be large enough to hold the data of incoming
   * datagrams.
   *
   * @param size a buffer size - corresponding to the size of expected
   * UDP datagrams.
   */
  public void setBufsize(int size) {
    _bufsz = size;
    _server.setBufsize(size);
  }

  /**
   * Returns the node identifier of this instance.
   *
   * @return this instance's node identifier.
   */
  public String getNode() {
    return _node;
  }

  /**
   * Closes this instance, which should thereafter not be used.
   */
  public synchronized void close() {
    if (_server != null) {
      _server.close();
      _server = null;
    }
  }

  /**
   * @see BroadcastDispatcher#dispatch(boolean, String, Object)
   */
  public void dispatch(boolean alldomains, String evtType, Object data)
    throws IOException {
    RemoteEvent evt;

    if (alldomains) {
      evt = new RemoteEvent(null, evtType, data).setNode(_node);
    } else {
      evt = new RemoteEvent(_domain, evtType, data).setNode(_node);
    }

    _server.send(Util.toBytes(evt, _bufsz));
  }

  /**
   * @see BroadcastDispatcher#dispatch(String, String, Object)
   */
  public void dispatch(String domain, String evtType, Object data)
    throws IOException {
    RemoteEvent evt;

    if(Log.isDebug()){
      Log.debug(getClass(), "Sending event bytes for: " + evtType);
    }
    evt = new RemoteEvent(domain, evtType, data).setNode(_node);
    _server.send(Util.toBytes(evt, _bufsz));
  }

  /**
   * Registers the given listener with the given event type.
   *
   * @param evtType the logical type of <code>RemoteEvents</code> to listen for.
   * @param listener the <code>AsyncEventListener</code> to register.
   */
  public synchronized void registerAsyncListener(String evtType,
    AsyncEventListener listener) {
    _consumer.registerAsyncListener(evtType, listener);
  }

  /**
   * Unregisters the given listener from this instance.
   *
   * @param listener the <code>AsyncEventListener</code> to unregister.
   */
  public synchronized void unregisterListener(AsyncEventListener listener) {
    _consumer.unregisterListener(listener);
  }

  /**
   * Starts this instance.
   */
  public void start() {
    _server.setDaemon(true);
    _server.start();
  }

  /**
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#getMulticastAddress()
   */
  public String getMulticastAddress() {
    return _server.getMulticastAddress();
  }

  /**
   * @see org.sapia.ubik.mcast.BroadcastDispatcher#getMulticastPort()
   */
  public int getMulticastPort() {
    return _server.getMulticastPort();
  }

  /*////////////////////////////////////////////////////////////////
                             INNER CLASSES
  ////////////////////////////////////////////////////////////////*/

  /**
   * @author Yanick Duchesne
   *
   * <dl>
   * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
   * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
   *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
   * </dl>
   */
  static class BroadcastServer extends MulticastServer {
    EventConsumer _consumer;

    public BroadcastServer(EventConsumer consumer, int soTimeout,
      String mcastAddress, int mcastPort, int ttl) throws IOException {
      super("ubik.mcast.BroadcastServer", soTimeout, mcastAddress, mcastPort,
        ttl);
      _consumer = consumer;
    }

    protected void handle(DatagramPacket pack, MulticastSocket sock) {
      try {
        _consumer.onAsyncEvent((RemoteEvent) Util.fromDatagram(pack));
      } catch (Exception e) {
        Log.error(getClass(), "Could not deserialize remote event", e);
      }
    }

    protected void handlePacketSizeToShort(DatagramPacket pack) {
      System.err.println("Packet size to short: " + pack.getLength() +
        " - increase buffer size to correct.");
    }

    protected void handleSoTimeout() {
    }
  }
}
