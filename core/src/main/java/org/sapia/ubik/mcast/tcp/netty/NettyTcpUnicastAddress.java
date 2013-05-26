package org.sapia.ubik.mcast.tcp.netty;

import org.sapia.ubik.net.netty.NettyAddress;

/**
 * The {@link NettyAddress} implementation used for the Netty-based unicast dispatcher.
 * 
 * @author yduchesne
 *
 */
public class NettyTcpUnicastAddress extends NettyAddress {
  
  /**
   *  The transport type identifier.
   */
  public static final String TRANSPORT_TYPE = "tcp/unicast/netty";

  /**
   * Do not use: meant for externalization only.
   */
  public NettyTcpUnicastAddress() {
    super();
  }
  
  /**
   * @param host a server host. 
   * @param port a server port.
   */
  public NettyTcpUnicastAddress(String host, int port) {
    super(TRANSPORT_TYPE, host, port);
  }

}
