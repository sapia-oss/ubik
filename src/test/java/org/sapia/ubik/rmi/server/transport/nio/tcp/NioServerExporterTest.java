package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;

public class NioServerExporterTest {

  @Test
	public void testExport() throws Exception {
		
		final AtomicReference<String> ref = new AtomicReference<String>();
		System.setProperty(Consts.ENABLE_COLOCATED_CALLS, "false");
		try {
			NioServerExporter exporter = new NioServerExporter();
			TestInterface remoteObject = (TestInterface) exporter.export(new TestInterface() {
				@Override
				public void test() {
					ref.set("test");
				}
			});
			
			remoteObject.test();
			remoteObject.test();
		} finally {
			System.clearProperty(Consts.ENABLE_COLOCATED_CALLS);			
			Hub.shutdown();
		}
	}
	
	public interface TestInterface {
		
		public void test();
	}

}
