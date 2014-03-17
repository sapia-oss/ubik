package org.sapia.ubik.rmi.examples.site.groupcomm;

import java.io.IOException;
import java.util.Properties;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.tcp.mina.MinaTcpUnicastDispatcher;
import org.sapia.ubik.mcast.udp.UDPBroadcastDispatcher;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

/**
 * Creates an {@link EventChannel} which does broadcast using the default mechanism (IP multicast
 * through the {@link UDPBroadcastDispatcher}) and uses TCP for point-to-point communication
 * (through the {@link MinaTcpUnicastDispatcher}.
 *
 * @author yduchesne
 *
 */
public class TCPUnicastEventChannel {

	public static void main(String[] args) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP);
		EventChannel channel = new EventChannel("myDomain", new Conf().addProperties(properties));
  }
}
