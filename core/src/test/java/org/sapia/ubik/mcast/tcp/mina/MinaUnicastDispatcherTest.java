package org.sapia.ubik.mcast.tcp.mina;


import java.io.IOException;

import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherTestSupport;
import org.sapia.ubik.mcast.tcp.mina.MinaTcpUnicastDispatcher;
import org.sapia.ubik.rmi.Consts;

public class MinaUnicastDispatcherTest extends UnicastDispatcherTestSupport {
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer)
      throws IOException {
    return new MinaTcpUnicastDispatcher(consumer, Defaults.DEFAULT_HANDLER_COUNT, Consts.DEFAULT_MARSHALLING_BUFSIZE);
  }
}
