package org.sapia.ubik.mcast.tcp;


import java.io.IOException;

import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherTestSupport;

public class TcpUnicastDispatcherTest extends UnicastDispatcherTestSupport {
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer)
      throws IOException {
    return new TcpUnicastDispatcher(consumer, Defaults.DEFAULT_HANDLER_COUNT);
  }
}
