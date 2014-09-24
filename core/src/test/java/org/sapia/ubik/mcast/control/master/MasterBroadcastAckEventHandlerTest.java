package org.sapia.ubik.mcast.control.master;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.control.ChannelCallback;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.EventChannelController;
import org.sapia.ubik.net.ServerAddress;

@RunWith(MockitoJUnitRunner.class)
public class MasterBroadcastAckEventHandlerTest {
  
  @Mock
  private ChannelCallback                callback;

  @Mock
  private ServerAddress                  address;
  
  private EventChannelController         controller;
  
  private MasterBroadcastAckEventHandler handler;
  
  @Before
  public void setUp() {
    controller = new EventChannelController(new ControllerConfiguration(), callback);
    handler    = new MasterBroadcastAckEventHandler(controller.getContext());
  }
  
  @Test
  public void testHandle_contains_node_false() {
    when(callback.containsNode(anyString())).thenReturn(false);
    controller = new EventChannelController(new ControllerConfiguration(), callback);
    handler    = new MasterBroadcastAckEventHandler(controller.getContext());
    handler.handle("test", address, new MasterBroadcastAckEvent());
    
    verify(callback).sendUnicastEvent(eq(address), any(MasterSyncEvent.class));
  }
  
  @Test
  public void testHandle_different_node_count() {
    when(callback.getNodeCount()).thenReturn(2);
    when(callback.containsNode(anyString())).thenReturn(true);
    controller = new EventChannelController(new ControllerConfiguration(), callback);
    handler    = new MasterBroadcastAckEventHandler(controller.getContext());
    handler.handle("test", address, new MasterBroadcastAckEvent(1));
    
    verify(callback).sendUnicastEvent(eq(address), any(MasterSyncEvent.class));
  }

  @Test
  public void testHandle_no_sync() {
    when(callback.getNodeCount()).thenReturn(1);
    when(callback.containsNode(anyString())).thenReturn(true);
    controller = new EventChannelController(new ControllerConfiguration(), callback);
    handler    = new MasterBroadcastAckEventHandler(controller.getContext());
    handler.handle("test", address, new MasterBroadcastAckEvent(1));
    
    verify(callback, never()).sendUnicastEvent(eq(address), any(MasterSyncEvent.class));
  }
}
