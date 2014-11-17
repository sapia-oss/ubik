package org.sapia.ubik.mcast.control.master;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.util.Serialization;

public class MasterBroadcastAckEventTest {
  
  @Test
  public void testSerialization() throws Exception {
    MasterBroadcastAckEvent evt  = new MasterBroadcastAckEvent(new ArrayList<NodeInfo>());
    MasterBroadcastAckEvent copy = (MasterBroadcastAckEvent) Serialization.deserialize(Serialization.serialize(evt));
    assertEquals(0, copy.getSlaveView().size());
  }

}
