package org.sapia.ubik.mcast.control.health;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControllerConfiguration;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.EventChannelFacade;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.SysClock.MutableClock;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.UbikMetrics;

@RunWith(MockitoJUnitRunner.class)
public class DelegatedHealthCheckControlEventHandlerTest {

  @Mock
  private ExecutorService executor;
    
  @Mock
  private EventChannelFacade facade;
  
  
  private MutableClock clock;
  
  private ControllerContext context;
  
  private DelegatedHealthCheckControlEventHandler handler;
  
  private NodeInfo originNode;
  
  private DelegatedHealthCheckControlEvent event;
  
  
  @Before
  public void setUp() throws Exception {
    clock = MutableClock.getInstance();
    context = new ControllerContext(facade, clock, new ControllerConfiguration(), new UbikMetrics());
    handler = new DelegatedHealthCheckControlEventHandler(context);
    
    originNode = new NodeInfo(new TCPAddress("test", "host", 0), "origin-node");
    
    event = new DelegatedHealthCheckControlEvent(new NodeInfo(new TCPAddress("test", "test-host", 1), "test-node"));
 
    when(facade.getAsyncIoExecutor()).thenReturn(executor);
    when(facade.getNode()).thenReturn("local-node");
    doAnswer(new Answer<Void>() {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            invocation.getArgumentAt(0,  Runnable.class).run();
            return null;
        }
    }).when(executor).submit(any(Runnable.class));
  }

  @Test
  public void testHandle_success_response() throws Exception {
    Set<SynchronousControlResponse> responses = new HashSet<>();
    responses.add(new SynchronousHealthCheckResponse("localNode", mock(ServerAddress.class)));
    
    when(facade.sendSynchronousRequest(anySetOf(String.class), any(SynchronousHealthCheckRequest.class), any(TimeValue.class)))
    .thenReturn(responses);
    
    handler.handle(originNode.getNode(), originNode.getAddr(), event);
    
    verify(facade).heartbeat(originNode.getNode(), originNode.getAddr());
    
    ArgumentCaptor<HealthCheckConfirmationControlEvent> captor = ArgumentCaptor.forClass(HealthCheckConfirmationControlEvent.class);
    verify(facade).sendUnicastEvent(isA(ServerAddress.class), captor.capture());
   
    HealthCheckConfirmationControlEvent sent = captor.getValue();
    assertTrue(sent.isUp());
  }
  
  @Test
  public void testHandle_empty_response() throws Exception {
    when(facade.sendSynchronousRequest(anySetOf(String.class), any(SynchronousHealthCheckRequest.class), any(TimeValue.class)))
    .thenReturn(new HashSet<SynchronousControlResponse>());
    
    handler.handle(originNode.getNode(), originNode.getAddr(), event);
    
    verify(facade).heartbeat(originNode.getNode(), originNode.getAddr());
    
    ArgumentCaptor<HealthCheckConfirmationControlEvent> captor = ArgumentCaptor.forClass(HealthCheckConfirmationControlEvent.class);
    verify(facade).sendUnicastEvent(isA(ServerAddress.class), captor.capture());
   
    HealthCheckConfirmationControlEvent sent = captor.getValue();
    assertFalse(sent.isUp());
  }
  
  @Test
  public void testHandle_error_response() throws Exception {
    when(facade.sendSynchronousRequest(anySetOf(String.class), any(SynchronousHealthCheckRequest.class), any(TimeValue.class)))
    .thenThrow(new IOException("I/O error"));
    
    handler.handle(originNode.getNode(), originNode.getAddr(), event);
    
    verify(facade).heartbeat(originNode.getNode(), originNode.getAddr());
    
    ArgumentCaptor<HealthCheckConfirmationControlEvent> captor = ArgumentCaptor.forClass(HealthCheckConfirmationControlEvent.class);
    verify(facade).sendUnicastEvent(isA(ServerAddress.class), captor.capture());
   
    HealthCheckConfirmationControlEvent sent = captor.getValue();
    assertFalse(sent.isUp());
  }

}
