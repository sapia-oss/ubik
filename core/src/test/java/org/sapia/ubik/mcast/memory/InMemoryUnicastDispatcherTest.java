package org.sapia.ubik.mcast.memory;

import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherTestSupport;

public class InMemoryUnicastDispatcherTest extends UnicastDispatcherTestSupport {

  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) {
    return new InMemoryUnicastDispatcher(consumer);
  }
}