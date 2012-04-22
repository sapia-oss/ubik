package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.stats.Statistic;

public class StatisticTest {
  
  Statistic stat;

  @Before
  public void setUp() throws Exception {
    stat = new Statistic("testSource", "test", "this is a test stat");
    stat.setEnabled(true);
  }

  @Test
  public void testEnabled() {
    stat.setEnabled(true);
    assertTrue(stat.isEnabled());
    stat.setEnabled(false);
    assertTrue(!stat.isEnabled());    
  }

  @Test
  public void testGetName() {
    assertEquals("test", stat.getName());
  }

  @Test
  public void testGetValue() {
    assertEquals(new Double(0), new Double(stat.getValue()));
  }

  @Test
  public void testIncrementLong() {
    stat.incrementLong(10);
    assertEquals(new Double(10), new Double(stat.getValue()));    
  }

  @Test
  public void testIncrementInt() {
    stat.incrementInt(10);
    assertEquals(new Double(10), new Double(stat.getValue()));
  }

  @Test
  public void testAvg() {
    stat.incrementInt(10);
    assertEquals(new Double(10), new Double(stat.getStat()));
    stat.incrementLong(10);
    assertEquals(new Double(10), new Double(stat.getStat()));    
  }

}
