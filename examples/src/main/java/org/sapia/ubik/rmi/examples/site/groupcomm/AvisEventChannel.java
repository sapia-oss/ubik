package org.sapia.ubik.rmi.examples.site.groupcomm;

import java.io.IOException;
import java.util.Properties;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class AvisEventChannel {

	
	public static void main(String[] args) throws IOException {
	  
		Properties properties = new Properties();
		properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AVIS);
		properties.setProperty(Consts.BROADCAST_AVIS_URL, "elvin://localhost:2917");
		EventChannel channel = new EventChannel("myDomain", new Conf().addProperties(properties));
  }
}
