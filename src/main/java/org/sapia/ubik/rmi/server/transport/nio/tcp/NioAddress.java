package org.sapia.ubik.rmi.server.transport.nio.tcp;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

/**
 * Implements the NIO {@link ServerAddress} by extending the {@link TCPAddress}
 * class.
 * 
 * @author Yanick Duchesne
 * 
 */
public class NioAddress extends TCPAddress {

  static final long          serialVersionUID = 1L;

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
    super(NioTcpTransportProvider.TRANSPORT_TYPE, host, port);
  }

  /**
   * @see org.sapia.ubik.net.TCPAddress#getTransportType()
   */
  public String getTransportType() {
    return NioTcpTransportProvider.TRANSPORT_TYPE;
  }
}
