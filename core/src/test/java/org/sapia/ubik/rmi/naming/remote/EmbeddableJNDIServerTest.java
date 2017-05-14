package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.assertNotNull;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.InitialContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.memory.InMemoryBroadcastDispatcher;
import org.sapia.ubik.mcast.memory.InMemoryUnicastDispatcher;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.ExtendedProperties;
import org.sapia.ubik.util.PropUtil;

public class EmbeddableJNDIServerTest  {
  
  private EventChannel channel1, channel2;
  private EmbeddableJNDIServer jndi;
  
  @Before
  public void setUp() throws Exception {
    Log.setDebug();
    EventChannel.disableReuse();
    
    Conf conf = new ExtendedProperties()
        .setInt(Consts.MCAST_CONSUMER_MIN_COUNT, 1)
        .setInt(Consts.MCAST_CONSUMER_MAX_COUNT, 10).toConf();
    
    EventConsumer cons1 = new EventConsumer("test", conf);
    
    channel1 = new EventChannel(
        cons1, 
        createUnicastDispatcher(cons1), createBroadcastDispatcher(cons1)
    );

    EventConsumer cons2 = new EventConsumer("test", conf);
    channel2 = new EventChannel(
        cons2, 
        createUnicastDispatcher(cons2), createBroadcastDispatcher(cons2)
    );
    
    jndi = new EmbeddableJNDIServer(channel1, 1098);
  }
  
  @After
  public void tearDown() {
    Log.setError();
    EventChannel.enableReuse();
    channel1.close();
    channel2.close();
    PropUtil.clearUbikSystemProperties();
    Hub.shutdown();
  }
  
  private BroadcastDispatcher createBroadcastDispatcher(EventConsumer cons) {
    InMemoryBroadcastDispatcher bd = new InMemoryBroadcastDispatcher();
    bd.initialize(cons, Conf.newInstance().addSystemProperties());
    return bd;
  }
  
  private UnicastDispatcher createUnicastDispatcher(EventConsumer cons) {
    InMemoryUnicastDispatcher ud = new InMemoryUnicastDispatcher();
    ud.initialize(cons, Conf.newInstance().addSystemProperties());
    return ud;
  }

  @Test
  public void testInMemoryBindServiceDiscovery() throws Exception {
    jndi.start(true);
    
    DiscoveryHelper helper = new DiscoveryHelper(channel2.getReference());
    final BlockingRef<TestService> ref = new BlockingRef<>();
    helper.addServiceDiscoListener(new ServiceDiscoListener() {
      @Override
      public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
        try {
          ref.set((TestService) evt.getService()); 
        } catch (RemoteException e) {
          ref.setNull();
        }
      }
    });
    channel2.start();
    
    jndi.getLocalContext().bind("test", Mockito.mock(TestService.class));
    
    TestService service = ref.await();
    
    assertNotNull(service);
  }
  
  @Test
  public void testInMemoryBindServiceDiscovery_LateStartOfJndi() throws Exception {
    
    DiscoveryHelper helper = new DiscoveryHelper(channel2.getReference());
    final BlockingRef<TestService> ref = new BlockingRef<>();
    helper.addServiceDiscoListener(new ServiceDiscoListener() {
      @Override
      public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
        try {
          ref.set((TestService) evt.getService()); 
        } catch (RemoteException e) {
          ref.setNull();
        }
      }
    });
    channel2.start();
    
    jndi.start(true);
    jndi.getLocalContext().bind("test", Mockito.mock(TestService.class));
    
    TestService service = ref.await();
    
    assertNotNull(service);
  }
  
  @Test
  public void testInMemoryBindServiceDiscovery_LateStartOfDiscoHelper() throws Exception {
    DiscoveryHelper helper = new DiscoveryHelper(channel2.getReference());
    final BlockingRef<TestService> ref = new BlockingRef<>();
    helper.addServiceDiscoListener(new ServiceDiscoListener() {
      @Override
      public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
        try {
          ref.set((TestService) evt.getService()); 
        } catch (RemoteException e) {
          ref.setNull();
        }
      }
    });
    
    jndi.start(true);
    jndi.getLocalContext().bind("test", Mockito.mock(TestService.class));
    channel2.start();
    
    TestService service = ref.await();
    
    assertNotNull(service);
  }
  
  @Test
  public void testRemoteLookup() throws Exception {
    jndi.start(true);
    Thread.sleep(500);
    jndi.getLocalContext().bind("test", Mockito.mock(TestService.class));

    Properties props = new Properties();
    props.setProperty(Consts.UNICAST_PROVIDER,  Consts.UNICAST_PROVIDER_MEMORY);
    props.setProperty(Consts.BROADCAST_PROVIDER,  Consts.BROADCAST_PROVIDER_MEMORY);

    props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, "test");
    props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1111");
    props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());

    InitialContext ctx = new InitialContext(props);
    ctx.lookup("test");
  }
  
  public interface TestService {
    public void test();
  }

}
