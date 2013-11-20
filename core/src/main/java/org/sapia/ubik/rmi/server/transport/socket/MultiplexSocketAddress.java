package org.sapia.ubik.rmi.server.transport.socket;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

/**
 * The {@link ServerAddress} implementation used for the
 * {@link MultiplexSocketTransportProvider} implementation.
 * 
 * @author yduchesne
 * 
 */
public class MultiplexSocketAddress extends TCPAddress {

  /**
   * Meant for externalization only.
   */
  public MultiplexSocketAddress() {
  }

  public MultiplexSocketAddress(String host, int port) {
    super(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE, host, port);
  }

}
