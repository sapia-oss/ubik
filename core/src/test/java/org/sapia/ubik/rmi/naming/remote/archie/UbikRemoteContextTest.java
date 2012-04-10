package org.sapia.ubik.rmi.naming.remote.archie;

import java.io.Serializable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.mcast.EventChannelTestSupport;

public class UbikRemoteContextTest {
	
	private UbikRemoteContext src, target;
	
	@Before
	public void setUp() throws Exception {
		src 	 = UbikRemoteContext.newInstance(EventChannelTestSupport.createEventChannel("ubik.test"));
		target = UbikRemoteContext.newInstance(EventChannelTestSupport.createEventChannel("ubik.test"));		
	}
	
	@After
	public void tearDown() throws Exception {
		src.close();
		target.close();
	}

	@Test
	public void testReplicatedBind() throws Exception {
		src.bind("service", new SerializableObj());
		Thread.sleep(2000);
		target.lookup("service");
	}
	
	@Test
	public void testReplicatedRebind() throws Exception {
		src.rebind("service", new SerializableObj());
		Thread.sleep(2000);
		target.lookup("service");
	}
	
	@Test
	public void testReplicatedLookup() throws Exception {
		src.rebind("service", new SerializableObj());
		UbikRemoteContext lateContext = UbikRemoteContext.newInstance(EventChannelTestSupport.createEventChannel("ubik.test"));
		Thread.sleep(2000);
		try {
			lateContext.lookup("service");
		} finally {
			lateContext.close();
		}
	}	
	
	public static class SerializableObj implements Serializable {
		
	}

}
