package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.ubik.rmi.server.stats.HitsStatistic;
import org.sapia.ubik.rmi.server.stats.StatsTimeUnit;
import org.sapia.ubik.util.Delay;

public class HitStatisticTest {
  
  @Test
  public void testGetHitsPerSecond() throws Exception{
    
    HitsStatistic hits = new HitsStatistic("test source", "test", "this is a test", StatsTimeUnit.SECONDS);
    hits.setEnabled(true);
    
    Delay delay = new Delay(5000);
    while(!delay.isOver()){
      hits.getHits().hit();
      Thread.sleep(250);
    }
    
    double hitsValue = hits.getHits().getValue();
   
    assertTrue("Expected value between 3 and 5, got " + hitsValue, between(hitsValue, 3, 5));
  }
  
  @Test
  public void testGetHitsPerMinute() throws Exception{
    
    HitsStatistic hits = new HitsStatistic("test source", "test", "this is a test", StatsTimeUnit.MINUTES);
    hits.setEnabled(true);
    
    Delay delay = new Delay(5000);
    while(!delay.isOver()){
      hits.getHits().hit();
      Thread.sleep(250);
    }
    
    double hitsValue = hits.getHits().getValue();
   
    assertTrue("Expected value between 239 and 241, got " + hitsValue, between(hitsValue, 239, 241));
  }
  
  @Test
  public void testGetHitsPerHour() throws Exception{
    
    HitsStatistic hits = new HitsStatistic("test source", "test", "this is a test", StatsTimeUnit.HOURS);
    hits.setEnabled(true);
    
    Delay delay = new Delay(5000);
    while(!delay.isOver()){
      hits.getHits().hit();
      Thread.sleep(250);
    }
    
    double hitsValue = hits.getHits().getValue();
   
    assertTrue("Expected value between 14300 and 14500, got " + hitsValue, between(hitsValue, 14300, 14500));
  }
  
  @Test
  public void testGetHitsPerDay() throws Exception{
    
    HitsStatistic hits = new HitsStatistic("test source", "test", "this is a test", StatsTimeUnit.DAYS);
    hits.setEnabled(true);
    
    Delay delay = new Delay(5000);
    while(!delay.isOver()){
      hits.getHits().hit();
      Thread.sleep(250);
    }
    
    double hitsValue = hits.getHits().getValue();
   
    assertTrue("Expected value between 345000 and 346000, got " + hitsValue, between(hitsValue, 345000, 346000));
  }
  
  private boolean between(double given, double low, double high) {
    return given > low && given < high;
  }

}
