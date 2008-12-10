package org.sapia.ubik.net.udp;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ConnectionFactory;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * @author Yanick Duchesne
 * 16-Jun-2003
 */
public class UDPConnectionFactory implements ConnectionFactory {
  private int _bufSize;
  private int _timeout;

  /**
   * Constructor for UDPConnectionFactory.
   */
  public UDPConnectionFactory(int bufSize, int timeout) {
    _bufSize   = bufSize;
    _timeout   = timeout;
  }

  /**
   * @see org.sapia.ubik.net.ConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port)
    throws IOException {
    return new UDPConnection(new UDPServerAddress(InetAddress.getByName(host),
        port), _bufSize, _timeout);
  }

  public UDPConnection newConnection(InetAddress addr, int port)
    throws IOException {
    return new UDPConnection(new UDPServerAddress(addr, port), _bufSize,
      _timeout);
  }

  public UDPConnection newConnection(DatagramSocket localSocket,
    DatagramPacket pack) throws IOException {
    return new UDPConnection(localSocket, pack, _bufSize, _timeout);
  }
}
