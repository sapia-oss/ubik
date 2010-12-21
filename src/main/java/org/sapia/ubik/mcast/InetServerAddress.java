package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;

import java.net.InetAddress;


/**
 * Implement a <code>ServerAddress</code> around a
 * an <code>InetAddress</code> and a port.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class InetServerAddress implements ServerAddress {
  
  static final long serialVersionUID = 1L;
  
  private String      _transportType = "udp/event/address";
  private InetAddress _addr;
  private int         _port;
  private int         _hashCode;

  /**
   * Constructor for InetServerAddress.
   *
   * @param addr the <code>InetAddress</code> of the server
   * to which this instance corresponds.
   *
   * @param port the port of the server
   * to which this instance corresponds.
   */
  public InetServerAddress(InetAddress addr, int port) {
    _addr       = addr;
    _port       = port;
    _hashCode   = (addr.toString() + port).hashCode();
  }

  /**
   * Returns this instance's <code>InetAddress</code>.
   *
   * @return an <code>InetAddess</code>.
   */
  public InetAddress getInetAddress() {
    return _addr;
  }

  /**
   * Returns this instance's port.
   *
   * @return a port.
   */
  public int getPort() {
    return _port;
  }

  /**
   * @see Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    try {
      InetServerAddress addr = (InetServerAddress) other;

      return addr._addr.equals(_addr) && (addr._port == _port);
    } catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * @see ServerAddress#getTransportType()
   */
  public String getTransportType() {
    return _transportType;
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    return _hashCode;
  }
  
  public String toString(){
    return new StringBuffer("[")
      .append("address=").append(_addr)
      .append(" ,port=").append(_port)
      .append("]").toString();
  }
}
