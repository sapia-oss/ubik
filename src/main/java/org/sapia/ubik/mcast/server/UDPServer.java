package org.sapia.ubik.mcast.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class UDPServer extends Thread {
  static final int         DEFAULT_BUFSZ = 1000;
  protected DatagramSocket _sock;
  private int              _bufsize = DEFAULT_BUFSZ;

  /**
   * Constructor for UDPServer.
   */
  public UDPServer(String name, int soTimeout) throws java.net.SocketException {
    super(name);
    super.setDaemon(true);
    _sock = new DatagramSocket();
    _sock.setSoTimeout(soTimeout);
  }

  public UDPServer(String name, int soTimeout, int port)
    throws java.net.SocketException {
    super(name);
    _sock = new DatagramSocket(port);
    _sock.setSoTimeout(soTimeout);
  }

  public void setBufsize(int size) {
    _bufsize = size;
  }

  public int getPort() {
    return _sock.getLocalPort();
  }

  public void run() {
    DatagramPacket pack = null;

    while (true) {
      try {
        pack = new DatagramPacket(new byte[_bufsize], _bufsize);
        _sock.receive(pack);
        handle(pack, _sock);
      } catch (InterruptedIOException e) {
        handleSoTimeout();
      } catch (SocketException e) {
        if (_sock.isClosed()) {
          break;
        }
      } catch (EOFException e) {
        handlePacketSizeToShort(pack);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  protected int bufSize() {
    return _bufsize;
  }

  protected abstract void handleSoTimeout();

  protected abstract void handlePacketSizeToShort(DatagramPacket pack);

  protected abstract void handle(DatagramPacket pack, DatagramSocket sock);
}
