package org.sapia.ubik.mcast.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.sapia.ubik.concurrent.ThreadStartup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Localhost;

/**
 * Implements a basic UDP server.
 * 
 * @author Yanick Duchesne
 */
public abstract class UDPServer extends Thread {

  private Category         log = Log.createCategory(getClass());
  
  protected DatagramSocket sock;
  private volatile int     bufsize = Defaults.DEFAULT_UDP_PACKET_SIZE;
  protected ThreadStartup  startupBarrier = new ThreadStartup();

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

  private static DatagramSocket createSocket(int port) throws UnknownHostException, SocketException {
    DatagramSocket socket = new DatagramSocket(port, Localhost.getPreferredLocalAddress());
    return socket;
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
  
  /**
   * Closes this instance's socket - in fact terminating it.
   */
  public void close() {
    if (sock != null) {
      sock.close();
    }
  }
  
  /**
   * @return this instance's startup barrier.
   */
  public ThreadStartup getStartupBarrier() {
    return startupBarrier;
  }
  
  /**
   * @return the {@link InetAddress} to which this instance is currently bound.
   */
  public InetAddress getLocalAddress() {
    Assertions.illegalState(sock == null, "Server not started");
    return sock.getLocalAddress();
  }

  /**
   * @return the port to which this instance is currently bound.
   */
  public int getPort() {
    Assertions.illegalState(sock == null, "Server not started");
    return sock.getLocalPort();
  }

  /**
   * @return this instance's buffer size.
   */
  public int getBufSize() {
    return bufsize;
  }
  
  public void setBufsize(int size) {
    bufsize = size;
  }
  
  // --------------------------------------------------------------------------
  // Template methods

  /**
   * @param pack invoked when a packet could not be read from due to this intance's buffer size being too small.
   */
  protected abstract void handlePacketSizeToShort(DatagramPacket pack);

  /**
   * @param pack a {@link DatagramPacket}.
   * @param sock the client socket {@link Socket}.
   */
  protected abstract void handle(DatagramPacket pack, DatagramSocket sock);
}
