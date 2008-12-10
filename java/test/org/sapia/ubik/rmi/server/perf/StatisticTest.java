package org.sapia.ubik.rmi.server.perf;

import junit.framework.TestCase;

public class StatisticTest extends TestCase {
  
  Statistic stat;

  public StatisticTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
    stat = new Statistic("test", 10);
    stat.setEnabled(true);
  }

  public void testEnabled() {
    stat.setEnabled(true);
    assertTrue(stat.isEnabled());
    stat.setEnabled(false);
    assertTrue(!stat.isEnabled());    
  }

  public void testGetName() {
    assertEquals("test", stat.getName());
  }

  public void testGetValue() {
    assertEquals(new Double(0), new Double(stat.getValue()));
  }

  public void testIncrementLong() {
    stat.incrementLong(10);
    assertEquals(new Double(10), new Double(stat.getValue()));    
  }

  public void testIncrementInt() {
    stat.incrementInt(10);
    assertEquals(new Double(10), new Double(stat.getValue()));
  }

  public void testAvg() {
    stat.incrementInt(10);
    assertEquals(new Double(10), new Double(stat.getStat()));
    stat.incrementLong(10);
    assertEquals(new Double(10), new Double(stat.getStat()));    
  }

  public void testMaxCount(){
    for(int i = 0; i < 10; i++){
      stat.incrementInt(i+1);
    }
    Double avg = new Double(stat.getStat());
    assertEquals(new Double(5.5), avg);
    stat.incrementInt(22);
    avg = new Double(stat.getStat());
    assertEquals(new Double(7), avg);    
  }

}
