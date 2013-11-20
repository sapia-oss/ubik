package org.sapia.ubik.rmi.server.command;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;

public class OutQueueTest {

  private Destination destination;
  private OutQueue queue;

  @Before
  public void setUp() throws Exception {
    destination = new Destination(new InMemoryAddress("test"), VmId.getInstance());
    queue = new OutQueue(Executors.newFixedThreadPool(2), destination);

  }

  @Test
  public void testAddResponseAndNotification() throws Exception {
    final BlockingRef<Destination> destinationRef = new BlockingRef<Destination>();
    final BlockingRef<List<Response>> responseListRef = new BlockingRef<List<Response>>();
    queue.addQueueListener(new OutQueue.OutQueueListener() {
      @Override
      public void onResponses(Destination destination, List<Response> responses) {
        destinationRef.set(destination);
        responseListRef.set(responses);
      }
    });

    Response res = new Response(1, "response");
    try {
      queue.add(res);
      assertEquals(1, responseListRef.await(3000).size());
    } catch (RejectedExecutionException e) {
      // noop
    }
  }

  @Test
  public void testShutdown() throws InterruptedException {
    queue.shutdown(1000);
    assertTrue(queue.isShutdown());
  }
}
