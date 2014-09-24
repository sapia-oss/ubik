package org.sapia.ubik.mcast.control.master;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.ubik.util.Serialization;

public class MasterBroadcastEventTest {
  
  @Test
  public void testGetNodeCount() {
    assertEquals(1, new MasterBroadcastEvent(1).getNodeCount());
  }
  
  @Test
  public void testSerialization() throws Exception {
    MasterBroadcastEvent evt  = new MasterBroadcastEvent(1);
    MasterBroadcastEvent copy = (MasterBroadcastEvent) Serialization.deserialize(Serialization.serialize(evt));
    assertEquals(1, copy.getNodeCount());
  }

}
