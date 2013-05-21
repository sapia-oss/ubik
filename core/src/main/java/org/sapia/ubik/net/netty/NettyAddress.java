package org.sapia.ubik.net.netty;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.transport.netty.NettyTransportProvider;

/**
 * Implements the Netty {@link ServerAddress} by extending the {@link TCPAddress}
 * class.
 * 
 * @author Yanick Duchesne
 * 
 */
public class NettyAddress extends TCPAddress {

  static final long  serialVersionUID = 1L;

  /**
   * Do not call: meant for externalization.
   */
  public NettyAddress() {
    super();
  }
  
  /**
   * @param transportType
   *          a transport type
   * @param host
   *          a host
   * @param port
   *          a port
   */
  public NettyAddress(String transportType, String host, int port) {
    super(transportType, host, port);
  }

  /**
   * @param host
   *          a host
   * @param port
   *          a port
   */
  public NettyAddress(String host, int port) {
    super(NettyTransportProvider.TRANSPORT_TYPE, host, port);
  }

}
