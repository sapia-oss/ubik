package org.sapia.ubik.mcast.control;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketAddress;
import org.sapia.ubik.util.Clock;

public class ControlRequestTest {

	@Test
	public void testSizeFor10Nodes() throws Exception {
	  System.out.println("Size with 10 nodes: " + McastUtil.getSizeInBytes(createWith(10)));
	}

	@Test
	public void testSizeFor20Nodes() throws Exception {
	  System.out.println("Size with 20 nodes: " + McastUtil.getSizeInBytes(createWith(20)));
	}
	
	@Test
	public void testSizeFor40Nodes() throws Exception {
	  System.out.println("Size with 40 nodes: " + McastUtil.getSizeInBytes(createWith(40)));
	}
	
	@Test
	public void testSizeFor100Nodes() throws Exception {
	  System.out.println("Size with 100 nodes: " + McastUtil.getSizeInBytes(createWith(100)));
	}	
	
	@Test
	public void testSizeFor200Nodes() throws Exception {
	  System.out.println("Size with 200 nodes: " + McastUtil.getSizeInBytes(createWith(200)));
	}	
	
	
	private TestControlRequest createWith(int numberOfNodes) {
		Set<String> nodes = new HashSet<String>();
		for(int i = 0; i < numberOfNodes; i++) {
			nodes.add(UUID.randomUUID().toString());
		}
		return new TestControlRequest(0, nodes);
	}
	
	
	public static class TestControlRequest extends ControlRequest {
		
		public TestControlRequest() {}
		
		public TestControlRequest(long requestId, Set<String> targetedNodes) {
			super(Clock.SystemClock.getInstance(), requestId, "master", new MultiplexSocketAddress("test", 1), targetedNodes);
		}
		
		public TestControlRequest(Set<String> targetedNodes) {
			super(targetedNodes);
    }
		
		@Override
		protected ControlRequest getCopy(Set<String> targetedNodes) {
		  return new TestControlRequest(targetedNodes);
		}
	}

}
