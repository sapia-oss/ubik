package org.sapia.ubik.mcast.udp;


import java.io.IOException;

import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherTestSupport;

public class UDPUnicastDispatcherTest extends UnicastDispatcherTestSupport {

  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) throws IOException {
    return new UDPUnicastDispatcher(consumer);
  }

}
