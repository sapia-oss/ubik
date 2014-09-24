package org.sapia.ubik.mcast.control.master;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ChannelCallback;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.EventChannelController;
import org.sapia.ubik.net.ServerAddress;

@RunWith(MockitoJUnitRunner.class)
public class MasterSyncEventHandlerTest {
  
  @Mock
  private ChannelCallback        callback;
  
  @Mock
  private ServerAddress          address;
  
  private EventChannelController controller;
  
  private MasterSyncEventHandler handler;   

  @Test
  public void testHandle() {
    controller = new EventChannelController(new ControllerConfiguration(), callback);
    handler    = new MasterSyncEventHandler(controller.getContext());
    
    MasterSyncEvent event = new MasterSyncEvent();
    handler.handle("test", address, event);
    
    verify(callback).updateView(anyListOf(NodeInfo.class));
  }

}
