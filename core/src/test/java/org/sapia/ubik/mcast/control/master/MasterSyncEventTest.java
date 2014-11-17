package org.sapia.ubik.mcast.control.master;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Serialization;

public class MasterSyncEventTest {

  private MasterSyncEvent event;
  
  @Before
  public void setUp() {
    event = new MasterSyncEvent(Collects.arrayToList(new NodeInfo(new TCPAddress("test", "test", 8000), "test")));
  }
  
  @Test
  public void testGetView() {
    assertEquals(1, event.getMasterView().size());
  }
  
  @Test
  public void testSerialization() throws Exception {
    MasterSyncEvent copy = (MasterSyncEvent) Serialization.deserialize(Serialization.serialize(event));
    assertEquals(1, copy.getMasterView().size());
  }
}
