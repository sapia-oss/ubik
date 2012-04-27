package org.sapia.ubik.mcast.tcp;


import java.io.IOException;

import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;

public class NioTcpUnicastDispatcherTest extends UnicastDispatcherTestSupport {
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer)
      throws IOException {
    return new NioTcpUnicastDispatcher(consumer, Defaults.DEFAULT_HANDLER_COUNT, Consts.DEFAULT_MARSHALLING_BUFSIZE);
  }
}
