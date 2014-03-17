package org.sapia.ubik.rmi.examples.site.groupcomm;

import java.io.IOException;
import java.util.Properties;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.avis.AvisBroadcastDispatcher;
import org.sapia.ubik.mcast.tcp.mina.MinaTcpUnicastDispatcher;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

/**
 * Creates an {@link EventChannel} which does broadcast using Avis (using the {@link AvisBroadcastDispatcher})
 * and uses the default for point-to-point communication (which is provided by the {@link MinaTcpUnicastDispatcher}).
 *
 * @author yduchesne
 */
public class AvisEventChannel {


	public static void main(String[] args) throws IOException {

		Properties properties = new Properties();
		properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AVIS);
		properties.setProperty(Consts.BROADCAST_AVIS_URL, "elvin://localhost:2917");
		EventChannel channel = new EventChannel("myDomain", new Conf().addProperties(properties));
  }
}
