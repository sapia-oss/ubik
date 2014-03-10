package org.sapia.ubik.mcast.hazelcast;


import java.io.IOException;

import org.junit.Test;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.BroadcastDispatcherTestSupport;
import org.sapia.ubik.mcast.EventConsumer;

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
 
  @Override
  protected BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException {
    return new HazelcastBroadcastDispatcher(consumer, "unit-test-topic");
  }

}
