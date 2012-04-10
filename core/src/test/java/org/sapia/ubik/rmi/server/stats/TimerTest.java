package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;

public class TimerTest {
  
  private Timer timer;

  @Before
  public void setUp() throws Exception {
    timer = Stats.getInstance().createTimer(getClass(), "Test", "test");
    Stats.getInstance().enable();
  }
  
  @After
  public void tearDown() {
    Stats.getInstance().clear();
  }

  @Test
  public void testStartEnd() throws Exception {
    timer.start();
    Thread.sleep(1000);
    timer.end();
    
    assertTrue(
        "Invalid timer value. Expected greater than or equal to 1000 millis, got: " + timer.getValue(), 
        timer.getValue() >= 1000);
  }
  
  
  public void testMultipleStartEnd() throws Exception {
    for(int i = 0; i < 10; i++) {
      timer.start();
      Thread.sleep(100);
      timer.end();
    }
    
    assertTrue(
        "Invalid timer value. Expected greater than or equal to 1000 millis, got: " + timer.getValue(), 
        timer.getValue() >= 1000);
    
  }

}
