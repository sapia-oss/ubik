package org.sapia.ubik.mcast.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.memory.InMemoryUnicastDispatcher;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.ExtendedProperties;

public abstract class BroadcastDispatcherTestSupport {

  private static final String ASYNC_EVENT_TYPE = "async";

  protected EventConsumer sourceConsumer;
  protected EventConsumer domainConsumer;
  protected EventConsumer nonDomainConsumer;
  protected EventConsumer allDomainConsumer;

  protected BroadcastDispatcher source;
  protected BroadcastDispatcher domainDestination;
  protected BroadcastDispatcher nonDomainDestination;
  protected BroadcastDispatcher allDomainDestination;

  @Before
  public void setUp() throws Exception {
    doSetup();
    Conf conf = new ExtendedProperties().setInt(Consts.MCAST_CONSUMER_MIN_COUNT, 1).setInt(Consts.MCAST_CONSUMER_MAX_COUNT, 10).toConf();
    source = createDispatcher(sourceConsumer = new EventConsumer("broadcast/01", conf));
    domainDestination = createDispatcher(domainConsumer = new EventConsumer("broadcast/01", conf));
    nonDomainDestination = createDispatcher(nonDomainConsumer = new EventConsumer("broadcast/02", conf));
    allDomainDestination = createDispatcher(allDomainConsumer = new EventConsumer("broadcast", conf));

    source.start();
    domainDestination.start();
    nonDomainDestination.start();
    allDomainDestination.start();
  }

  @After
  public void tearDown() throws Exception {
    doTearDown();
    if (source != null) source.close();
    if (domainDestination != null) domainDestination.close();
    if (nonDomainDestination != null) nonDomainDestination.close();
    if (allDomainDestination != null) allDomainDestination.close();
  }

  protected void doSetup() throws Exception {
  }
  
  protected void doTearDown() throws Exception {
  }

  @Test
  public void testDispatchToDomain() throws Exception {

    final BlockingRef<String> response = new BlockingRef<String>();

    domainConsumer.registerAsyncListener(ASYNC_EVENT_TYPE, new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        response.set("response");
      }
    });

    source.dispatch(new InMemoryUnicastDispatcher.InMemoryUnicastAddress(), sourceConsumer.getDomainName().toString(), ASYNC_EVENT_TYPE, "test");

    String responseData = response.await(3000);

    assertEquals("response", responseData);
  }

  @Test
  public void testDispatchToAllDomains() throws Exception {

    final BlockingRef<String> domainResponse = new BlockingRef<String>();
    final BlockingRef<String> nonDomainResponse = new BlockingRef<String>();
    final BlockingRef<String> allDomainResponse = new BlockingRef<String>();

    domainConsumer.registerAsyncListener(ASYNC_EVENT_TYPE, new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        domainResponse.set("response");
      }
    });

    nonDomainConsumer.registerAsyncListener(ASYNC_EVENT_TYPE, new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        nonDomainResponse.set("response");
      }
    });

    allDomainConsumer.registerAsyncListener(ASYNC_EVENT_TYPE, new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        allDomainResponse.set("response");
      }
    });

    source.dispatch(new InMemoryUnicastDispatcher.InMemoryUnicastAddress(), true, ASYNC_EVENT_TYPE, "test");

    String domainResponseData = domainResponse.await(3000);
    String nonDomainResponseData = nonDomainResponse.await(3000);
    String allDomainResponseData = allDomainResponse.await(3000);
    assertTrue("domain response not set", domainResponseData != null);
    assertTrue("non-domain response not set", nonDomainResponseData != null);
    assertTrue("all-domain response not set", allDomainResponseData != null);
  }

  @Test
  public void testDispatchWithDomainPartitioning() throws Exception {

    final BlockingRef<String> domainResponse = new BlockingRef<String>();
    final BlockingRef<String> nonDomainResponse = new BlockingRef<String>();

    domainConsumer.registerAsyncListener(ASYNC_EVENT_TYPE, new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        domainResponse.set("response");
      }
    });

    nonDomainConsumer.registerAsyncListener(ASYNC_EVENT_TYPE, new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        nonDomainResponse.set("response");
      }
    });

    allDomainDestination.dispatch(new InMemoryUnicastDispatcher.InMemoryUnicastAddress(), allDomainConsumer.getDomainName().toString(),
        ASYNC_EVENT_TYPE, "test");

    String domainResponseData = domainResponse.await(3000);
    String nonDomainResponseData = nonDomainResponse.await(3000);
    assertTrue("domain response not set", domainResponseData != null);
    assertTrue("non-domain response not set", nonDomainResponseData != null);

  }

  protected abstract BroadcastDispatcher createDispatcher(EventConsumer consumer) throws IOException;

}
