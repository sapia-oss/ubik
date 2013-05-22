package org.sapia.ubik.rmi.server.transport.mina;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

/**
 * Implements the NIO {@link ServerAddress} by extending the {@link TCPAddress}
 * class.
 * 
 * @author Yanick Duchesne
 * 
 */
public class MinaAddress extends TCPAddress {

  static final long          serialVersionUID = 1L;

  public MinaAddress() {
    super();
  }

  /**
   * @param host
   *          a host
   * @param port
   *          a port
   */
  public MinaAddress(String host, int port) {
    super(MinaTransportProvider.TRANSPORT_TYPE, host, port);
  }

  /**
   * @see org.sapia.ubik.net.TCPAddress#getTransportType()
   */
  public String getTransportType() {
    return MinaTransportProvider.TRANSPORT_TYPE;
  }
}
