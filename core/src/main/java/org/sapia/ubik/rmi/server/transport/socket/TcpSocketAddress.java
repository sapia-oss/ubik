package org.sapia.ubik.rmi.server.transport.socket;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

/**
 * The {@link ServerAddress} implementation used for the {@link SocketTransportProvider}.
 * 
 * @author yduchesne
 *
 */
public class TcpSocketAddress extends TCPAddress {
	
	/**
	 * Meant for externalization only.
	 */
	public TcpSocketAddress() {
	}

	/**
	 * @param host a host.
	 * @param port a port.
	 */
	public TcpSocketAddress(String host, int port) {
		super(SocketTransportProvider.SOCKET_TRANSPORT_TYPE, host, port);
  }

}
