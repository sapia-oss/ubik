package org.sapia.ubik.mcast;

import java.util.Properties;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class EventChannelTestSupport {

  public static EventChannel createEventChannel(String domainName, long heartBeatInterval, long heartBeatTimeout) throws Exception {
    Properties properties = new Properties();
    properties.setProperty(Consts.MCAST_HEARTBEAT_TIMEOUT, Long.toString(heartBeatTimeout));
    //properties.setProperty(Consts.MCAST_CONTROL_RESPONSE_TIMEOUT, Long.toString(heartBeatInterval));
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_MEMORY);
    return new EventChannel(domainName, new Conf().addProperties(properties));
  }

  public static EventChannel createEventChannel(String domainName) throws Exception {
    Properties properties = new Properties();
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_MEMORY);
    return new EventChannel(domainName, new Conf().addProperties(properties));
  }
}
