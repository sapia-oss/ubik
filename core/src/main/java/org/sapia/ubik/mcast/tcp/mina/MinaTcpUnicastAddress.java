package org.sapia.ubik.mcast.tcp.mina;

import org.sapia.ubik.net.TCPAddress;

/**
 * The {@link NettyAddress} implementation used for the Mina-based unicast
 * dispatcher.
 * 
 * @author yduchesne
 * 
 */
public final class MinaTcpUnicastAddress extends TCPAddress {

  /**
   * The transport type identifier.
   */
  public static final String TRANSPORT_TYPE = "tcp/unicast/mina";

  /** Meant for serialization only */
  public MinaTcpUnicastAddress() {
  }

  public MinaTcpUnicastAddress(String host, int port) {
    super(TRANSPORT_TYPE, host, port);
  }
}