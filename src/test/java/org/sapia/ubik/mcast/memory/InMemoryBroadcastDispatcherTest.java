package org.sapia.ubik.mcast.memory;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.After;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.BroadcastDispatcherTestSupport;
import org.sapia.ubik.mcast.EventConsumer;

public class InMemoryBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {
    

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    assertFalse("Source dispatcher not unregistered", 
        InMemoryDispatchChannel.getInstance().isRegistered((InMemoryBroadcastDispatcher)source));
    assertFalse("Domain dispatcher not unregistered", 
        InMemoryDispatchChannel.getInstance().isRegistered((InMemoryBroadcastDispatcher)domainDestination));
    assertFalse("Non-domain dispatcher not unregistered", 
        InMemoryDispatchChannel.getInstance().isRegistered((InMemoryBroadcastDispatcher)nonDomainDestination));
  }
  
  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer)
      throws IOException {
    return new InMemoryBroadcastDispatcher(consumer);
  }
}
