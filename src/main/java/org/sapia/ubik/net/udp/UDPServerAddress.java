package org.sapia.ubik.net.udp;

import java.net.InetAddress;

import org.sapia.ubik.net.ServerAddress;


/**
 * Implement a {@link ServerAddress} around an {@link InetAddress} and a port.
 *
 * @author Yanick Duchesne
 */
public class UDPServerAddress implements ServerAddress {
  
  static final long serialVersionUID = 1L;
  
  public static final String TRANSPORT_TYPE = "udp/socket";
  
  private String             transportType = TRANSPORT_TYPE;
  private InetAddress        addr;
  private int                port;

  /**
   * @param addr the {@link InetAddress} of the server to which this instance corresponds.
   * @param port the port of the server to which this instance corresponds.
   */
  public UDPServerAddress(InetAddress addr, int port) {
    this(addr, port, TRANSPORT_TYPE);
  }

  /**
   * @param addr the {@link InetAddress} of the server to which this instance corresponds.
   * @param port the port of the server to which this instance corresponds.
   * @param transportType a transport type identifier.
   */
  public UDPServerAddress(InetAddress addr, int port, String transportType) {
    this.addr            = addr;
    this.port            = port;
    this.transportType   = transportType;
  }

  /**
   * @return this instance's {@link InetAddress}.
   */
  public InetAddress getInetAddress() {
    return addr;
  }

  /**
   * @return this instance's port.
   */
  public int getPort() {
    return port;
  }

  /**
   * @see Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if(other instanceof UDPServerAddress) {
      UDPServerAddress addr = (UDPServerAddress) other;
      return addr.addr.equals(addr) && (addr.port == port);
    }
    return false;
  }

  /**
   * @see ServerAddress#getTransportType()
   */
  public String getTransportType() {
    return transportType;
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    return addr.hashCode() * 31 ^ port * 31;
  }
}
