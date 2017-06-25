package org.sapia.ubik.mcast.udp;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.testing.BroadcastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;

@Ignore
public class UDPBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {
  
  @Test
  public void testLoadDispatcher() {
    BroadcastDispatcher dispatcher = DispatcherFactory.loadBroadcastDispatcher(Consts.BROADCAST_PROVIDER_UDP);
    assertTrue(dispatcher instanceof UDPBroadcastDispatcher);
  }

  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    UDPBroadcastDispatcher ud = new UDPBroadcastDispatcher();
    ud.initialize(new DispatcherContext(consumer));
    return ud;
  }
}
