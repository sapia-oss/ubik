package org.sapia.ubik.mcast;

import java.io.IOException;
import java.util.Properties;

import org.sapia.ubik.mcast.memory.InMemoryBroadcastDispatcher;
import org.sapia.ubik.mcast.memory.InMemoryUnicastDispatcher;
import org.sapia.ubik.mcast.tcp.TCPUnicastDispatcher;
import org.sapia.ubik.mcast.udp.UDPBroadcastDispatcher;
import org.sapia.ubik.mcast.udp.UDPUnicastDispatcher;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;

/**
 * A convenient factory of {@link EventChannel}s.
 * 
 * @author yduchesne
 *
 */
public class EventChannels {

  /**
   * Creates an event channel that uses the a a {@link UDPBroadcastDispatcher} and a {@link UDPUnicastDispatcher}. 
   * The {@link UDPBroadcastDispatcher} will be created using the default multicast address and port.
   * 
   * @param domain
   * @return a new {@link EventChannel}
   * @throws IOException if a problem occurs creating the channel.
   * 
   * @see Consts#DEFAULT_MCAST_ADDR
   * @see Consts#DEFAULT_MCAST_PORT
   */
  public static EventChannel createDefaultUdpEventChannel(String domain) throws IOException {
    return new EventChannel(domain, Consts.DEFAULT_MCAST_ADDR, Consts.DEFAULT_MCAST_PORT);
  }
  
  /**
   * Creates an event channel that uses the a a {@link UDPBroadcastDispatcher} and a {@link UDPUnicastDispatcher}. 
   * The {@link UDPBroadcastDispatcher} will be created using the given multicast address and port.
   * 
   * @param domain
   * @return a new {@link EventChannel}
   * @throws IOException if a problem occurs creating the channel.
   */  
  public static EventChannel createUdpEventChannel(String domain, String mcastAddr, int mcastPort) throws IOException {
    return new EventChannel(domain, mcastAddr, mcastPort);
  }
  
  /**
   * Creates an event channel that uses the a a {@link UDPBroadcastDispatcher} and a {@link TCPUnicastDispatcher}. 
   * The {@link UDPBroadcastDispatcher} will be created using the given multicast address and port.
   * 
   * @param domain
   * @return a new {@link EventChannel}
   * @throws IOException if a problem occurs creating the channel.
   */  
  public static EventChannel createTcpEventChannel(String domain, String mcastAddr, int mcastPort) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP);
    return new EventChannel(domain, new Props().addProperties(properties));
  }
  
  /**
   * Creates an event channel that uses the a a {@link InMemoryBroadcastDispatcher} and an {@link InMemoryUnicastDispatcher}. 
   * The {@link UDPBroadcastDispatcher} will be created using the given multicast address and port.
   * 
   * @param domain
   * @return a new {@link EventChannel}
   * @throws IOException if a problem occurs creating the channel.
   */  
  public static EventChannel createInMemoryEventChannel(String domain) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_MEMORY);
    return new EventChannel(domain, new Props().addProperties(properties));
  }
  
}
