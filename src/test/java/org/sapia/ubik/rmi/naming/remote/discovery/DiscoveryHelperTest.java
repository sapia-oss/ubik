package org.sapia.ubik.rmi.naming.remote.discovery;

import static org.junit.Assert.assertNotNull;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.naming.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.mcast.EventChannelTestSupport;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;
import org.sapia.ubik.rmi.server.Hub;

public class DiscoveryHelperTest {
	
	private EmbeddableJNDIServer jndi;
	private DiscoveryHelper 		 helper;
	
	@Before
	public void setUp() throws Exception {
		jndi   = new EmbeddableJNDIServer(EventChannelTestSupport.createEventChannel("test"), 1099);
		helper = new DiscoveryHelper(EventChannelTestSupport.createEventChannel("test"));
		jndi.start(true);
		
	}
	
	@After
	public void tearDown() {
		helper.close();
		jndi.stop();
	}

	@Test
	public void testServiceDiscovery() throws Exception {
		final BlockingRef<TestService> ref = new BlockingRef<DiscoveryHelperTest.TestService>();
		helper.addServiceDiscoListener(new ServiceDiscoListener() {
			@Override
			public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
				try {
					ref.set((TestService)evt.getService());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
		TestService toBind = new TestService() {};
		jndi.getRootContext().bind("test", Hub.exportObject(toBind));
		
		TestService service = ref.await(3000);
		assertNotNull("Service not discovered", service);
	}
	
	@Test
	public void testJndiDiscovery() throws Exception {
		final BlockingRef<Boolean> ref = new BlockingRef<Boolean>();
		helper.addJndiDiscoListener(new JndiDiscoListener() {
			@Override
			public void onJndiDiscovered(Context ctx) {
				ref.set(new Boolean(true));
			}
		});
		
		Boolean discovered = ref.await(3000);
		assertNotNull("JNDI not discovered", discovered);
	}
	
	interface TestService extends Remote {
		
	}
}
