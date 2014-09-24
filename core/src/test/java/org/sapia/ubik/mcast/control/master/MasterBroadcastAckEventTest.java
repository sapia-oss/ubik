package org.sapia.ubik.mcast.control.master;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.ubik.util.Serialization;

public class MasterBroadcastAckEventTest {
  
  @Test
  public void testGetNodeCount() {
    assertEquals(1, new MasterBroadcastAckEvent(1).getNodeCount());
  }
  
  @Test
  public void testSerialization() throws Exception {
    MasterBroadcastAckEvent evt  = new MasterBroadcastAckEvent(1);
    MasterBroadcastAckEvent copy = (MasterBroadcastAckEvent) Serialization.deserialize(Serialization.serialize(evt));
    assertEquals(1, copy.getNodeCount());
  }

}
