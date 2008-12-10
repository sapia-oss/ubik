package org.sapia.ubik.mcast;

import org.sapia.ubik.mcast.server.UDPServer;
import org.sapia.ubik.net.ServerAddress;

import java.io.*;

import java.net.*;

import java.util.List;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.util.Localhost;


/**
 * Implements the <code>UnicastDispatcher</code> interface over UDP.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class UDPUnicastDispatcher extends UDPServer implements UnicastDispatcher {
  static final int              DEFAULT_BUFSZ    = 5000;
  private EventConsumer         _consumer;
  private int                   _bufsize         = DEFAULT_BUFSZ;
  private int                   _responseTimeout = 10000;
  private String                _domain;
  private String                _node;
  private SocketTimeoutListener _listener;
  private ServerAddress         _addr;

  /**
   * Constructor for UnicastDispatcherImpl.
   */
  public UDPUnicastDispatcher(int soTimeout, EventConsumer consumer)
    throws SocketException {
    super(consumer.getNode() + "Unicast@" +
      consumer.getDomainName().toString(), soTimeout);
    _consumer   = consumer;
    _domain     = consumer.getDomainName().toString();
    _node       = consumer.getNode();
  }

  /**
   * Constructor for UnicastDispatcherImpl.
   */
  public UDPUnicastDispatcher(int soTimeout, int port, EventConsumer consumer)
    throws SocketException {
    super(consumer.getNode() + "@" + consumer.getDomainName().toString(),
      soTimeout, port);
    _consumer   = consumer;
    _domain     = consumer.getDomainName().toString();
    _node       = consumer.getNode();
  }

  /**
   * @see UnicastDispatcher#setBufsize(int)
   */
  public void setBufsize(int size) {
    super.setBufsize(size);
    _bufsize = size;
  }

  /**
   * Allows to set a listener that will be notified when the internal
   * UDP socket used by this instance reaches a timeout.
   */
  public void setSoTimeoutListener(SocketTimeoutListener listener) {
    _listener = listener;
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#start()
   */
  public void start() {
    super.start();

    try {
      InetAddress addr = Localhost.getLocalAddress();
      if(Log.isDebug()){
        Log.debug(getClass(), "Local address: " + addr.getHostAddress());
      }
      _addr = new InetServerAddress(addr, getPort());
    } catch (UnknownHostException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#close()
   */
  public void close() {
    if (super._sock != null) {
      super._sock.close();
    }
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#dispatch(ServerAddress, String, Object)
   */
  public void dispatch(ServerAddress addr, String type, Object data)
    throws IOException {
    DatagramSocket sock = new DatagramSocket();

    sock.setSoTimeout(_responseTimeout);

    try {
      RemoteEvent       evt  = new RemoteEvent(null, type, data).setNode(_node);
      InetServerAddress inet = (InetServerAddress) addr;

      if(Log.isDebug()){
        Log.debug(getClass(), "dispatch() : " + addr + ", type: " + type + ", data: " + data);
      }

      doSend(inet.getInetAddress(), inet.getPort(), sock,
        Util.toBytes(evt, bufSize()), false, type);
    } catch (TimeoutException e) {
      // will not occur - see doSend();
    } finally {
      try{
        sock.close();
      }catch(RuntimeException e){}
    }
  }

  /**
   * @see UnicastDispatcher#send(ServerAddress, String, Object)
   */
  public Response send(ServerAddress addr, String type, Object data)
    throws IOException {
    DatagramSocket sock = new DatagramSocket();

    sock.setSoTimeout(_responseTimeout);

    RemoteEvent       evt = new RemoteEvent(null, type, data).setNode(_node)
                                                             .setSync();
    InetServerAddress inet = (InetServerAddress) addr;

    try {
      return (Response) doSend(inet.getInetAddress(), inet.getPort(), sock,
        Util.toBytes(evt, bufSize()), true, type);
    } catch (TimeoutException e) {
      return new Response(evt.getId(), null).setStatusSuspect();
    } finally {
      try{
        sock.close();
      }catch(RuntimeException e){}
    }
  }

  /**
   * @see UnicastDispatcher#send(java.util.List, String, Object)
   */
  public RespList send(List addresses, String type, Object data)
    throws IOException {
    DatagramSocket sock = new DatagramSocket();

    sock.setSoTimeout(_responseTimeout);

    try{
      RemoteEvent       evt = new RemoteEvent(null, type, data).setNode(_node)
                                                               .setSync();
      byte[]            bytes   = Util.toBytes(evt, bufSize());
      InetServerAddress current;
      RespList          resps   = new RespList(addresses.size());
      Response          resp;

      for (int i = 0; i < addresses.size(); i++) {
        current = (InetServerAddress) addresses.get(i);

        try {
          resp = (Response) (Response) doSend(current.getInetAddress(),
              current.getPort(), sock, bytes, true, type);
        } catch (TimeoutException e) {
          resp = new Response(evt.getId(), null).setStatusSuspect();
        }

        if (!resp.isNone()) {
          resps.addResponse(resp);
        }
        
      } 
      
      return resps;        
    }
    finally {
      try{
        sock.close();
      }catch(RuntimeException e){}
    }    
  }

  /**
   * @see org.sapia.ubik.mcast.UnicastDispatcher#getAddress()
   */
  public ServerAddress getAddress() throws IllegalStateException {
    if (_addr == null) {
      throw new IllegalStateException(
        "The address of this instance is not yet available");
    }

    return _addr;
  }

  /**
   * @see org.sapia.ubik.mcast.server.UDPServer#handleSoTimeout()
   */
  protected void handleSoTimeout() {
    if (_listener != null) {
      _listener.handleSoTimeout();
    }
  }

  /**
   * @see org.sapia.ubik.mcast.server.UDPServer#handlePacketSizeToShort(DatagramPacket)
   */
  protected void handlePacketSizeToShort(DatagramPacket pack) {
    String msg = "Buffer size to short; set to: " + bufSize() +
      ". This size is not enough to receive some incoming packets";

    System.err.println(msg);
  }

  /**
   * @see org.sapia.ubik.mcast.server.UDPServer#bufSize()
   */
  protected int bufSize() {
    return super.bufSize();
  }

  /**
   * @see org.sapia.ubik.mcast.server.UDPServer#handle(DatagramPacket, DatagramSocket)
   */
  protected void handle(DatagramPacket pack, DatagramSocket sock) {
    try {
      Object o = Util.fromDatagram(pack);

      if (o instanceof RemoteEvent) {
        RemoteEvent evt = (RemoteEvent) o;

        if (evt.isSync()) {
          InetAddress addr = pack.getAddress();
          int         port = pack.getPort();

          if (_consumer.hasSyncListener(evt.getType())) {
            Object response = _consumer.onSyncEvent(evt);

            try {
              doSend(addr, port, sock,
                Util.toBytes(new Response(evt.getId(), response), bufSize()),
                false, evt.getType());
            } catch (TimeoutException e) {
              // will not occur - see doSend()
            }
          } else {
            try {
              doSend(addr, port, sock,
                Util.toBytes(new Response(evt.getId(), null).setNone(),
                  bufSize()), false, evt.getType());
            } catch (TimeoutException e) {
              // will not occur - see doSend()
            }
          }
        } else {
          _consumer.onAsyncEvent(evt);
        }
      } else {
        System.out.println("Object not a remote event: " +
          o.getClass().getName() + "; " + o);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private Object doSend(InetAddress addr, int port, DatagramSocket sock,
    byte[] bytes, boolean synchro, String type) throws IOException, TimeoutException {
    if (bytes.length > _bufsize) {
      throw new IOException(
        "Size of data larger than buffer size; increase this instance's buffer size through the setBufsize() method");
    }

    if(Log.isDebug()){
      Log.debug(getClass(), "doSend() : " + addr + ", event type: " + type);
    }
    DatagramPacket pack = new DatagramPacket(bytes, 0, bytes.length, addr, port);

    sock.send(pack);

    if (synchro) {
      bytes   = new byte[bufSize()];
      pack    = new DatagramPacket(bytes, bytes.length);

      try {
        sock.receive(pack);
      } catch (InterruptedIOException e) {
        throw new TimeoutException();
      }

      try {
        return Util.fromDatagram(pack);
      } catch (ClassNotFoundException e) {
        throw new IOException(e.getClass().getName() + ": " + e.getMessage());
      }
    } else {
      return null;
    }
  }
}
