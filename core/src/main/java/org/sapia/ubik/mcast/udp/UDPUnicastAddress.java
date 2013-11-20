package org.sapia.ubik.mcast.udp;

import java.net.InetAddress;

import org.sapia.ubik.net.ServerAddress;

/**
 * Implement a {@link ServerAddress} around a {@link InetAddress} and a port.
 * 
 * @author Yanick Duchesne
 */
public class UDPUnicastAddress implements ServerAddress {

  static final long serialVersionUID = 1L;

  private static final String TRANSPORT_TYPE = "udp/unicast";

  private InetAddress addr;
  private int port;
  private int hashCode;

  /**
   * Constructor for InetServerAddress.
   * 
   * @param addr
   *          the {@link InetAddress} of the server to which this instance
   *          corresponds.
   * 
   * @param port
   *          the port of the server to which this instance corresponds.
   */
  public UDPUnicastAddress(InetAddress addr, int port) {
    this.addr = addr;
    this.port = port;
    this.hashCode = (addr.toString() + port).hashCode();
  }

  /**
   * Returns this instance's {@link InetAddress}.
   * 
   * @return an {@link InetAddress}.
   */
  public InetAddress getInetAddress() {
    return addr;
  }

  /**
   * Returns this instance's port.
   * 
   * @return a port.
   */
  public int getPort() {
    return port;
  }

  /**
   * @see Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    try {
      UDPUnicastAddress addr = (UDPUnicastAddress) other;

      return addr.addr.equals(addr) && (addr.port == port);
    } catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * @see ServerAddress#getTransportType()
   */
  public String getTransportType() {
    return TRANSPORT_TYPE;
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    return hashCode;
  }

  public String toString() {
    return new StringBuffer("[").append("address=").append(addr).append(" ,port=").append(port).append("]").toString();
  }
}
