package org.sapia.ubik.rmi.server.transport.socket;

import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.mplex.MultiplexServerSocket;
import org.sapia.ubik.net.mplex.MultiplexSocketConnector;
import org.sapia.ubik.net.mplex.StreamSelector;
import org.sapia.ubik.rmi.server.Hub;

public class MultiplexSocketHelper {
	
  /**
   * Creates a new socket connector for the stream selector passed in.
   *
   * @param aSelector The stream selector of the connector to create.
   * @return The created socket connector.
   */
  public static MultiplexSocketConnector createSocketConnector(StreamSelector aSelector) {
  	SocketServer server = (SocketServer) Hub.getModules()
  			.getServerTable()
  			.getServerFor(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE);
    MultiplexServerSocket serverSocket = (MultiplexServerSocket) server.serverSocket();
    return serverSocket.createSocketConnector(aSelector);
  }

  /**
   * Removes the passed in connector from the multiplex server.
   *
   * @param anInterceptor The connector to remove.
   */
  public static void removeSocketConnector(MultiplexSocketConnector anInterceptor) {
  	SocketServer server = (SocketServer) Hub.getModules()
  			.getServerTable()
  			.getServerFor(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE);
    MultiplexServerSocket serverSocket = (MultiplexServerSocket) server.serverSocket();
    serverSocket.removeSocketConnector(anInterceptor);
  }


}
