package org.sapia.ubik.mcast.udp;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.testing.UnicastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;

public class UDPUnicastDispatcherTest extends UnicastDispatcherTestSupport {

  @Test
  public void testLoadDispatcher() {
    UnicastDispatcher dispatcher = DispatcherFactory.loadUnicastDispatcher(Consts.UNICAST_PROVIDER_UDP);
    assertTrue(dispatcher instanceof UDPUnicastDispatcher);
  }
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) throws IOException {
    UDPUnicastDispatcher ud = new UDPUnicastDispatcher();
    ud.initialize(new DispatcherContext(consumer));
    return ud;
  }

}
