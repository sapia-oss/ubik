package org.sapia.ubik.mcast.tcp.netty;


import java.io.IOException;

import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;

public class NettyTcpUnicastDispatcherTest extends UnicastDispatcherTestSupport {
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer)
      throws IOException {
    return new NettyTcpUnicastDispatcher(consumer, Consts.DEFAULT_MARSHALLING_BUFSIZE);
  }
}
