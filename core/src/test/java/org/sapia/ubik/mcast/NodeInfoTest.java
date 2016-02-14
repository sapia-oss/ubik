package org.sapia.ubik.mcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Serialization;
import org.sapia.ubik.util.SysClock.MutableClock;

public class NodeInfoTest {

  private MutableClock clock;
  
  private NodeInfo node;
  
  @Before
  public void setUp() throws Exception {
    clock = MutableClock.getInstance();
    node  = new NodeInfo(new TCPAddress("test", "test", 1), "123");
    node.touch(clock);
    assertEquals(NodeInfo.State.NORMAL, node.getState());
  }

  @Test
  public void testReset() {
    clock.increaseCurrentTimeMillis(1000);
    node.checkState(100, clock);
   
    assertEquals(NodeInfo.State.SUSPECT, node.getState());
    node.reset(clock);
    assertEquals(NodeInfo.State.NORMAL, node.getState());
  }

  @Test
  public void testSuspect() {
    node.suspect();
    
    assertEquals(NodeInfo.State.SUSPECT, node.getState());
  }

  @Test
  public void testCheckState() {
    clock.increaseCurrentTimeMillis(1000);
    node.checkState(100, clock);
   
    assertEquals(NodeInfo.State.SUSPECT, node.getState());
  }

  @Test
  public void testTouch() {
    clock.increaseCurrentTimeMillis(1000);
    node.touch(clock);
    
    assertEquals(1000L, node.getTimestamp());
  }

  @Test
  public void testSerialization() throws Exception {
    byte[] payload = Serialization.serialize(node);
    
    NodeInfo copy = (NodeInfo) Serialization.deserialize(payload);
    
    assertEquals(node, copy);
  }

  @Test
  public void testEquals() {
    NodeInfo other = new NodeInfo(new TCPAddress("test", "test", 1), "123");
    assertEquals(node,  other);
  }

  
  @Test
  public void testEquals_false() {
    NodeInfo other = new NodeInfo(new TCPAddress("test", "test", 1), "456");
    assertNotEquals(node,  other);
  }
  
  @Test
  public void testCompare_same() {
    NodeInfo other = new NodeInfo(new TCPAddress("test", "test", 1), "456");
    other.touch(clock);
    
    assertEquals(0, node.compareTo(other));
  }
  
  @Test
  public void testCompare_befores() {
    NodeInfo other = new NodeInfo(new TCPAddress("test", "test", 1), "456");
    clock.increaseCurrentTimeMillis(1);
    other.touch(clock);
    
    assertEquals(-1, node.compareTo(other));
  }
  
  @Test
  public void testCompare_after() {
    NodeInfo other = new NodeInfo(new TCPAddress("test", "test", 1), "456");
    other.touch(clock);
    clock.increaseCurrentTimeMillis(1);
    node.touch(clock);
    
    assertEquals(1, node.compareTo(other));
  }

}
