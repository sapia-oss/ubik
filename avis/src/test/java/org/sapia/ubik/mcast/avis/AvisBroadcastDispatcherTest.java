package org.sapia.ubik.mcast.avis;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.testing.BroadcastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

@Ignore
public class AvisBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {

  
  @Test
  public void testLoadDispatcher() {
    BroadcastDispatcher dispatcher = DispatcherFactory.loadBroadcastDispatcher(Consts.BROADCAST_PROVIDER_AVIS);
    assertTrue(dispatcher instanceof AvisBroadcastDispatcher);
  }

  @Override
  public BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    AvisBroadcastDispatcher abd = new AvisBroadcastDispatcher();
    abd.initialize(new DispatcherContext(consumer)
        .withConf(Conf.newInstance()
            .addProperties(Consts.BROADCAST_AVIS_URL, "elvin://localhost")
            .addSystemProperties()));
    return abd;
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }
}
