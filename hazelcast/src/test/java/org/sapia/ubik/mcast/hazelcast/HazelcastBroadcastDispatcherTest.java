package org.sapia.ubik.mcast.hazelcast;


import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.testing.BroadcastDispatcherTestSupport;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastBroadcastDispatcherTest extends BroadcastDispatcherTestSupport {
  
  private HazelcastInstance hz;
  
  @Override
  protected void doSetup() throws Exception {
    hz = Hazelcast.newHazelcastInstance();
    Singleton.set(hz);
  }
  
  @Override
  protected void doTearDown() throws Exception {
    Singleton.unset();
    hz.shutdown();
  }
  
  @Test
  public void testLoadDispatcher() {
    BroadcastDispatcher dispatcher = DispatcherFactory.loadBroadcastDispatcher(Consts.BROADCAST_PROVIDER_HAZELCAST);
    assertTrue(dispatcher instanceof HazelcastBroadcastDispatcher);
  }
 
  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    HazelcastBroadcastDispatcher bd = new HazelcastBroadcastDispatcher();
    bd.initialize(new DispatcherContext(consumer).withConf(Conf.newInstance()
        .addProperties(Consts.BROADCAST_HAZELCAST_TOPIC, "unit-test-topic").addSystemProperties()));
    return bd;
  }

}
