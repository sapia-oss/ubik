package org.sapia.ubik.mcast.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.sapia.ubik.concurrent.ThreadStartup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.util.Localhost;

/**
 * Implements a basic UDP server.
 * 
 * @author Yanick Duchesne
 */
public abstract class UDPServer extends Thread {

  private Category log = Log.createCategory(getClass());
  protected DatagramSocket sock;
  private int bufsize = Defaults.DEFAULT_UDP_PACKET_SIZE;
  protected ThreadStartup startupBarrier = new ThreadStartup();

  /**
   * Constructor for UDPServer.
   */
  public UDPServer(String name) throws java.net.SocketException {
    this(name, 0);
  }

  public UDPServer(String name, int port) throws java.net.SocketException {
    super(name);
    super.setDaemon(true);
    try {
      sock = createSocket(port);
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Could not bind to local address", e);
    }
  }

  public void setBufsize(int size) {
    bufsize = size;
  }

  private static DatagramSocket createSocket(int port) throws UnknownHostException, SocketException {
    DatagramSocket socket = new DatagramSocket(port, Localhost.getPreferredLocalAddress());
    return socket;
  }

  public int getPort() {
    return sock.getLocalPort();
  }

  public void run() {
    DatagramPacket pack = null;

    while (true) {
      try {
        pack = new DatagramPacket(new byte[bufsize], bufsize);
        startupBarrier.started();
        sock.receive(pack);
        handle(pack, sock);
      } catch (SocketTimeoutException e) {
        log.warning("Socket timeout out error while waiting for request", e);
      } catch (InterruptedIOException e) {
        if (!sock.isClosed()) {
          sock.close();
        }
        break;
      } catch (SocketException e) {
        if (sock.isClosed()) {
          break;
        }
      } catch (EOFException e) {
        handlePacketSizeToShort(pack);
      } catch (IOException e) {
        log.error("IO Exception while waiting for request", e);
      }
    }
  }

  protected int bufSize() {
    return bufsize;
  }

  protected abstract void handlePacketSizeToShort(DatagramPacket pack);

  protected abstract void handle(DatagramPacket pack, DatagramSocket sock);
}
