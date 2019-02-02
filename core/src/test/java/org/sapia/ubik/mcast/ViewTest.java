package org.sapia.ubik.mcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.throttle.NullThrottle;

@RunWith(MockitoJUnitRunner.class)
public class ViewTest {

  private SysClock.MutableClock clock;
  private View view;
  
  @Mock
  private EventChannelStateListener listener;

  @Before
  public void setUp() throws Exception {
    clock = SysClock.MutableClock.getInstance();
    view = new View(clock, "test", () -> new NullThrottle());
  }
  
  @Test
  public void testContainsNode() {
    view.addHost(new TCPAddress("test", "test", 1), "123");    
    assertTrue(view.containsNode("123"));
  }
  
  @Test
  public void testHeartbeat_first() {
    view.heartbeat(new TCPAddress("test", "test", 1), "123", clock);
    assertTrue(view.containsNode("123"));
  }

  @Test
  public void testHeartbeat_subsequent() {
    clock.increaseCurrentTimeMillis(100);
    view.heartbeat(new TCPAddress("test", "test", 1), "123", clock);
    
    assertTrue(view.containsNode("123"));
    assertEquals(100L, view.getNodeInfo("123").getTimestamp());
  }
  
  @Test
  public void testHeartbeat_fromDeadNode() {
    view.addHost(new TCPAddress("test", "test", 1), "123");    
    view.removeDeadNode("123");
    assertFalse(view.containsNode("123"));
    assertTrue(view.isNodeDead("123"));

    view.heartbeat(new TCPAddress("test", "test", 1), "123", clock);
    assertTrue(view.containsNode("123"));
    assertFalse(view.isNodeDead("123"));
  }

  @Test
  public void testRemoveDeadNode() {
    view.addHost(new TCPAddress("test", "test", 1), "123");    
    view.removeDeadNode("123");
    assertFalse(view.containsNode("123"));
    assertTrue(view.isNodeDead("123"));
  }
  
  @Test
  public void testGetNode() {
    view.addHost(new TCPAddress("test", "test", 1), "123");    
    assertNotNull(view.getNodeInfo("123"));
  }
  
  @Test
  public void testEventChannelStateListenerOnUpWithNewHost() {
    view.addEventChannelStateListener(listener);
    view.addHost(new TCPAddress("test", "test", 1), "123");

    verify(listener).onUp(any(EventChannelEvent.class));
  }

  @Test
  public void testEventChannelStateListenerOnUpWithHeartbeat() {
    view.addEventChannelStateListener(listener);
    view.heartbeat(new TCPAddress("test", "test", 1), "123", clock);

    verify(listener).onUp(any(EventChannelEvent.class));
  }

  @Test
  public void testEventChannelStateListenerOnDown() throws Exception {
    view.addEventChannelStateListener(listener);
    view.heartbeat(new TCPAddress("test", "test", 1), "123", clock);
    view.removeDeadNode("123");
 
    verify(listener).onDown(any(EventChannelEvent.class));
  }
  
  @Test
  public void testEventChannelStateListenerOnLeft() throws Exception {
    view.addEventChannelStateListener(listener);
    view.heartbeat(new TCPAddress("test", "test", 1), "123", clock);
    view.removeLeavingNode("123");

    verify(listener).onLeft(any(EventChannelEvent.class));
  }

  @Test
  public void testRemoveEventChannelStateListener() {

    view.addEventChannelStateListener(listener);
    assertTrue("EventChannelStateListener was not removed", view.removeEventChannelStateListener(listener));
    view.heartbeat(new TCPAddress("test", "test", 1), "123", clock);
    
    verify(listener, never()).onUp(any(EventChannelEvent.class));
  }
  
  @Test
  public void testGetFilteredNodes() {
    view.addHost(mock(ServerAddress.class), "1");
    view.addHost(mock(ServerAddress.class), "2");
    view.addHost(mock(ServerAddress.class), "3");
    
    List<NodeInfo> hosts = view.getNodeInfos(new Condition<NodeInfo>() {
      @Override
      public boolean apply(NodeInfo host) {
        return host.getNode().equalsIgnoreCase("3");
      }
    });
    
    assertEquals(1, hosts.size());
    assertEquals("3", hosts.get(0).getNode());
  }

  @Test
  public void testGetAddressFor() {
    TCPAddress addr = new TCPAddress("test", "test", 1);
    view.addHost(addr, "123");
    assertEquals("No address found for node", addr, view.getAddressFor("123"));
  }
  
  @Test
  public void testAddingDeadNode() {
    view.addHost(new TCPAddress("test", "test", 1), "123");    
    view.removeDeadNode("123");
    assertFalse(view.containsNode("123"));
    assertTrue(view.isNodeDead("123"));

    boolean actual = view.addHost(new TCPAddress("test", "test", 1), "123");
    assertFalse(actual);
    assertFalse(view.containsNode("123"));
    assertTrue(view.isNodeDead("123"));
  }

  @Test
  public void testCleanupDeadNodeList_inGracePeriod() {
    view.addHost(new TCPAddress("test", "test", 1), "123");    
    clock.increaseCurrentTimeMillis(100);
    view.removeDeadNode("123");
    assertTrue(view.isNodeDead("123"));
    
    clock.increaseCurrentTimeMillis(100);
    view.cleanupDeadNodeList(5000);
    assertTrue(view.isNodeDead("123"));
  }

  @Test
  public void testCleanupDeadNodeList_afterGracePeriod() {
    view.addHost(new TCPAddress("test", "test", 1), "123");    
    clock.increaseCurrentTimeMillis(100);
    view.removeDeadNode("123");
    assertTrue(view.isNodeDead("123"));
    
    clock.increaseCurrentTimeMillis(10000);
    view.cleanupDeadNodeList(5000);
    assertFalse(view.isNodeDead("123"));
  }
  
}
