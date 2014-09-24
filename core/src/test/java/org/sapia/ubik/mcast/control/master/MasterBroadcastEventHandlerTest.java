package org.sapia.ubik.mcast.control.master;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ChannelCallback;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.EventChannelController;
import org.sapia.ubik.mcast.control.challenge.ChallengeRequest;
import org.sapia.ubik.net.ServerAddress;

@RunWith(MockitoJUnitRunner.class)
public class MasterBroadcastEventHandlerTest {
  
  @Mock
  private ChannelCallback            callback;

  @Mock
  private ServerAddress              address;
  
  private EventChannelController     controller;

  private MasterBroadcastEventHandler handler;
  
  @Before
  public void setUp() {
    controller = new EventChannelController(new ControllerConfiguration(), callback);
    handler    = new MasterBroadcastEventHandler(controller.getContext());
  }
  
  @Test
  public void testHandle_current_role_is_master() {
    controller.getContext().setMasterNode(null);
    controller.getContext().setRole(Role.MASTER);
    handler.handle("test", address, new MasterBroadcastEvent(1));
    
    verify(callback).sendRequest(any(ChallengeRequest.class));
  }
  
  @Test
  public void testHandle_add_new_node() {
    controller.getContext().setMasterNode("test");
    controller.getContext().setRole(Role.SLAVE);
    handler.handle("test", address, new MasterBroadcastEvent(1));
    
    verify(callback).sendUnicastEvent(eq(address), any(MasterBroadcastAckEvent.class));
  }
  
  @Test
  public void testHandle_different_master() {
    controller.getContext().setMasterNode("test2");
    controller.getContext().setRole(Role.SLAVE);
    handler.handle("test", address, new MasterBroadcastEvent(1));
    
    verify(callback).sendUnicastEvent(eq(address), any(MasterBroadcastAckEvent.class));
  }

  
  @Test
  public void testHandle_different_node_count() {
    when(callback.getNodeCount()).thenReturn(2);
    controller.getContext().setMasterNode("test");
    controller.getContext().setRole(Role.SLAVE);
    handler.handle("test", address, new MasterBroadcastEvent(1));
    
    verify(callback).sendUnicastEvent(eq(address), any(MasterBroadcastAckEvent.class));
  }
  
  @Test
  public void testHandle_no_ack() {
    when(callback.getNodeCount()).thenReturn(1);
    controller.getContext().setMasterNode("test");
    controller.getContext().setRole(Role.SLAVE);
    handler.handle("test", address, new MasterBroadcastEvent(1));
    
    verify(callback, never()).sendUnicastEvent(eq(address), any(MasterBroadcastAckEvent.class));
  }
}
