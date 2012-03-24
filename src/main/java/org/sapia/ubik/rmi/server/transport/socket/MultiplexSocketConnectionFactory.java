package org.sapia.ubik.rmi.server.transport.socket;


public class MultiplexSocketConnectionFactory extends SocketRmiConnectionFactory {
	
	public MultiplexSocketConnectionFactory() {
		super(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE);
  }

}
