package org.sapia.ubik.rmi.server.transport.socket;

import org.sapia.ubik.net.TCPAddress;

public class TcpSocketAddress extends TCPAddress {
	
	public TcpSocketAddress(String host, int port) {
		super(SocketTransportProvider.SOCKET_TRANSPORT_TYPE, host, port);
  }

}
