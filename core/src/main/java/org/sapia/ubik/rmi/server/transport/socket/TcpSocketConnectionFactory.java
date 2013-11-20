package org.sapia.ubik.rmi.server.transport.socket;

public class TcpSocketConnectionFactory extends SocketRmiConnectionFactory {

  public TcpSocketConnectionFactory() {
    super(SocketTransportProvider.SOCKET_TRANSPORT_TYPE);
  }

}
