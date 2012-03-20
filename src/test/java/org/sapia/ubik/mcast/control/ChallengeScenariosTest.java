package org.sapia.ubik.mcast.control;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.mcast.control.EventChannelStateController.Role;

public class ChallengeScenariosTest extends EventChannelControllerTestSupport {

	@Before
	public void setUp() {
		super.setUp();
	}
	
	// testing that the master is properly determine at startup, if no master currently
	// is defined
	@Test
	public void testMaster() {
		slave1.getController().checkStatus();
		assertEquals("Wrong role, expected SLAVE", Role.SLAVE, slave1.getController().getContext().getRole());
		slave2.getController().checkStatus();
		assertEquals("Wrong role, expected SLAVE", Role.SLAVE, slave2.getController().getContext().getRole());
		master.getController().checkStatus();
		assertEquals("Wrong role, expected MASTER", Role.MASTER, master.getController().getContext().getRole());
	}
	
	// testing that a new node does not become master if the current master
	// is still present
	@Test
	public void testMasterConflict() {
		slave1.getController().checkStatus();
		slave2.getController().checkStatus();
		master.getController().checkStatus();
		TestChannelCallback newNode = new TestChannelCallback("AA", clock, config);	
		newNode.addSibling(slave1).addSibling(slave2).addSibling(master);
		slave1.addSibling(newNode);
		slave2.addSibling(newNode);
		master.addSibling(newNode);
		newNode.getController().checkStatus();
		assertEquals("Wrong role, expected SLAVE", Role.SLAVE, newNode.getController().getContext().getRole());
	}
	
	// testing that a new node becomes master if current master is down
	@Test
	public void testNewMaster() {
		slave1.getController().checkStatus();
		slave2.getController().checkStatus();
		master.getController().checkStatus();
		slave1.down(master.getNode());
		slave2.down(master.getNode());
		TestChannelCallback newNode = new TestChannelCallback("AA", clock, config);	
		newNode.addSibling(slave1).addSibling(slave2);
		slave1.addSibling(newNode);
		slave2.addSibling(newNode);
		newNode.getController().checkStatus();
		assertEquals("Wrong role, expected MASTER", Role.MASTER, newNode.getController().getContext().getRole());
	}
	
	// testing that an existing node does not become master if it detects a heartbeat interval timeout and attempts
	// becoming the master itself
	@Test
	public void testMasterConflictAfterHeartbeatIntervalTimeout() {
		slave1.getController().checkStatus();
		slave2.getController().checkStatus();
		master.getController().checkStatus();
		clock.increaseCurrentTimeMillis(master.getController().getConfig().getHeartbeatInterval() + 10000);
		slave1.getController().checkStatus();
		assertEquals("Wrong role, expected MASTER", Role.MASTER, master.getController().getContext().getRole());
		assertEquals("Challenge request was not sent",  clock.currentTimeMillis(), slave1.getController().getContext().getLastChallengeRequestSentTime());
	}
	
	// testing that an existing node does become master if it detects a heartbeat interval timeout and the current
	// master is down
	@Test
	public void testNewAfterHeartbeatIntervalTimeout() {
		slave1.getController().checkStatus();
		slave2.getController().checkStatus();
		master.getController().checkStatus();
		slave1.down(master.getNode());
		slave2.down(master.getNode());
		clock.increaseCurrentTimeMillis(master.getController().getConfig().getHeartbeatInterval() + 10000);
		slave1.getController().checkStatus();
		assertEquals("Wrong role, expected MASTER", Role.MASTER, slave1.getController().getContext().getRole());
		assertEquals("Challenge request was not sent",  clock.currentTimeMillis(), slave1.getController().getContext().getLastChallengeRequestSentTime());
	}
	
}
