package org.sapia.ubik.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ConnectionFactory;


/**
 * Implements A UDP {@link ConnectionFactory}.
 * 
 * @author Yanick Duchesne
 */
public class UDPConnectionFactory implements ConnectionFactory {
  
  private int bufsize;
  private int timeout;

  /**
   * @param bufsize the internal buffer size to use.
   * @param the connection timeout.
   */
  public UDPConnectionFactory(int bufsize, int timeout) {
    this.bufsize = bufsize;
    this.timeout = timeout;
  }

  /**
   * @param host the host for which to obtain a connection.
   * @param port the port of the connection.
   * @return a {@link UDPConnection} bound to the given host and port.
   * 
   * @throws IOException if a problem occurs while creating the connection.
   */
  public Connection newConnection(String host, int port)
    throws IOException {
    return new UDPConnection(new UDPServerAddress(InetAddress.getByName(host), port), bufsize, timeout);
  }

  /**
   * @param addr the {@link InetAddress} for which to obtain a connection.
   * @param port the port of the connection.
   * @return a {@link UDPConnection} bound to the given host and port.
   * 
   * @throws IOException if a problem occurs while creating the connection.
   */
  public UDPConnection newConnection(InetAddress addr, int port)
    throws IOException {
    return new UDPConnection(new UDPServerAddress(addr, port), bufsize, timeout);
  }

  /**
   * @param socket the {@link DatagramSocket} to use for the connection.
   * @param pack a {@link DatagramPacket}.
   * @return a {@link UDPConnection} using the given socket.
   * 
   * @throws IOException if a problem occurs while creating the connection.
   */
  public UDPConnection newConnection(DatagramSocket socket, DatagramPacket pack) throws IOException {
    return new UDPConnection(socket, pack, bufsize, timeout);
  }
}
