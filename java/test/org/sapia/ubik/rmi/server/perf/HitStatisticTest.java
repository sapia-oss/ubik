package org.sapia.ubik.rmi.server.perf;

import junit.framework.TestCase;

public class HitStatisticTest extends TestCase {
  
  HitsPerSecStatistic hit;

  public HitStatisticTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
    hit = new HitsPerSecStatistic("Test");
  }

  public void testGetStat() throws Exception{
    hit.setEnabled(true);
    long startTime = System.currentTimeMillis();
    long duration = 5000;
    int count = 0;
    while(System.currentTimeMillis() - startTime < duration){
      hit.hit();
      count++;
      Thread.sleep(250);
    }
    
    double hits = hit.getStat();
    
    assertTrue(hits > 3 && hits < 5);
  }

}
