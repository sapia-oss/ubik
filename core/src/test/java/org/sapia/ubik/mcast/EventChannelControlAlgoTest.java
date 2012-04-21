package org.sapia.ubik.mcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.log.IncludeClassFilter;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.EventChannelControllerListener;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Clock;
import org.sapia.ubik.util.Clock.MutableClock;
import org.sapia.ubik.util.Delay;
import org.sapia.ubik.util.Props;

public class EventChannelControlAlgoTest {

	private MutableClock       clock;
	private List<EventChannel> channels;
	private TestControllerListener listener;
	
	@Before
	public void setUp() throws Exception {
		clock 	 = Clock.MutableClock.getInstance();
		channels = new ArrayList<EventChannel>();
		listener = new TestControllerListener();
		
		for (int i = 0; i < 100; i++) {
			EventChannel channel = createChannel();
			channels.add(channel);
			channel.getController().getContext().addControllerListener(listener);
		}
		
		for (EventChannel channel : channels) {
			channel.start();
		  Thread.sleep(100);			
		}

	}
	
	@After
	public void tearDown() {
		for (EventChannel channel : channels) {
			channel.close();
		}
	}
	
	@Test
	public void testChannel() throws Exception {
		
		// kickoffing challenge and waiting for responses to come in
		checkStatus();
		listener.waitChallengeCompleted();

		// checking that master was determined
		doAssertMaster();
		
		// triggering heartbeat request and waiting
		// for responses to come in
		clock.increaseCurrentTimeMillis(Defaults.DEFAULT_HEARTBEAT_INTERVAL+1);		
		do {
			checkStatus();
		} while (!listener.waitHeartbeatCompleted(3000));
	}
	
	
	private void checkStatus() {
		for (EventChannel channel : channels) {
			channel.getController().checkStatus();
		}		
	}
	
	private void doAssertMaster() {
		for (EventChannel channel : channels) {
			if (channel.getRole() == Role.MASTER) {
				return; 
			}
		}		
		fail("Master channel not found");
	}
	
	private void doAssertLastHeartbeatRequestReceived(long expected) {
		for (EventChannel channel : channels) {
			if (channel.getRole() != Role.MASTER) {
				assertEquals(expected, channel.getController().getContext().getLastHeartbeatRequestReceivedTime());
			}
		}		
	}	
	
	private EventChannel createChannel() throws Exception {
    Properties properties = new Properties();
    properties.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    properties.setProperty(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_MEMORY);
    EventChannel channel = new TestEventChannel("test", new Props().addProperties(properties));
    return channel;
	}
	
	// ==========================================================================
	
	public class TestEventChannel extends EventChannel {
		
		public TestEventChannel(String domain, Props props) throws IOException {
			super(domain, props);
    }

  	@Override
  	protected void startTimer(long heartbeatInterval) {}
  	
  	@Override
  	protected Clock createClock() {
  	  return clock;
  	}
		
	} 
	
	public class TestControllerListener implements EventChannelControllerListener {
		
		private boolean challengeCompleted;
		private boolean heartbeatCompleted;
		
		@Override
		public synchronized void onChallengeCompleted(ControllerContext context) {
		  challengeCompleted = true;
		  notify();
		}
		
		@Override
		public synchronized void onHeartBeatCompleted(ControllerContext context,
		    int expectedCount, int effectiveCount) {
			heartbeatCompleted = true;
			notify();
		}
		
		synchronized void waitChallengeCompleted() throws InterruptedException {
			while (!challengeCompleted) {
				wait();
			}
			challengeCompleted = false;
		}
		
		synchronized boolean waitHeartbeatCompleted(long timeout) throws InterruptedException {
			Delay delay = new Delay(timeout); 
			while (!heartbeatCompleted && !delay.isOver()) {
				wait(delay.remainingNotZero());
			}
			
			boolean toReturn = heartbeatCompleted; 
			heartbeatCompleted = false;
			return toReturn;
		}
		
	}
}
