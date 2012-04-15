package org.sapia.ubik.rmi.examples.site.groupcomm;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;

public class BasicEventChannel {

	
	public static void main(String[] args) {
	  try {
	  	
	  	EventChannel channel = new EventChannel("mydomain");
	  	channel.start();
	  	
	  	final EmbeddableJNDIServer jndiServer    = new EmbeddableJNDIServer(channel, 1099);
	  	jndiServer.start(true);
	  	
	  	Runtime.getRuntime().addShutdownHook(new Thread() {
	  		
	  		@Override
	  		public void run() {
	  		  jndiServer.stop();
	  		}
	  	});
	  	
	  } catch (Exception e) {
	  	e.printStackTrace();
	  }
		
  }
}
