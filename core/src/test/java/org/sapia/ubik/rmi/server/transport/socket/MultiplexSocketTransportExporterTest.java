package org.sapia.ubik.rmi.server.transport.socket;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.Counter;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;

public class MultiplexSocketTransportExporterTest {
	
	@Before
	public void setUp() {
		Hub.shutdown();
		System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
	}
	
	@After
	public void tearDown() {
		Hub.shutdown();
		System.clearProperty(Consts.COLOCATED_CALLS_ENABLED);
	}

  @Test
	public void testExport() throws Exception {
		
		final Counter counter = new Counter(2);
		MultiplexServerExporter exporter = new MultiplexServerExporter();
		TestInterface remoteObject = (TestInterface) exporter.export(new TestInterface() {
			@Override
			public void test() {
				counter.increment();
			}
		});
		
		remoteObject.test();
		remoteObject.test();
		assertEquals(2, counter.getCount());
	}
	
	public interface TestInterface {
		
		public void test();
	}
}
