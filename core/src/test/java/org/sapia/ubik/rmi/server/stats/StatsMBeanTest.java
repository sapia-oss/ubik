package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.StatsMBean;
import org.sapia.ubik.rmi.server.stats.Timer;

public class StatsMBeanTest {
  
  StatsMBean mbean;
  Timer      timer;

  @Before
  public void setUp() throws Exception {
    timer = Stats.getInstance().createTimer(getClass(), "Test", "testTimer");
    Stats.getInstance().enable();
    mbean = new StatsMBean();
  }
  
  @After
  public void tearDown() throws Exception {
    Stats.getInstance().clear();
  }

  @Test
  public void testGetMBeanInfo() {
    mbean.getMBeanInfo();
  }
  
  @Test
  public void testGetTimerAttribute() throws Exception {
    timer.start();
    Thread.sleep(500);
    timer.end();
    Double duration = (Double)mbean.getAttribute(getClass().getSimpleName()+".Test");
    assertTrue("Invalid duration: " + duration, Math.abs(duration - 500) < 50);
  }
  
  @Test
  public void testIsEnabled() throws Exception {
    Boolean enabled = (Boolean)mbean.getAttribute("IsEnabled");
    assertTrue("Should be enabled", enabled.booleanValue());
  }

  @Test
  public void testInvoke() throws Exception{
    mbean.invoke("Enable", new Object[0], new String[0]);
    assertTrue(Stats.getInstance().isEnabled());
    mbean.invoke("Disable", new Object[0], new String[0]);
    assertTrue(!Stats.getInstance().isEnabled());    
  }

}
