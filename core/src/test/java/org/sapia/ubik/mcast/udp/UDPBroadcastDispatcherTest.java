package org.sapia.ubik.mcast.udp;

import java.io.IOException;

import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.BroadcastDispatcherTestSupport;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.rmi.Consts;

public class UDPBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {

  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    return new UDPBroadcastDispatcher(consumer, Consts.DEFAULT_MCAST_ADDR, Consts.DEFAULT_MCAST_PORT, Defaults.DEFAULT_TTL);
  }
}
