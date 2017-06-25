package org.sapia.ubik.mcast.bou;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.bou.BroadcastOverUnicastDispatcher.ViewCallback;
import org.sapia.ubik.mcast.memory.InMemoryUnicastDispatcher;
import org.sapia.ubik.net.ServerAddress;

public abstract class BroadcastOverUnicastDispatcherTestSupport {

  private static final String ASYNC_EVENT_TYPE = "async";

  protected EventConsumer sourceConsumer;
  protected EventConsumer domainConsumer;
  protected EventConsumer nonDomainConsumer;

  protected BroadcastDispatcher source;
  protected BroadcastDispatcher domainDestination;
  protected BroadcastDispatcher nonDomainDestination;
  
  protected Map<String, ServerAddress> addressesByNode;


  @Before
  public void setUp() throws Exception {
    addressesByNode = new HashMap<>();
    doSetup();
    
    sourceConsumer = new EventConsumer("broadcast/01");
    domainConsumer = new EventConsumer("broadcast/01");
    nonDomainConsumer = new EventConsumer("broadcast/02");
    
    source = createDispatcher(sourceConsumer, createViewCallback(sourceConsumer));
    domainDestination = createDispatcher(domainConsumer, createViewCallback(domainConsumer));
    nonDomainDestination = createDispatcher(nonDomainConsumer, createViewCallback(nonDomainConsumer));

    source.start();
    domainDestination.start();
    nonDomainDestination.start();
  }
  
  @After
  public void tearDown() throws Exception {
    doTearDown();
    if (source != null) source.close();
    if (domainDestination != null) domainDestination.close();
    if (nonDomainDestination != null) nonDomainDestination.close();
  }
  
  private ViewCallback createViewCallback(final EventConsumer consumer) {
   return new ViewCallback() {
      
      @Override
      public void register(String eventType, AsyncEventListener listener) {
        consumer.registerAsyncListener(eventType, listener);
      }
      
      @Override
      public void notifyAsyncListeners(RemoteEvent event) {
        consumer.onAsyncEvent(event);
      }
      
      @Override
      public String getNode() {
        return consumer.getNode();
      }
      
      @Override
      public DomainName getDomainName() {
        return consumer.getDomainName();
      }
      
      @Override
      public ServerAddress getAddressFor(String node) {
        return addressesByNode.get(node);
      }
      
      @Override
      public Set<String> getOtherNodes() {
        return new HashSet<>(addressesByNode.keySet());
      }
    };
    
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
    
    source.dispatch(new InMemoryUnicastDispatcher.InMemoryUnicastAddress(), DomainName.parse("broadcast").toString(),
        ASYNC_EVENT_TYPE, "test");

    String domainResponseData = domainResponse.await(3000);
    String nonDomainResponseData = nonDomainResponse.await(3000);
    assertTrue("domain response not set", domainResponseData != null);
    assertTrue("non-domain response not set", nonDomainResponseData != null);

  }

  protected abstract BroadcastDispatcher createDispatcher(EventConsumer consumer, ViewCallback view) throws IOException;

}
