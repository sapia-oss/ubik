package org.sapia.ubik.mcast;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.TCPAddress;

public class ViewTest {

  private View view;
  
  @Before
  public void setUp() throws Exception {
    view = new View();
  }

  @Test
  public void testEventChannelStateListenerOnUpWithNewHost() {
    
    final AtomicBoolean onUp = new AtomicBoolean();
    EventChannelStateListener listener = new EventChannelStateListener() {
      public void onDown(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {}
      public void onUp(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {
        onUp.set(true);
      }
    };
    view.addEventChannelStateListener(listener);
    view.addHost(new TCPAddress("test", "test", 1), "123");
    assertTrue("EventChannelStateListener onUp() not called", onUp.get());
  }
  
  @Test
  public void testEventChannelStateListenerOnUpWithHeartbeat() {
    
    final AtomicBoolean onUp = new AtomicBoolean();
    EventChannelStateListener listener = new EventChannelStateListener() {
      public void onDown(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {}
      public void onUp(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {
        onUp.set(true);
      }
    };
    view.addEventChannelStateListener(listener);
    view.heartbeat(new TCPAddress("test", "test", 1), "123");
    assertTrue("EventChannelStateListener onUp() not called", onUp.get());
  }
  
  @Test
  public void testEventChannelStateListenerOnDown() throws Exception {
    final AtomicBoolean onDown = new AtomicBoolean();
    EventChannelStateListener listener = new EventChannelStateListener() {
      public void onDown(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {
        onDown.set(true);
      }
      public void onUp(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {

      }
    };
    view.addEventChannelStateListener(listener);
    view.heartbeat(new TCPAddress("test", "test", 1), "123");
    view.removeDeadNode("123");
    assertTrue("EventChannelStateListener onDown() not called", onDown.get());
  }

  @Test
  public void testRemoveEventChannelStateListener() {
    final AtomicBoolean notified = new AtomicBoolean();

    EventChannelStateListener listener = new EventChannelStateListener() {
      public void onDown(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {
        notified.set(true);
      }
      public void onUp(org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent event) {
        notified.set(true);
      }
    };
    view.addEventChannelStateListener(listener);
    assertTrue("EventChannelStateListener was not removed", view.removeEventChannelStateListener(listener));
    view.heartbeat(new TCPAddress("test", "test", 1), "123");
    assertTrue("Removed EventChannelStateListener should not have been notified", !notified.get());

  }

  @Test
  public void testGetAddressFor() {
    TCPAddress addr = new TCPAddress("test", "test", 1);
    view.addHost(addr, "123");
    assertEquals("No address found for node", addr, view.getAddressFor("123"));
  }

}
