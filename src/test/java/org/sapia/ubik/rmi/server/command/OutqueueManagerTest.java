package org.sapia.ubik.rmi.server.command;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;

public class OutqueueManagerTest {
  
  private ResponseSender  sender;
  private OutqueueManager manager;

  @Before
  public void setUp() throws Exception {
    sender        = mock(ResponseSender.class);
    manager       = new OutqueueManager(new ResponseSenderFactory() {
      @Override
      public ResponseSender getResponseSenderFor(Destination destination) {
        return sender;
      }
    },
    OutqueueManager.DEFAULT_OUTQUEUE_THREADS);
  }

  @Test
  public void testGetQueueFor() {
    Destination dest  = new Destination(new InMemoryAddress("test"), VmId.getInstance());
    OutQueue    queue = manager.getQueueFor(dest);
    assertEquals("Queue was not cached", queue, manager.getQueueFor(dest));
  }

  @Test
  public void testShutdown() throws Exception {
    Destination dest  = new Destination(new InMemoryAddress("test"), VmId.getInstance());
    manager.getQueueFor(dest);    
    manager.shutdown(1000);
    assertTrue("Queue should be shut down", manager.getQueueFor(dest).isShutdown());
  }
  
  static class TestResponseSenderFactory implements ResponseSenderFactory {
    
    @Override
    public ResponseSender getResponseSenderFor(Destination destination) {
      return null;
    }
  }
}
