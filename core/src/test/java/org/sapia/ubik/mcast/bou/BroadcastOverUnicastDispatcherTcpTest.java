package org.sapia.ubik.mcast.bou;

import java.io.IOException;

import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.bou.BroadcastOverUnicastDispatcher.ViewCallback;
import org.sapia.ubik.mcast.tcp.mina.MinaTcpUnicastDispatcher;

public class BroadcastOverUnicastDispatcherTcpTest extends BroadcastOverUnicastDispatcherTestSupport {
  
  private MinaTcpUnicastDispatcher ud;
  
  @Override
  protected void doTearDown() throws Exception {
    ud.close();
  }

  @Override
  protected BroadcastDispatcher createDispatcher(final EventConsumer consumer, final ViewCallback view) throws IOException {
    ud = new MinaTcpUnicastDispatcher();
    ud.initialize(new DispatcherContext(consumer));
    ud.start();
    addressesByNode.put(consumer.getNode(), ud.getAddress());
    BroadcastOverUnicastDispatcher bd = new BroadcastOverUnicastDispatcher(view, ud, 2);
    return bd;
  }

}
