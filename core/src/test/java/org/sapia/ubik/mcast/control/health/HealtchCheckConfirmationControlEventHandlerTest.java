package org.sapia.ubik.mcast.control.health;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.EventChannelFacade;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.UbikMetrics;
import org.sapia.ubik.util.SysClock.MutableClock;

@RunWith(MockitoJUnitRunner.class)
public class HealtchCheckConfirmationControlEventHandlerTest {

  @Mock
  private EventChannelFacade facade;
  
  private MutableClock clock;
  
  private ControllerContext context;
  
  private HealtchCheckConfirmationControlEventHandler handler;
  
  private NodeInfo originNode, suspectNode;
  
  private HealthCheckConfirmationControlEvent upEvent, downEvent;
  
  @Before
  public void setUp() throws Exception {
    clock = MutableClock.getInstance();

    context = new ControllerContext(facade, clock, new ControllerConfiguration(), new UbikMetrics());
    
    originNode = new NodeInfo(new TCPAddress("test", "host", 0), "origin-node");
    suspectNode = new NodeInfo(new TCPAddress("test", "host", 1), "suspect-node").suspect().touch(clock);

    
    upEvent   = new HealthCheckConfirmationControlEvent(suspectNode, true);
    downEvent = new HealthCheckConfirmationControlEvent(suspectNode, false);

    handler = new HealtchCheckConfirmationControlEventHandler(context);
    
    
    when(facade.getNodeInfoFor(suspectNode.getNode())).thenReturn(suspectNode);
    when(facade.getNode()).thenReturn("local-node");
  }

  @Test
  public void testHandle_up() {
    clock.increaseCurrentTimeMillis(1000);
    handler.handle(originNode.getNode(), originNode.getAddr(), upEvent);
   
    assertEquals(NodeInfo.State.NORMAL, suspectNode.getState());
    assertEquals(1000L, suspectNode.getTimestamp());
  }

  @Test
  public void testHandle_down() {
    clock.increaseCurrentTimeMillis(1000);
    handler.handle(originNode.getNode(), originNode.getAddr(), downEvent);
   
    assertEquals(NodeInfo.State.SUSPECT, suspectNode.getState());
    assertEquals(0L, suspectNode.getTimestamp());
    verify(facade).down(suspectNode.getNode());
  }
}
