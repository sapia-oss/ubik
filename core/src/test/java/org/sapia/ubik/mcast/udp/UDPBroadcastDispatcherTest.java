package org.sapia.ubik.mcast.udp;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.testing.BroadcastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

@Ignore
public class UDPBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {
  
  @Test
  public void testLoadDispatcher() {
    BroadcastDispatcher dispatcher = DispatcherFactory.loadBroadcastDispatcher(Conf.newInstance().addProperties(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UDP));
    assertTrue(dispatcher instanceof UDPBroadcastDispatcher);
  }

  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    UDPBroadcastDispatcher ud = new UDPBroadcastDispatcher();
    ud.initialize(consumer, Conf.newInstance().addProperties(
        Consts.MCAST_ADDR_KEY, Consts.DEFAULT_MCAST_ADDR, 
        Consts.MCAST_PORT_KEY, "" + Consts.DEFAULT_MCAST_PORT, 
        Consts.MCAST_TTL, "" + Defaults.DEFAULT_TTL
     ).addSystemProperties());
    return ud;
  }
}
