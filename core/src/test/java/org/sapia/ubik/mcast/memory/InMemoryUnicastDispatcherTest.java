package org.sapia.ubik.mcast.memory;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.testing.UnicastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;

public class InMemoryUnicastDispatcherTest extends UnicastDispatcherTestSupport {

  @Test
  public void testLoadDispatcher() {
    UnicastDispatcher dispatcher = DispatcherFactory.loadUnicastDispatcher(Consts.UNICAST_PROVIDER_MEMORY);
    assertTrue(dispatcher instanceof InMemoryUnicastDispatcher);
  }
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) {
    InMemoryUnicastDispatcher bd = new InMemoryUnicastDispatcher();
    bd.initialize(new DispatcherContext(consumer));
    return bd;
  }
}