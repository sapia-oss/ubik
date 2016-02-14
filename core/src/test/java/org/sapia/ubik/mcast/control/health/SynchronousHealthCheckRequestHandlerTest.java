package org.sapia.ubik.mcast.control.health;

import static org.junit.Assert.assertNotNull;

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
import org.sapia.ubik.util.SysClock.MutableClock;

@RunWith(MockitoJUnitRunner.class)
public class SynchronousHealthCheckRequestHandlerTest {
  
  @Mock
  private EventChannelFacade facade;
  
  private MutableClock clock;
  
  private ControllerContext context;

  private NodeInfo originNode;

  private SynchronousHealthCheckRequestHandler handler;
  
  @Before
  public void setUp() throws Exception {
    clock = MutableClock.getInstance();
    
    originNode = new NodeInfo(new TCPAddress("test", "host", 0), "origin-node");

    context = new ControllerContext(facade, clock, new ControllerConfiguration());
    
    handler = new SynchronousHealthCheckRequestHandler(context);
  }

  @Test
  public void testHandle() {
    assertNotNull(handler.handle(originNode.getNode(), originNode.getAddr(), new SynchronousHealthCheckRequest()));
  }

}
