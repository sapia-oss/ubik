package org.sapia.ubik.rmi.server.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CallbackResponseQueueTest {

  private CallbackResponseQueue queue;

  @Before
  public void setUp() throws Exception {
    queue = new CallbackResponseQueue();
  }

  @Test
  public void testShutdown() {
  }

  @Test
  public void testCreateResponseLock() {
    queue.createResponseLock();
    assertEquals(1, queue.size());
  }

  @Test
  public void testOnResponses() throws InterruptedException {
    ResponseLock lock = queue.createResponseLock();
    Response res = new Response(lock.getId(), "Response");
    queue.onResponse(res);
    assertEquals("Response", lock.await(100));
    assertEquals("Response lock not removed from queue", 0, queue.size());
  }

}
