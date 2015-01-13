package org.sapia.ubik.mcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class ViewTest {

  private View view;
  
  @Mock
  private EventChannelStateListener listener;

  @Before
  public void setUp() throws Exception {
    view = new View();
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
    view.heartbeatResponse(new TCPAddress("test", "test", 1), "123");

    verify(listener).onUp(any(EventChannelEvent.class));
  }

  @Test
  public void testEventChannelStateListenerOnDown() throws Exception {
    view.addEventChannelStateListener(listener);
    view.heartbeatResponse(new TCPAddress("test", "test", 1), "123");
    view.removeDeadNode("123");
 
    verify(listener).onDown(any(EventChannelEvent.class));
  }
  
  @Test
  public void testEventChannelStateListenerOnLeft() throws Exception {
    view.addEventChannelStateListener(listener);
    view.heartbeatResponse(new TCPAddress("test", "test", 1), "123");
    view.removeLeavingNode("123");

    verify(listener).onLeft(any(EventChannelEvent.class));
  }

  @Test
  public void testRemoveEventChannelStateListener() {

    view.addEventChannelStateListener(listener);
    assertTrue("EventChannelStateListener was not removed", view.removeEventChannelStateListener(listener));
    view.heartbeatResponse(new TCPAddress("test", "test", 1), "123");
    
    verify(listener, never()).onUp(any(EventChannelEvent.class));
  }
  
  @Test
  public void testUpdateView() {
    view.addHost(mock(ServerAddress.class), "1");
    view.addHost(mock(ServerAddress.class), "2");
    view.addHost(mock(ServerAddress.class), "3");

    EventChannelStateListener listener = mock(EventChannelStateListener.class);
    view.addEventChannelStateListener(listener);
    
    List<NodeInfo> actual = new ArrayList<>();
    actual.add(new NodeInfo(mock(ServerAddress.class), "1"));
    actual.add(new NodeInfo(mock(ServerAddress.class), "2"));
    actual.add(new NodeInfo(mock(ServerAddress.class), "4"));

    view.update(actual);
    
    verify(listener).onDown(any(EventChannelEvent.class));
  }

  @Test
  public void testGetAddressFor() {
    TCPAddress addr = new TCPAddress("test", "test", 1);
    view.addHost(addr, "123");
    assertEquals("No address found for node", addr, view.getAddressFor("123"));
  }

}
