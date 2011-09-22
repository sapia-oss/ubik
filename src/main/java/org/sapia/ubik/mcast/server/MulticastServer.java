package org.sapia.ubik.mcast.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.sapia.ubik.mcast.ByteArrayPool;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.util.Localhost;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class MulticastServer extends Thread {
  static final int          DEFAULT_BUFSZ = 20000;
  static final int          TTL       = 7;
  protected MulticastSocket _sock;
  private InetAddress       _group;
  private String            _groupStr;
  private int               _port;
  private ByteArrayPool     _bytes = new ByteArrayPool(DEFAULT_BUFSZ);

  /**
   * Constructor for UDPServer.
   */
  public MulticastServer(String name, int soTimeout, String mcastAddress,
    int mcastPort, int ttl) throws IOException {
    super(name);
    super.setDaemon(true);
    _group      = InetAddress.getByName(mcastAddress);
    _groupStr   = mcastAddress;
    _sock       = new MulticastSocket(mcastPort);
    if (Localhost.isIpPatternDefined()) {
        _sock.setNetworkInterface(NetworkInterface.getByInetAddress(Localhost.getAnyLocalAddress()));
    }
    _sock.setSoTimeout(soTimeout);
    _sock.setTimeToLive(ttl);
    _sock.joinGroup(_group);
    _port = mcastPort;
  }

  public MulticastServer(String name, int soTimeout, String mcastAddress,
    int mcastPort) throws IOException {
    this(name, soTimeout, mcastAddress, mcastPort, TTL);
  }

  public void setBufsize(int size) {
    _bytes.setBufSize(size);
  }

  public String getMulticastAddress() {
    return _groupStr;
  }

  public int getMulticastPort() {
    return _port;
  }

  public void send(byte[] toSend) throws IOException {
    if (_sock == null) {
      throw new IllegalStateException("Server not started");
    }

    DatagramPacket pack = new DatagramPacket(toSend, toSend.length, _group,
        _port);
    _sock.send(pack);
  }

  public void close() {
    try {
      _sock.leaveGroup(_group);
    } catch (IOException e) {
      // noop
    }

    _sock.close();
  }

  public void run() {
    DatagramPacket pack = null;

    while (true) {
      byte[] bytes = null;
      
      try{
        bytes = (byte[])_bytes.acquire();
      } catch (Exception e) {
        Log.error(getClass(), "Could not acquire byte buffer");
        e.printStackTrace();
        break;
      }
      
      try {
        pack = new DatagramPacket(bytes, bytes.length);
        _sock.receive(pack);
        handle(pack, _sock);
      } catch (InterruptedIOException e) {
        handleSoTimeout();
      } catch (EOFException e) {
        handlePacketSizeToShort(pack);
      } catch (SocketException e) {
        if (_sock.isClosed()) {
          break;
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        _bytes.release(bytes);
      }
    }
  }

  protected int bufSize() {
    return _bytes.getBufSize();
  }

  protected abstract void handleSoTimeout();

  protected abstract void handlePacketSizeToShort(DatagramPacket pack);

  protected abstract void handle(DatagramPacket pack, MulticastSocket sock);
}
