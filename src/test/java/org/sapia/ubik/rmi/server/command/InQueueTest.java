package org.sapia.ubik.rmi.server.command;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;

public class InQueueTest {
  
  private BlockingRef<List<Response>> responseRef;
  private OutqueueManager manager;
  private InQueue         inQueue;

  @Before
  public void setUp() throws Exception {
    
    responseRef = new BlockingRef<List<Response>>();
    manager = new OutqueueManager(new ResponseSenderFactory() {
      @Override
      public ResponseSender getResponseSenderFor(Destination destination) {
        return new ResponseSender() {
          @Override
          public void sendResponses(Destination destination, List<Response> responses) {
            responseRef.set(responses);
          }
        };
      }
    }, OutqueueManager.DEFAULT_OUTQUEUE_THREADS);
    inQueue = new InQueue(1, manager);
  }
  
  @Test
  public void testProcessCommand() throws Exception {
    
    inQueue.add(new AsyncCommand(1, VmId.getInstance(), new InMemoryAddress("test"), new Command() {
      @Override
      public Object execute() throws Throwable {
        return "TEST";
      }
    }));
    
    assertEquals("Expected 1 response", 1, responseRef.await(10000).size());
  }

}
