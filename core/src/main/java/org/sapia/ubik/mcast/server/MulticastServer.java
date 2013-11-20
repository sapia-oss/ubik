package org.sapia.ubik.mcast.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Localhost;

/**
 * Implements a basic multicast server. One only needs inheriting from this
 * class and override the {@link #handle(DatagramPacket, MulticastSocket)}
 * method.
 * 
 * @author Yanick Duchesne
 */
public abstract class MulticastServer {

  static final int DEFAULT_TTL = 7;

  protected Category log = Log.createCategory(getClass());
  protected MulticastSocket sock;
  private InetAddress group;
  private String groupStr;
  private int port;
  private Thread serverThread;
  private int bufSize = Defaults.DEFAULT_UDP_PACKET_SIZE;

  private class Acceptor implements Runnable {

    private Acceptor() {
    }

    @Override
    public void run() {
      doRun();
    }
  }

  /**
   * @param name
   *          the name to use for this instance's server thread.
   * @param mcastAddress
   *          the address of the multicast group on which this instance should
   *          listen.
   * @param mcastPort
   *          the multicast port to which this instance should bind.
   * @param ttl
   *          the time-to-live of datagram packets that this instance will send.
   * @throws IOException
   *           if a problem occurs while calling this constructor.
   */
  protected MulticastServer(String name, String mcastAddress, int mcastPort, int ttl) throws IOException {
    serverThread = NamedThreadFactory.createWith(name).setDaemon(true).newThread(new Acceptor());
    group = InetAddress.getByName(mcastAddress);
    groupStr = mcastAddress;
    sock = new MulticastSocket(mcastPort);
    if (Localhost.isIpPatternDefined()) {
      sock.setNetworkInterface(NetworkInterface.getByInetAddress(Localhost.getAnyLocalAddress()));
    }
    sock.setTimeToLive(ttl);
    sock.joinGroup(group);
    port = mcastPort;
  }

  /**
   * Constructs an instance of this class with the default time-to-live (see
   * {@value #DEFAULT_TTL}).
   * 
   * @param name
   *          the name to use for this instance's server thread.
   * @param mcastAddress
   *          the address of the multicast group on which this instance should
   *          listen.
   * @param mcastPort
   *          the multicast port to which this instance should bind.
   * @throws IOException
   *           if a problem occurs while calling this constructor.
   */
  protected MulticastServer(String name, String mcastAddress, int mcastPort) throws IOException {
    this(name, mcastAddress, mcastPort, DEFAULT_TTL);
  }

  /**
   * Sets this instance buffer size.
   * 
   * @param size
   *          a buffer size.
   */
  public void setBufsize(int size) {
    this.bufSize = size;
  }

  /**
   * @return this instance's multicast address.
   */
  public String getMulticastAddress() {
    return groupStr;
  }

  /**
   * @return this instance's multicast port.
   */
  public int getMulticastPort() {
    return port;
  }

  /**
   * Sends the given bytes to this instance's multicast group.s
   * 
   * @param toSend
   *          the bytes to send.
   * @throws IOException
   *           if a problem occurs trying to send the given bytes.
   */
  public void send(byte[] toSend) throws IOException {
    Assertions.illegalState(sock == null, "Server not started");

    DatagramPacket pack = new DatagramPacket(toSend, toSend.length, group, port);
    sock.send(pack);
  }

  /**
   * Starts this instance.
   */
  public synchronized void start() {
    if (!serverThread.isAlive()) {
      serverThread.start();
    }
  }

  /**
   * Closes this instance.
   */
  public synchronized void close() {
    if (sock != null) {
      try {
        sock.leaveGroup(group);
      } catch (IOException e) {
        // noop
      }
      sock.close();
    }
    ThreadShutdown.create(serverThread).shutdownLenient();
  }

  private void doRun() {
    DatagramPacket pack = null;
    byte[] bytes = new byte[bufSize];
    while (true) {

      try {
        pack = new DatagramPacket(bytes, bytes.length);
        sock.receive(pack);
        handle(pack, sock);
      } catch (EOFException e) {
        log.error("EOF: could not read incoming packet bytes. Packet size may be too short: " + bufSize);
      } catch (SocketException e) {
        // socket might have been closed, which is the case when this instance's
        // close() method is called. In which case, just exit silently
        if (sock.isClosed()) {
          break;
        } else {
          log.error("Socket error caught while reading data", e);
        }
      } catch (Exception e) {
        log.error("Unexpected exception caught while waiting for incoming packets", e);
      } finally {
        pack.setLength(bytes.length);
      }
    }
  }

  /**
   * @return the size (in bytes) of the buffers that are used to read incoming
   *         datagrams.
   */
  protected int bufSize() {
    return bufSize;
  }

  /**
   * This template method is called internally by this instance whenever a new
   * incoming datagram is read.
   * 
   * @param pack
   *          an incoming {@link DatagramPacket}.
   * @param sock
   *          the {@link MulticastSocket} that was used to read the incoming
   *          datagram.
   */
  protected abstract void handle(DatagramPacket pack, MulticastSocket sock);
}
