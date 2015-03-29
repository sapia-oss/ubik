package org.sapia.ubik.rmi.server.transport.netty;

import org.junit.Test;
import org.sapia.ubik.rmi.server.Hub;

public class TranportManagerNettyTest {
  
  @Test
  public void testGetProviderFor_netty() {
    Hub.getModules().getTransportManager().getProviderFor(NettyTransportProvider.TRANSPORT_TYPE);
  }

}
