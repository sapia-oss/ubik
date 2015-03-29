package org.sapia.ubik.rmi.server.transport.http;

import org.junit.After;
import org.junit.Test;
import org.sapia.ubik.rmi.server.Hub;

public class TransportManagerHttpTest {

  @After
  public void tearDown() {
    Hub.shutdown();
  }
  
  @Test
  public void testGetProviderFor_http() {
    Hub.getModules().getTransportManager().getProviderFor(HttpTransportProvider.TRANSPORT_TYPE);
  }
}
