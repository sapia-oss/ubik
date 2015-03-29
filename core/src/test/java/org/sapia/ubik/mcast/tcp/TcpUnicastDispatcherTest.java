package org.sapia.ubik.mcast.tcp;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.testing.UnicastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class TcpUnicastDispatcherTest extends UnicastDispatcherTestSupport {
  
  @Test
  public void testLoadDispatcher() {
    UnicastDispatcher dispatcher = DispatcherFactory.loadUnicastDispatcher(Conf.newInstance().addProperties(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP_BIO));
    assertTrue(dispatcher instanceof TcpUnicastDispatcher);
  }

  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) throws IOException {
    TcpUnicastDispatcher ud = new TcpUnicastDispatcher();
    ud.initialize(consumer, Conf.newInstance().addProperties(Consts.MCAST_HANDLER_COUNT, "5").addSystemProperties());
    return ud;
  }
}
