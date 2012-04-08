package org.sapia.ubik.rmi.server.transport.socket;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sapia.ubik.concurrent.Counter;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;

public class SocketTransportExporterTest {

  @Test
	public void testExport() throws Exception {
		
		final Counter counter = new Counter(2);
		System.setProperty(Consts.ENABLE_COLOCATED_CALLS, "false");
		try {
			SocketTransportExporter exporter = new SocketTransportExporter();
			TestInterface remoteObject = (TestInterface) exporter.export(new TestInterface() {
				@Override
				public void test() {
					counter.increment();
				}
			});
			
			remoteObject.test();
			remoteObject.test();
			assertEquals(2, counter.getCount());
		} finally {
			System.clearProperty(Consts.ENABLE_COLOCATED_CALLS);			
			Hub.shutdown();
		}
	}
	
	public interface TestInterface {
		
		public void test();
	}


}
