package org.sapia.ubik.mcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingCompletionQueue;

/**
 * @author Yanick Duchesne
 * 
 */
public class EventConsumerTest {

  @Test
  public void testMatchesAll() throws Exception {
    EventConsumer cons = new EventConsumer("123", "default");
    DomainName other = DomainName.parse("local");
    DomainName thisDomain = DomainName.parse("default");
    assertTrue(!cons.matchesAll(other, "456"));
    assertTrue(!cons.matchesAll(thisDomain, "456"));
    assertTrue(cons.matchesAll(null, "456"));
    assertTrue(!cons.matchesAll(null, "123"));
  }

  @Test
  public void testMatchesThis() throws Exception {
    EventConsumer cons = new EventConsumer("123", "default");
    DomainName other = DomainName.parse("local");
    DomainName thisDomain = DomainName.parse("default");
    assertTrue(!cons.matchesThis(other, "456"));
    assertTrue(cons.matchesThis(thisDomain, "456"));
    assertTrue(!cons.matchesThis(thisDomain, "123"));
  }

  @Test
  public void testRegisterAsyncListener() throws Exception {
    EventConsumer cons = new EventConsumer("node", "domain");
    TestEventListener listener = new TestEventListener();
    cons.registerAsyncListener("test", listener);
    assertTrue("Should contain AsyncEventListener", cons.containsAsyncListener(listener));
  }
  
  @Test
  public void testUnregisterAsyncListener() throws Exception {
    EventConsumer cons = new EventConsumer("node", "domain");
    TestEventListener listener = new TestEventListener();
    cons.registerAsyncListener("test", listener);
    cons.unregisterListener((AsyncEventListener) listener);
    assertTrue("Should contain AsyncEventListener that has been removed", !cons.containsAsyncListener(listener));
  }

  @Test
  public void testRegisterSyncListener() throws Exception {
    EventConsumer cons = new EventConsumer("node", "domain");
    SyncEventListener listener = new TestEventListener();
    cons.registerSyncListener("test", listener);
    assertTrue("Should contain AsyncEventListener", cons.containsSyncListener(listener));
  }
  
  @Test
  public void testUnregisterSyncListener() throws Exception {
    EventConsumer cons = new EventConsumer("node", "domain");
    SyncEventListener listener = new TestEventListener();
    cons.registerSyncListener("test", listener);
    cons.unregisterListener(listener);
    assertTrue("Should not contain SyncEventListener", !cons.containsSyncListener(listener));
    assertEquals("Listener count should be 0", 0, cons.getListenerCount());
  }

  @Test
  public void testOnAsyncEvent() throws Exception {
    EventConsumer cons = new EventConsumer("node", "domain");
    final BlockingCompletionQueue<String> queue = new BlockingCompletionQueue<String>(5);
    for (int i = 0; i < queue.getExpectedCount(); i++) {
      cons.registerAsyncListener("test", new AsyncEventListener() {
        @Override
        public void onAsyncEvent(RemoteEvent evt) {
          queue.add("ASYNC_LISTENER_RESPONSE");
        }
      });
    }
    cons.onAsyncEvent(new RemoteEvent("test", "TEST").setNode("123"));
    assertEquals("Expected " + queue.getExpectedCount() + " listeners to have been notified", queue.getExpectedCount(), queue.await(3000).size());
  }

  @Test
  public void testOnSyncEvent() throws Exception {
    EventConsumer cons = new EventConsumer("node", "domain");
    cons.registerSyncListener("test", new SyncEventListener() {
      @Override
      public Object onSyncEvent(RemoteEvent evt) {
        return "SYNC_LISTENER_RESPONSE";
      }
    });

    Object response = cons.onSyncEvent(new RemoteEvent("test", "TEST").setNode("123"));
    assertTrue("SyncEventListener was not notified", response != null);
  }
}
