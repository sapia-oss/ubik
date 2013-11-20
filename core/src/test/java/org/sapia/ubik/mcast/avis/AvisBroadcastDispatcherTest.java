package org.sapia.ubik.mcast.avis;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.BroadcastDispatcherTestSupport;
import org.sapia.ubik.mcast.EventConsumer;

public class AvisBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {

  private TestRouterSetup router;

  @Before
  public void setUp() throws Exception {
    router = new TestRouterSetup();
    router.setUp();
    super.setUp();
  }

  @Override
  public BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    return new AvisBroadcastDispatcher(consumer, "elvin://localhost");
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    router.tearDown();
  }
}
