package org.sapia.ubik.mcast.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.log.Log;

public class HeartbeatScenariosTest extends EventChannelControllerTestSupport {

	@Before
	public void setUp() {
		super.setUp();
	}
	
	@Test
	public void testHeartbeat() {
		master.getController().checkStatus();
		slave1.getController().checkStatus();
		slave2.getController().checkStatus();
		clock.increaseCurrentTimeMillis(master.getController().getConfig().getHeartbeatInterval() + 1);
		master.getController().checkStatus();
		assertEquals(
				"Heartbeat request was not received by slave1", 
				clock.currentTimeMillis(), 
				slave1.getController().getContext().getLastHeartbeatRequestReceivedTime());
		
		assertEquals(
				"Heartbeat request was not received by slave2", 
				clock.currentTimeMillis(), 
				slave2.getController().getContext().getLastHeartbeatRequestReceivedTime());
	}
	
	@Test
	public void testDownDetected() {
		master.getController().checkStatus();
		slave1.getController().checkStatus();
		slave2.getController().checkStatus();
		slave2.flagDown();
		clock.increaseCurrentTimeMillis(master.getController().getConfig().getHeartbeatInterval() + 1);
		master.getController().checkStatus();
		clock.increaseCurrentTimeMillis(master.getController().getConfig().getResponseTimeout() + 1);
		master.getController().checkStatus();
		
		assertFalse(
				"Down notification not received by slave1", 
				slave1.containsSibling(slave2.getNode()));		
		assertFalse(
				"Down node not removed from master", 
				master.containsSibling(slave2.getNode()));
	}
	
}
