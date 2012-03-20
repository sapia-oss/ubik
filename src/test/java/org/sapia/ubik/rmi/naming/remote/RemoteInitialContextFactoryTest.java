package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.*;

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.spi.InitialContextFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;

public class RemoteInitialContextFactoryTest {
	
	private EmbeddableJNDIServer server;
	
	@Before
	public void setUp() throws Exception {
		System.setProperty(Consts.UBIK_DOMAIN_NAME, "test");
		System.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
		System.setProperty(Consts.BROADCAST_MEMORY_NODE, "test");
		EventChannel ec = new EventChannel("test", Props.getSystemProperties());
		server = new EmbeddableJNDIServer(ec, 1099);
		server.start(false);
	}

	@After
	public void tearDown() {
		System.clearProperty(Consts.BROADCAST_PROVIDER);
		System.clearProperty(Consts.BROADCAST_MEMORY_NODE);
		server.stop();
	}

	@Test
	public void testJndiDiscovery() throws Exception {
		Hashtable props = new Hashtable();
		props.put(InitialContext.PROVIDER_URL, "ubik://localhost:9999/");
		props.put(InitialContext.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
		InitialContext context = new InitialContext(props);  
		try {
			context.lookup("some/service");
			fail("NameNotFoundException should have been thrown");
		} catch (NameNotFoundException e) {
			// ok
		} 
		context.close();
	}

}
