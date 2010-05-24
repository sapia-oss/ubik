package org.sapia.ubik.net;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This class models a TCP "server address". This class encapsulates host and port information.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TCPAddress implements java.io.Externalizable, ServerAddress {
  static final long          serialVersionUID = 1L;
  public static final String TRANSPORT_TYPE = "tcp/socket";
  private String             _host;
  private int                _port;
  private int                _hashCode;
  protected String           _transportType = TRANSPORT_TYPE;

  /** Do not call; used for externalization only. */
  public TCPAddress() {
  }

  /**
   * Creates an instance of this class with the given host and port
   * information.
   *
   * @param host the address of a host.
   * @param port the port of the server running on the given host.
   *
   */
  public TCPAddress(String host, int port) {
    _host       = host;
    _port       = port;
    _hashCode   = (host + port).hashCode();
  }

  /**
   * Overrides equals; two host identifiers are equal if they have the
   * same host and port.
   *
   * @param obj the instance with which to perform the comparison.
   */
  public boolean equals(Object obj) {
    TCPAddress other;

    try {
      other = (TCPAddress) obj;

      return (other._port == _port) && other._host.equals(_host);
    } catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * Returns the address of the host to which this instance corresponds.
   *
   * @return this instance's host address.
   */
  public String getHost() {
    return _host;
  }

  /**
   * Returns the port of the server to which this instance corresponds.
   *
   * @return a server port..
   */
  public int getPort() {
    return _port;
  }

  /**
   * This method returns a hash code based on this instances host and port.
   *
   * @return a hash code, as an <code>int</code>.
   */
  public int hashCode() {
    return _hashCode;
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _host            = in.readUTF();
    _port            = in.readInt();
    _hashCode        = in.readInt();
    _transportType   = in.readUTF();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(_host);
    out.writeInt(_port);
    out.writeInt(_hashCode);
    out.writeUTF(_transportType);
  }

  /**
   * @see org.sapia.ubik.net.ServerAddress#getTransportType()
   */
  public String getTransportType() {
    return _transportType;
  }

  /**
   * Returns a string representation of this instance.
   *
   * @return a <code>String</code>.
   */
  public String toString() {
    return "[ host=" + _host + ", port=" + _port + ", type=" + _transportType +
    " ]";
  }
}
