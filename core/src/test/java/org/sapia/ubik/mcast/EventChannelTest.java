package org.sapia.ubik.mcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.mcast.memory.InMemoryDispatchChannel;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.PropUtil;

public class EventChannelTest {

  private EventChannel source, destination, other;

  @Before
  public void setUp() {
    EventChannel.closeCachedChannels();
    InMemoryDispatchChannel.getInstance().clear();
    PropUtil.clearUbikSystemProperties();
  }

  @After
  public void tearDown() throws Exception {
    if (source != null)
      source.close();
    if (destination != null)
      destination.close();
    if (other != null)
      other.close();
  }

  @Test
  public void testEventChannelState() throws Exception {
    source = createEventChannel(1000, 2000);

    final BlockingRef<Boolean> isUpRef   = new BlockingRef<Boolean>();
    final BlockingRef<Boolean> isDownRef = new BlockingRef<Boolean>();
    
    EventChannelStateListener listener = new EventChannelStateListener() {
      @Override
      public void onDown(EventChannelEvent event) {
        isDownRef.set(true);
      }

      @Override
      public void onUp(EventChannelEvent event) {
        isUpRef.set(true);
      }
      
      @Override
      public void onLeft(EventChannelEvent event) {
      }
      
      @Override
      public void onHeartbeatRequest(EventChannelEvent event) {
      }
      
      @Override
      public void onHeartbeatResponse(EventChannelEvent event) {
      }
    };

    source.addEventChannelStateListener(listener);

    source.start();
    destination = createEventChannel(1000, 2000);
    destination.start();

    source.getView().awaitPeers(5, TimeUnit.SECONDS);
    
    Boolean isUp = isUpRef.await(10000);
    assertTrue("Destination not detected as up (event channel listener not called)", isUp != null);
    assertTrue("Destination not detected as up", isUp);

    destination.close();

    Boolean isDown = isDownRef.await(10000);
    assertTrue("Destination not detected as down (event channel listener not called)", isDown != null);
    assertTrue("Destination not detected as down", isDown);
  }
  
  @Test
  public void testEventChannelDomainChange() throws Exception {
    source      = createEventChannel(1000, 2000);
    destination = createEventChannel(1000, 2000);
    other       = createEventChannel("test2", 1000, 2000);
    
    System.out.println("source : " + source.getNode());
    System.out.println("destination : " + destination.getNode());
    System.out.println("other : " + other.getNode());
    
    final BlockingRef<Boolean> joinedOtherDomain = new BlockingRef<Boolean>();
    final BlockingRef<Boolean> leftOldDomain     = new BlockingRef<Boolean>();
    
    EventChannelStateListener otherDomainListener = new EventChannelStateListener.Adapter() {
      @Override
      public void onUp(EventChannelEvent event) {
        System.out.println("Joining!!!!!! " + event.getNode());
        joinedOtherDomain.set(true);
      }
    };
    other.addEventChannelStateListener(otherDomainListener);

    EventChannelStateListener destinationListener = new EventChannelStateListener.Adapter() {
      @Override
      public void onLeft(EventChannelEvent event) {
        System.out.println("Leaving!!!!!! " + event.getNode());
        leftOldDomain.set(true);
      }
    };
    destination.addEventChannelStateListener(destinationListener);
    
    source.start();
    other.start();
    destination.start();
    
    source.getView().awaitPeers(5, TimeUnit.SECONDS);
    destination.getView().awaitPeers(5, TimeUnit.SECONDS);
    
    source.changeDomain("test2");
    
    other.getView().awaitPeers(5, TimeUnit.SECONDS);

    Boolean joined = joinedOtherDomain.await(10000);
    assertTrue("Other domain node did not discover new node", joined != null && joined);
    
    Boolean left   = leftOldDomain.await(10000);
    assertTrue("Did not leave old domain", left != null && left);
  }

  @Test
  public void testRemoveEventChannelStateListener() throws Exception {
    source = createEventChannel();
    source.start();
    EventChannelStateListener listener = mock(EventChannelStateListener.class);
    source.addEventChannelStateListener(listener);
    assertTrue("EventChannelStateListener not removed", source.removeEventChannelStateListener(listener));
  }

  @Test
  public void testDiscoveryListener() throws Exception {
    source = createEventChannel();
    source.start();

    final BlockingRef<Boolean> discoveryRef = new BlockingRef<Boolean>();

    source.addDiscoveryListener(new DiscoveryListener() {
      @Override
      public void onDiscovery(ServerAddress addr, RemoteEvent evt) {
        discoveryRef.set(true);
      }
    });

    destination = createEventChannel();
    destination.start();
    
    source.getView().awaitPeers(5, TimeUnit.SECONDS);

    Boolean discovered = discoveryRef.await(3000);
    assertTrue("No discovery occurred", discovered != null);
  }

  @Test
  public void testRemoveDiscoveryListener() throws Exception {
    source = createEventChannel();
    source.start();
    DiscoveryListener listener = new DiscoveryListener() {
      @Override
      public void onDiscovery(ServerAddress addr, RemoteEvent evt) {
      }
    };
    source.addDiscoveryListener(listener);
    assertTrue("DiscoveryListener not removed", source.removeDiscoveryListener(listener));
  }

  @Test
  public void testDispatchToDomain() throws Exception {
    source = createEventChannel();
    source.start();
    destination = createEventChannel();
    destination.start();
    
    EventChannel otherDestination = createEventChannel();
    otherDestination.start();
    
    source.getView().awaitPeers(5, TimeUnit.SECONDS, 2);

    final BlockingRef<RemoteEvent> eventRef = new BlockingRef<RemoteEvent>();
    final BlockingRef<RemoteEvent> otherEventRef = new BlockingRef<RemoteEvent>();

    destination.registerAsyncListener("testEvent", new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        eventRef.set(evt);
      }
    });

    otherDestination.registerAsyncListener("testEvent", new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        otherEventRef.set(evt);
      }
    });

    source.dispatch("testEvent", "TEST");

    RemoteEvent evt = eventRef.await(3000);
    assertTrue("Remote event not received by destination 1", evt != null);

    evt = otherEventRef.await(3000);
    assertTrue("Remote event not received by destination 2", evt != null);

    otherDestination.close();
  }

  @Test
  public void testDispatchToAllDomains() throws Exception {
    source = createEventChannel();
    source.start();
    destination = createEventChannel();
    destination.start();

    EventChannel otherDestination = createEventChannel("test2");
    otherDestination.start();
    
    source.getView().awaitPeers(5, TimeUnit.SECONDS, 2);

    final BlockingRef<RemoteEvent> eventRef = new BlockingRef<RemoteEvent>();
    final BlockingRef<RemoteEvent> otherEventRef = new BlockingRef<RemoteEvent>();

    destination.registerAsyncListener("testEvent", new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        eventRef.set(evt);
      }
    });

    otherDestination.registerAsyncListener("testEvent", new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        otherEventRef.set(evt);
      }
    });

    source.dispatch(true, "testEvent", "TEST");

    RemoteEvent evt = eventRef.await(3000);
    assertTrue("Remote event not received by destination 1", evt != null);

    evt = otherEventRef.await(3000);
    assertTrue("Remote event not received by destination 2", evt != null);

    otherDestination.close();

  }

  @Test
  public void testDispatchPointToPoint() throws Exception {
    source = createEventChannel();
    source.start();
    destination = createEventChannel();
    destination.start();

    source.getView().awaitPeers(5, TimeUnit.SECONDS, 2);
    
    final BlockingRef<RemoteEvent> eventRef = new BlockingRef<RemoteEvent>();
    destination.registerAsyncListener("testEvent", new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        eventRef.set(evt);
      }
    });

    source.dispatch(destination.getUnicastAddress(), "testEvent", "TEST");

    RemoteEvent evt = eventRef.await(3000);
    assertTrue("Remote event not received by destination", evt != null);

  }

  @Test
  public void testSendToSingleDestination() throws Exception {
    source = createEventChannel();
    source.start();
    destination = createEventChannel();
    destination.start();
    
    source.getView().awaitPeers(5, TimeUnit.SECONDS);
    
    destination.registerSyncListener("testEvent", new SyncEventListener() {
      @Override
      public Object onSyncEvent(RemoteEvent evt) {
        return "SINGLE_DESTINATION_RESPONSE";
      }
    });
    
    Response response = source.send(destination.getUnicastAddress(), "testEvent", "TEST");
    assertEquals("Response not returned", "SINGLE_DESTINATION_RESPONSE", response.getData());
  }

  @Test
  public void testSendToAllNodes() throws Exception {
    source = createEventChannel();
    source.start();
    
    final CountDownLatch latch = new CountDownLatch(5);

    List<EventChannel> destinations = new ArrayList<EventChannel>();
    List<SyncEventListener> syncListeners = new ArrayList<SyncEventListener>();

    final AtomicInteger eventCount = new AtomicInteger();
    for (int i = 0; i < 5; i++) {
      EventChannel destination = createEventChannel();

      SyncEventListener syncListener = new SyncEventListener() {
        @Override
        public Object onSyncEvent(RemoteEvent evt) {
          eventCount.incrementAndGet();
          latch.countDown();
          return "ALL_DESTINATIONS_RESPONSE";
        }
      };
      syncListeners.add(syncListener);
      destination.registerSyncListener("testEvent", syncListener);
      destinations.add(destination);
      destination.start();
    }
    
    source.getView().awaitPeers(5, TimeUnit.SECONDS, 5);
    
    for (EventChannel destination : destinations) {
      destination.getView().awaitPeers(5, TimeUnit.SECONDS);
    }
    
    RespList responses = source.send("testEvent", "TEST");

    latch.await(10000, TimeUnit.MILLISECONDS);
    
    for (EventChannel destination : destinations) {
      destination.close();
    }

    assertEquals("Invalid total number of events received", destinations.size(), eventCount.get());
    assertEquals("Responses are missing", destinations.size(), responses.count());

    for (int i = 0; i < responses.count(); i++) {
      Response resp = responses.get(i);
      assertEquals("Wrong response data", "ALL_DESTINATIONS_RESPONSE", resp.getData());
    }
  }

  @Test
  public void testSendToSelectedNodes() throws Exception {
    source = createEventChannel();
    source.start();

    List<EventChannel> destinations = new ArrayList<EventChannel>();
    List<SyncEventListener> syncListeners = new ArrayList<SyncEventListener>();
    
    final CountDownLatch latch = new CountDownLatch(3);
    
    for (int i = 0; i < 5; i++) {
      EventChannel destination = createEventChannel();
      destination.start();
      SyncEventListener syncListener = new SyncEventListener() {
        @Override
        public Object onSyncEvent(RemoteEvent evt) {
          return "SELECTED_DESTINATIONS_RESPONSE";
        }
      };
      syncListeners.add(syncListener);
      destination.registerSyncListener("testEvent", syncListener);
      destinations.add(destination);
    }
    
    source.getView().awaitPeers(5, TimeUnit.SECONDS, 5);
    
    for (EventChannel dest : destinations) {
      dest.getView().awaitPeers(5, TimeUnit.SECONDS);
    }

    List<EventChannel> selectedDestinations = destinations.subList(0, 3);
    List<ServerAddress> selectedAddresses = new ArrayList<ServerAddress>();
    for (EventChannel dest : selectedDestinations) {
      selectedAddresses.add(dest.getUnicastAddress());
    }

    RespList responses = source.send(selectedAddresses, "testEvent", "TEST");

    latch.await(10000, TimeUnit.MILLISECONDS);

    for (EventChannel destination : destinations) {
      destination.close();
    }

    assertEquals("Responses are missing", selectedDestinations.size(), responses.count());

    for (int i = 0; i < responses.count(); i++) {
      Response resp = responses.get(i);
      assertEquals("Wrong response data", "SELECTED_DESTINATIONS_RESPONSE", resp.getData());
    }
  }

  @Test
  public void testUnregisterAsyncEventListener() throws Exception {
    source = createEventChannel();

    AsyncEventListener listener = new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
      }
    };

    source.registerAsyncListener("test", listener);
    source.unregisterAsyncListener(listener);
    assertTrue(!source.containsAsyncListener(listener));
  }

  @Test
  public void testUnregisterSyncEventListener() throws Exception {
    source = createEventChannel();

    SyncEventListener listener = new SyncEventListener() {
      @Override
      public Object onSyncEvent(RemoteEvent evt) {
        return null;
      }
    };

    source.registerSyncListener("test", listener);
    source.unregisterSyncListener(listener);
    assertTrue(!source.containsSyncListener(listener));
  }

  private EventChannel createEventChannel() throws Exception {
    return createEventChannel("test");
  }

  private EventChannel createEventChannel(String domain) throws Exception {
    return createEventChannel(domain, 5000, 15000);
  }

  private EventChannel createEventChannel(long heartBeatInterval, long heartBeatTimeout) throws Exception {
    Properties properties = new Properties();
    properties.setProperty(Consts.MCAST_HEARTBEAT_TIMEOUT, Long.toString(heartBeatTimeout));
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_MEMORY);
    return new EventChannel("test", new Conf().addProperties(properties));
  }

  private EventChannel createEventChannel(String domain, long heartBeatInterval, long heartBeatTimeout) throws Exception {
    Properties properties = new Properties();
    properties.setProperty(Consts.MCAST_HEARTBEAT_TIMEOUT, Long.toString(heartBeatTimeout));
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_MEMORY);
    return new EventChannel(domain, new Conf().addProperties(properties));
  }

}
