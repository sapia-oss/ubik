package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;
import org.sapia.ubik.util.Clock;

public class TimerTest {
  
	private Clock.MutableClock clock;
  private Timer timer;

  @Before
  public void setUp() throws Exception {
  	clock = Clock.MutableClock.getInstance();
    timer = new Timer(new Statistic("test", "test", "desc"), clock);
    timer.setEnabled(true);
  }
  
  @After
  public void tearDown() {
  }

  @Test
  public void testStartEnd() throws Exception {
    timer.start();
    clock.increaseCurrentTimeMillis(1000);
    timer.end();
    
    assertEquals(1000, timer.getValue(), 0);
  }
  
  @Test
  public void testMultipleStartEnd() throws Exception {
    for(int i = 0; i < 10; i++) {
      timer.start();
      clock.increaseCurrentTimeMillis(100);
      timer.end();
    }
    
    assertEquals(100, timer.getValue(), 0);
    
  }

}
