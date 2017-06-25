package org.sapia.ubik.mcast.nats;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

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
public class NatsBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {

  @Test
  public void testLoadDispatcher() {
    BroadcastDispatcher dispatcher = DispatcherFactory.loadBroadcastDispatcher(Consts.BROADCAST_PROVIDER_NATS);
    assertTrue(dispatcher instanceof NatsBroadcastDispatcher);
  }

  @Override
  public BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    NatsBroadcastDispatcher abd = new NatsBroadcastDispatcher();
    abd.initialize(new DispatcherContext(consumer)
        .withConf(Conf.newInstance()
            .addProperties(Consts.BROADCAST_NATS_URL, "nats://localhost:4222")
            .addSystemProperties()));
    return abd;
  }
  
  @Override
  public void testDispatchWithDomainPartitioning() throws Exception {
    // noop: implementation does not support handling of domain partitioning.
  }

}
