package org.sapia.ubik.mcast.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingCompletionQueue;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.SyncEventListener;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.TimeValue;

public abstract class UnicastDispatcherTestSupport {

  private static final String SYNC_EVENT_TYPE = "sync";
  private static final String ASYNC_EVENT_TYPE = "async";

  private UnicastDispatcher source;
  private List<UnicastDispatcher> destinations;
  private BlockingCompletionQueue<String> queue;

  @Before
  public void setUp() throws Exception {
    Hub.start();
    source = createUnicastDispatcher(new EventConsumer("testDomain", 1));
    source.start();
    destinations = new ArrayList<UnicastDispatcher>(5);
    for (int i = 0; i < 5; i++) {
      EventConsumer consumer = new EventConsumer("testDomain", 1);
      UnicastDispatcher dispatcher = createUnicastDispatcher(consumer);
      dispatcher.start();
      consumer.registerAsyncListener(ASYNC_EVENT_TYPE, createAsyncListener("listener" + i));
      consumer.registerSyncListener(SYNC_EVENT_TYPE, createSyncEventListener("listener" + i));
      destinations.add(dispatcher);
    }

  }

  @After
  public void tearDown() throws Exception {
    Hub.shutdown();
    source.close();
    for (UnicastDispatcher dispatcher : destinations) {
      dispatcher.close();
    }
  }

  @Test
  public void testDispatch() throws Exception {
    Thread.sleep(2000);
    queue = new BlockingCompletionQueue<String>(1);
    source.dispatch(randomDestination(), ASYNC_EVENT_TYPE, "test");
    assertEquals(1, queue.await(2000).size());
  }

  @Test
  public void testSendToSelectedDestinations() throws Exception {
    queue = new BlockingCompletionQueue<String>(3);
    RespList responses = source.send(selectedDestinations(3), SYNC_EVENT_TYPE, "test", TimeValue.createMillis(2000));
    assertEquals("Expected 3 responses", 3, responses.count());
    for (int i = 0; i < responses.count(); i++) {
      Response r = responses.get(i);
      assertTrue("Expected data", r.getData() != null);
    }
  }

  @Test
  public void testSendToSingleDestination() throws Exception {
    queue = new BlockingCompletionQueue<String>(1);
    Response response = source.send(selectedDestinations(1).get(0), SYNC_EVENT_TYPE, "test", TimeValue.createMillis(2000));
    assertTrue(response.getData() != null);
  }

  private ServerAddress randomDestination() {
    return destinations.get(new Random().nextInt(destinations.size())).getAddress();
  }

  private List<ServerAddress> selectedDestinations(int count) {
    List<ServerAddress> destinationAddresses = new ArrayList<ServerAddress>(count);
    for (int i = 0; i < count; i++) {
      UnicastDispatcher dispatcher = destinations.get(i);
      destinationAddresses.add(dispatcher.getAddress());
    }
    return destinationAddresses;
  }

  private AsyncEventListener createAsyncListener(final String listenerId) {
    return new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        queue.add(listenerId);
      }
    };
  }

  private SyncEventListener createSyncEventListener(final String listenerId) {
    return new SyncEventListener() {
      @Override
      public Object onSyncEvent(RemoteEvent evt) {
        return listenerId;
      }
    };
  }

  protected abstract UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) throws IOException;

}
