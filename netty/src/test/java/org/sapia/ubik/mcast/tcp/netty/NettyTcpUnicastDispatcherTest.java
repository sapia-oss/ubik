package org.sapia.ubik.mcast.tcp.netty;

import java.io.IOException;

import org.junit.After;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.testing.UnicastDispatcherTestSupport;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Conf;

public class NettyTcpUnicastDispatcherTest extends UnicastDispatcherTestSupport {

  @After
  public void tearDown() {
    Hub.shutdown();
  }
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) throws IOException {
    NettyTcpUnicastDispatcher ud = new NettyTcpUnicastDispatcher();
    ud.initialize(consumer, Conf.newInstance().addSystemProperties());
    return ud;
  }

}
