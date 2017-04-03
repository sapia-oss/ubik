package org.sapia.ubik.mcast;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.mcast.group.GroupMembershipService;
import org.sapia.ubik.util.Conf;

@RunWith(MockitoJUnitRunner.class)
public abstract class GroupMembershipBootstrapTestSupport {

  private EventConsumer            consumer1, consumer2;
  private GroupMembershipBootstrap bootstrap1, bootstrap2;
  
  private BlockingRef<RemoteEvent> disco1;
  private BlockingRef<RemoteEvent> disco2;
  
  private AsyncEventListener listener1, listener2;
  
  @Before
  public void setUp() throws Exception {
    doSetUp();
    consumer1  = new EventConsumer("domain01", 1, 10);
    consumer2  = new EventConsumer("domain01", 1, 10);
    bootstrap1 = new GroupMembershipBootstrap(consumer1, Conf.newInstance());
    bootstrap2 = new GroupMembershipBootstrap(consumer2, Conf.newInstance());
    
    GroupMembershipService svc1 = createGroupMembershipService();
    GroupMembershipService svc2 = createGroupMembershipService();
    
    UnicastDispatcher disp1 = createUnicastDispatcher(consumer1);
    UnicastDispatcher disp2 = createUnicastDispatcher(consumer2);
    
    disco1 = new BlockingRef<>();
    disco2 = new BlockingRef<>();
    
    listener1 = new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        disco1.set(evt);
      }
    };
    
    listener2 = new AsyncEventListener() {
     
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        disco2.set(evt);
      }
    };

    bootstrap1.start(new RemoteEvent("domain01", "test-event", "test-data-1"), listener1, svc1, disp1, 2);
    bootstrap2.start(new RemoteEvent("domain01", "test-event", "test-data-2"), listener2, svc2, disp2, 2);
  }
  
  @After
  public void tearDown() throws Exception {
    if (bootstrap1 != null) {
      bootstrap1.close();
    }
    if (bootstrap2 != null) {
      bootstrap2.close();
    }
    doTearDown();
  }

  @Test
  public void testMemberDiscovery() throws Exception {
    RemoteEvent evtFrom2 = disco1.await(3000);
    RemoteEvent evtFrom1 = disco2.await(3000);

    assertEquals("test-data-2", evtFrom2.getData());
    assertEquals("test-data-1", evtFrom1.getData());
  }
  
  protected void doSetUp() throws Exception {
  }
  
  protected void doTearDown() throws Exception {
  }
  
  protected abstract UnicastDispatcher createUnicastDispatcher(EventConsumer consumer);
  
  protected abstract GroupMembershipService createGroupMembershipService();
}
