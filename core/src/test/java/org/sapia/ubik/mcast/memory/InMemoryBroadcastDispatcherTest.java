package org.sapia.ubik.mcast.memory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.testing.BroadcastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;

public class InMemoryBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {

  
  @Test
  public void testLoadDispatcher() {
    BroadcastDispatcher dispatcher = DispatcherFactory.loadBroadcastDispatcher(Consts.BROADCAST_PROVIDER_MEMORY);
    assertTrue(dispatcher instanceof InMemoryBroadcastDispatcher);
  }
  
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    assertFalse("Source dispatcher not unregistered", InMemoryDispatchChannel.getInstance().isRegistered((InMemoryBroadcastDispatcher) source));
    assertFalse("Domain dispatcher not unregistered",
        InMemoryDispatchChannel.getInstance().isRegistered((InMemoryBroadcastDispatcher) domainDestination));
    assertFalse("Non-domain dispatcher not unregistered",
        InMemoryDispatchChannel.getInstance().isRegistered((InMemoryBroadcastDispatcher) nonDomainDestination));
  }

  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    InMemoryBroadcastDispatcher bd = new InMemoryBroadcastDispatcher();
    bd.initialize(new DispatcherContext(consumer));
    return bd;
  }
}
