package org.sapia.ubik.mcast.control;

import static org.junit.Assert.*;

import org.sapia.ubik.util.Clock.MutableClock;
import org.sapia.ubik.util.Collections2;

public abstract class EventChannelControllerTestSupport {

	protected ControllerConfiguration config;
	protected MutableClock 						clock;
	protected TestChannelCallback    	master, slave1, slave2;

	public void setUp() {
		config 		= new ControllerConfiguration();
		config.setHeartbeatInterval(5000);
		config.setHeartbeatTimeout(10000);
		config.setResponseTimeout(20000);
		clock  	= new MutableClock();
		master  = new TestChannelCallback("DA", clock, config);
		slave1  = new TestChannelCallback("DB", clock, config);
		slave2  = new TestChannelCallback("DC", clock, config);
		master.addSibling(slave1).addSibling(slave2);
		assertTrue("Expected DB,DC nodes. Got: " + master.getNodes(), master.getNodes().containsAll(Collections2.arrayToSet(new String[]{"DB", "DC"})));
		slave1.addSibling(master).addSibling(slave2);
		assertTrue("Expected DA,DC nodes. Got: " + slave1.getNodes(), slave1.getNodes().containsAll(Collections2.arrayToSet(new String[]{"DA", "DC"})));
		slave2.addSibling(master).addSibling(slave1);
		assertTrue("Expected DA,DB nodes. Got: " + slave2.getNodes(), slave2.getNodes().containsAll(Collections2.arrayToSet(new String[]{"DA", "DB"})));
		
	}
	
}
