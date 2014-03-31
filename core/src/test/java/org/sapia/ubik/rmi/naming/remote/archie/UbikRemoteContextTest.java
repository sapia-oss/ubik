package org.sapia.ubik.rmi.naming.remote.archie;

import java.io.Serializable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelTestSupport;

public class UbikRemoteContextTest {

  private UbikRemoteContext src, target;
  private EventChannel c1, c2;

  @Before
  public void setUp() throws Exception {
    EventChannel.disableReuse();
    c1 = EventChannelTestSupport.createEventChannel("ubik.test");
    c2 = EventChannelTestSupport.createEventChannel("ubik.test");
    c1.start();
    c2.start();
    src = UbikRemoteContext.newInstance(c1.getReference());
    target = UbikRemoteContext.newInstance(c2.getReference());
  }

  @After
  public void tearDown() throws Exception {
    EventChannel.disableReuse();
    src.close();
    target.close();
    
    c1.close();
    c2.close();
  }

  @Test
  public void testReplicatedBind() throws Exception {
    src.bind("service", new SerializableObj());
    Thread.sleep(2000);
    target.lookup("service");
  }

  @Test
  public void testReplicatedRebind() throws Exception {
    src.rebind("service", new SerializableObj());
    Thread.sleep(2000);
    target.lookup("service");
  }

  @Test
  public void testReplicatedLookup() throws Exception {
    src.rebind("service", new SerializableObj());
    EventChannel c3 = EventChannelTestSupport.createEventChannel("ubik.test");
    c3.start();
    try {
      UbikRemoteContext lateContext = UbikRemoteContext.newInstance(c3.getReference());
      Thread.sleep(2000);

      lateContext.lookup("service");
    } finally {
      c3.close();
    }
  }

  public static class SerializableObj implements Serializable {

  }

}
