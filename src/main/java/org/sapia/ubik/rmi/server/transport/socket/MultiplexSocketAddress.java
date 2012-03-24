package org.sapia.ubik.rmi.server.transport.socket;

import org.sapia.ubik.net.TCPAddress;

public class MultiplexSocketAddress extends TCPAddress {
	
	public MultiplexSocketAddress(String host, int port) {
		super(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE, host, port);
  }

}
