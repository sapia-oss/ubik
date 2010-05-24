package org.sapia.ubik.rmi.server.transport.nio.tcp;

import org.sapia.ubik.net.TCPAddress;

/**
 * @author Yanick Duchesne
 * 
 * <dl>
 * <dt><b>Copyright: </b>
 * <dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open
 * Source Software </a>. All Rights Reserved.</dd>
 * </dt>
 * <dt><b>License: </b>
 * <dd>Read the license.txt file of the jar or visit the <a
 * href="http://www.sapia-oss.org/license.html">license page </a> at the Sapia
 * OSS web site</dd>
 * </dt>
 * </dl>
 */
public class NioAddress extends TCPAddress {

  static final long          serialVersionUID = 1L;

  public static final String TRANSPORT_TYPE   = "tcp/nio";

  public NioAddress() {
    super();
  }

  /**
   * @param host
   *          a host
   * @param port
   *          a port
   */
  public NioAddress(String host, int port) {
    super(host, port);
  }

  /**
   * @see org.sapia.ubik.net.TCPAddress#getTransportType()
   */
  public String getTransportType() {
    return TRANSPORT_TYPE;
  }
}
