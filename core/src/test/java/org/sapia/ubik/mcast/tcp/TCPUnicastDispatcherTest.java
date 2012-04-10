package org.sapia.ubik.mcast.tcp;


import java.io.IOException;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherTestSupport;

public class TCPUnicastDispatcherTest extends UnicastDispatcherTestSupport {
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer)
      throws IOException {
    return new TCPUnicastDispatcher(consumer);
  }
}
