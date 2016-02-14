package org.sapia.ubik.mcast.control.gossip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.EventChannelFacade;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.IntRange;
import org.sapia.ubik.util.SysClock.MutableClock;

@RunWith(MockitoJUnitRunner.class)
public class GossipSyncNotificationHandlerTest {

  @Mock
  private EventChannelFacade facade;
  
  private MutableClock clock;
  
  private ControllerContext context;
  
  private GossipSyncNotificationHandler handler;
  
  private NodeInfo originNode;
  
  private GossipSyncNotification event;
  
  
  @Before
  public void setUp() throws Exception {
    clock = MutableClock.getInstance();
    context = new ControllerContext(facade, clock, new ControllerConfiguration());
    handler = new GossipSyncNotificationHandler(context);
    
    originNode = new NodeInfo(new TCPAddress("test", "host", 0), "origin-node");
    
    event = new GossipSyncNotification(
        new IntRange(1, 5).transform(new Func<NodeInfo, Integer>() {
          @Override
          public NodeInfo call(Integer i) {
            return new NodeInfo(new TCPAddress("test", "host", i), "node-" + i);
          }
        })
    );
    
    when(facade.getNode()).thenReturn("local-node");
    
    when(facade.getView(any(Condition.class))).thenReturn(
        new IntRange(3, 7).transform(new Func<NodeInfo, Integer>() {
          @Override
          public NodeInfo call(Integer i) {
            return new NodeInfo(new TCPAddress("test", "host", i), "node-" + i);
          }
        })
    );
  }

  @Test
  public void testHandle() {
    handler.handle(originNode.getNode(), originNode.getAddr(), event);
    
    for (NodeInfo n : event.getView()) {
      verify(facade).addNewNode(n.getNode(), n.getAddr());
    }
    
    verify(facade).heartbeat(originNode.getNode(), originNode.getAddr());
    
    ArgumentCaptor<GossipSyncAckControlEvent> notif = ArgumentCaptor.forClass(GossipSyncAckControlEvent.class);
    verify(facade).sendUnicastEvent(any(ServerAddress.class), notif.capture());
    
    GossipSyncAckControlEvent ack = notif.getValue();
    assertEquals(2, ack.getView().size());
    assertTrue(ack.getView().contains(new NodeInfo(new TCPAddress("test", "host", 6), "node-" + 6)));
    assertTrue(ack.getView().contains(new NodeInfo(new TCPAddress("test", "host", 7), "node-" + 7)));
  }

  @Test
  public void testHandle_local_node() {
    event.getView().add(new NodeInfo(new TCPAddress("test", "host", 100), "local-node"));
    
    handler.handle(originNode.getNode(), originNode.getAddr(), event);
    
    for (NodeInfo n : event.getView()) {
      if (n.getNode().equals("local-node")) {
        verify(facade, never()).addNewNode(n.getNode(), n.getAddr());
      } else {
        verify(facade).addNewNode(n.getNode(), n.getAddr());
      }
    }
    
    verify(facade).heartbeat(originNode.getNode(), originNode.getAddr());
    
    ArgumentCaptor<GossipSyncAckControlEvent> notif = ArgumentCaptor.forClass(GossipSyncAckControlEvent.class);
    verify(facade).sendUnicastEvent(any(ServerAddress.class), notif.capture());
    
    GossipSyncAckControlEvent ack = notif.getValue();
    assertEquals(2, ack.getView().size());
    assertTrue(ack.getView().contains(new NodeInfo(new TCPAddress("test", "host", 6), "node-" + 6)));
    assertTrue(ack.getView().contains(new NodeInfo(new TCPAddress("test", "host", 7), "node-" + 7)));
  }
}
