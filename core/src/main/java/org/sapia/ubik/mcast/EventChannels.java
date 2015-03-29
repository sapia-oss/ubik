package org.sapia.ubik.mcast;

import java.io.IOException;
import java.util.Properties;

import org.sapia.ubik.mcast.memory.InMemoryBroadcastDispatcher;
import org.sapia.ubik.mcast.memory.InMemoryUnicastDispatcher;
import org.sapia.ubik.mcast.tcp.TcpUnicastDispatcher;
import org.sapia.ubik.mcast.udp.UDPBroadcastDispatcher;
import org.sapia.ubik.mcast.udp.UDPUnicastDispatcher;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

/**
 * A convenient factory of {@link EventChannel}s.
 * 
 * @author yduchesne
 * 
 */
public class EventChannels {

  /**
   * Creates an event channel that uses a {@link UDPBroadcastDispatcher} and a
   * {@link UDPUnicastDispatcher}. The {@link UDPBroadcastDispatcher} will be
   * created using the default multicast address and port.
   * 
   * @param domain
   *          the channel's domain.
   * @return a new {@link EventChannel}
   * @throws IOException
   *           if a problem occurs creating the channel.
   * 
   * @see Consts#DEFAULT_MCAST_ADDR
   * @see Consts#DEFAULT_MCAST_PORT
   */
  public static EventChannel createDefaultUdpEventChannel(String domain) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_UDP);
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UDP);
    properties.setProperty(Consts.MCAST_ADDR_KEY, Consts.DEFAULT_MCAST_ADDR);
    properties.setProperty(Consts.MCAST_PORT_KEY, "" + Consts.DEFAULT_MCAST_PORT);
    return new EventChannel(domain, Conf.newInstance().addProperties(properties).addSystemProperties());
  }

  /**
   * Creates an event channel that uses a {@link UDPBroadcastDispatcher} and a
   * {@link UDPUnicastDispatcher}. The {@link UDPBroadcastDispatcher} will be
   * created using the given multicast address and port.
   * 
   * @param domain
   *          the channel's domain.
   * @return a new {@link EventChannel}
   * @throws IOException
   *           if a problem occurs creating the channel.
   */
  public static EventChannel createUdpEventChannel(String domain, String mcastAddr, int mcastPort) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_UDP);
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UDP);
    properties.setProperty(Consts.MCAST_ADDR_KEY, mcastAddr);
    properties.setProperty(Consts.MCAST_PORT_KEY, "" + mcastPort);
    return new EventChannel(domain, Conf.newInstance().addProperties(properties).addSystemProperties());
  }

  /**
   * Creates an event channel that uses a {@link UDPBroadcastDispatcher} and a
   * {@link TcpUnicastDispatcher}. The {@link UDPBroadcastDispatcher} will be
   * created using the given multicast address and port.
   * 
   * @param domain
   *          the channel's domain.
   * @return a new {@link EventChannel}
   * @throws IOException
   *           if a problem occurs creating the channel.
   */
  public static EventChannel createTcpEventChannel(String domain, String mcastAddr, int mcastPort) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP_NIO);
    return new EventChannel(domain, new Conf().addProperties(properties));
  }

  /**
   * Creates an {@link EventChannel} that uses an
   * {@link AvisBroadcastDispatcher} and a {@link TcpUnicastDispatcher}.
   * 
   * @param domain
   *          the channel's domain.
   * @param avisUrl
   *          the URL of the Avis router to connect to.
   * @return a new {@link EventChannel}
   * @throws IOException
   *           if a problem occurs creating the channel.
   */
  public static EventChannel createAvisEventChannel(String domain, String avisUrl) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP_NIO);
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AVIS);
    properties.setProperty(Consts.BROADCAST_AVIS_URL, avisUrl);
    return new EventChannel(domain, new Conf().addProperties(properties));
  }

  /**
   * Creates an event channel that uses a {@link InMemoryBroadcastDispatcher}
   * and an {@link InMemoryUnicastDispatcher}. The
   * {@link UDPBroadcastDispatcher} will be created using the given multicast
   * address and port.
   * 
   * @param domain
   *          the channel's domain.
   * @return a new {@link EventChannel}
   * @throws IOException
   *           if a problem occurs creating the channel.
   */
  public static EventChannel createInMemoryEventChannel(String domain) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_MEMORY);
    return new EventChannel(domain, new Conf().addProperties(properties));
  }

}
