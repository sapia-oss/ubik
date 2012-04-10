package org.sapia.ubik.rmi.server.stats;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Clock;

public class HitStatisticTest {
	
	private Clock.MutableClock clock;
	
	@Before
	public void setUp() {
		clock = new Clock.MutableClock();
	}
  
  @Test
  public void testGetHitsPerSecond() throws Exception{
    
    HitsStatistic hits = new HitsStatistic(clock, "test source", "test", "this is a test", StatsTimeUnit.SECONDS);
    hits.setEnabled(true);
    
    for(int i = 0; i < 100; i++) {
    	hits.getHits().hit();
    }
    
    clock.increaseCurrentTimeMillis(4000);
    double hitsValue = hits.getHits().getValue();
    
    assertEquals("Expected 25 hits per second, got " + hitsValue, 25, hitsValue, 0);
  }
  
  @Test
  public void testGetHitsPerMinute() throws Exception{
    
    HitsStatistic hits = new HitsStatistic(clock, "test source", "test", "this is a test", StatsTimeUnit.MINUTES);
    hits.setEnabled(true);
    
    for(int i = 0; i < 100; i++) {
    	hits.getHits().hit(10);
    }

    clock.increaseCurrentTimeMillis(2000);
    double hitsValue = hits.getHits().getValue();
   
    assertEquals("Expected 30,000 hits per minute, got " + hitsValue, 30000, hitsValue, 0);
  }
  
  @Test
  public void testGetHitsPerHour() throws Exception{
    
    HitsStatistic hits = new HitsStatistic(clock, "test source", "test", "this is a test", StatsTimeUnit.HOURS);
    hits.setEnabled(true);
    
    for(int i = 0; i < 100; i++) {
    	hits.getHits().hit(10);
    }

    clock.increaseCurrentTimeMillis(60000);    
    double hitsValue = hits.getHits().getValue();
    
    assertEquals("Expected 60,000 hits per hour, got " + hitsValue, 60000, hitsValue, 0);    
  }
  
  @Test
  public void testGetHitsPerDay() throws Exception{
    
    HitsStatistic hits = new HitsStatistic(clock, "test source", "test", "this is a test", StatsTimeUnit.DAYS);
    hits.setEnabled(true);
    
    for(int i = 0; i < 100; i++) {
    	hits.getHits().hit(10);
    }

    clock.increaseCurrentTimeMillis(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));    
    double hitsValue = hits.getHits().getValue();    
   
    assertEquals("Expected 6,000 hits per day, got " + hitsValue, 6000, hitsValue, 0);
  }

}
