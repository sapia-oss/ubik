package org.sapia.ubik.rmi.examples.site.transport.mplex;

import java.net.ServerSocket;

import org.sapia.ubik.net.mplex.MultiplexSocketConnector;
import org.sapia.ubik.net.mplex.ServerSocketAdapter;
import org.sapia.ubik.net.mplex.StreamSelector;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketHelper;

public class MplexSocketHelperExample {

	public static void main(String[] args) throws Exception {
	  
		MultiplexSocketConnector connector = MultiplexSocketHelper.createSocketConnector(new StreamSelector() {
			
			@Override
			public boolean selectStream(byte[] header) {
				// implement custom logic
				return true;
			}
		});
		
		
		ServerSocket socket = new ServerSocketAdapter(connector);
		// MyServer server = new MyServer(socket);
		// server.start();
  }
}
