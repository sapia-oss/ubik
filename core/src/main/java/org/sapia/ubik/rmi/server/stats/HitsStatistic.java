package org.sapia.ubik.rmi.server.stats;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.sapia.ubik.util.Clock;
import org.sapia.ubik.util.Clock.SystemClock;

/**
 * Calculates hits per second. 
 * 
 * @author yduchesne
 *
 */
public class HitsStatistic extends Statistic implements StatCapable {
  
  /**
   * Use to build an instance of this class.
   * @author yduchesne
   *
   */
  public static class Builder {
    
  	private Clock         clock        = SystemClock.getInstance();
    private String        source;
    private String        name;
    private String        description;
    private StatsTimeUnit unit         = StatsTimeUnit.SECONDS;
    
    Builder(String source, String name, String description) {
      this.source      = source;
      this.name        = name;
      this.description = description;      
    }
    
    public Builder timeUnit(TimeUnit timeUnit) {
      this.unit = StatsTimeUnit.fromJdkTimeUnit(timeUnit);
      return this;
    }
    
    public Builder timeUnit(StatsTimeUnit timeUnit) {
      this.unit = timeUnit;
      return this;
    }
    
    public Builder perSecond() {
      return timeUnit(TimeUnit.SECONDS);
    }        
    
    public Builder perMinute() {
      return timeUnit(TimeUnit.MINUTES);
    }    
    
    public Builder perHour() {
      return timeUnit(TimeUnit.HOURS);
    }
    
    public Builder perDay() {
      return timeUnit(TimeUnit.DAYS);
    }        
    
    public Builder clock(Clock clock) {
    	this.clock = clock;
    	return this;
    }
    
    public Hits build() {
      HitsStatistic stat = new HitsStatistic(clock, source, name, description, unit);
      Stats.getInstance().add(stat);
      return new Hits(stat);
    }
    
  }
  
  // --------------------------------------------------------------------------

	private Clock          clock      = SystemClock.getInstance();
  private AtomicLong     startTime;
  private AtomicInteger  counter    = new AtomicInteger();
  private Hits           hits;
  private StatsTimeUnit  unit       = StatsTimeUnit.SECONDS;
  
  /**
   * @see Statistic#Statistic(String, String, String)
   */
  protected HitsStatistic(Clock clock, String source, String name, String description, StatsTimeUnit unit){
    super(source, name, description);
    this.clock = clock;
    this.startTime  = new AtomicLong(clock.nanoTime());
    this.unit       = unit;
    hits = new Hits(this);
  }

  /**
   * This method must be called by client applications to increment
   * the int counter.
   */
  void increment(){
    counter.incrementAndGet();
  }
  
  /**
   * This method must be called by client applications to increment
   * the int counter.
   * 
   * @param increment
   */
  void increment(int increment){
    counter.addAndGet(increment);
  }  
  
  /**
   * @return this instance's {@link Hits}.
   */
  public Hits getHits() {
    return hits;
  }
  
  /**
   * @return the start time of the current sample interval, in millis.
   */
  public long getStartTime(){
    return startTime.get();
  }
  
  public synchronized double getStat() {
    long   start         = startTime.get();
    double elapsedMillis = convertToMillis(clock.nanoTime() - start);
    double rate          = elapsedMillis == 0 ? 0 : counter.doubleValue() / elapsedMillis;
    super.incrementDouble(rate);
    startTime.set(clock.nanoTime());
    counter.set(0);
    return super.getStat();
  }
  
  protected synchronized void onPostReset() {
    counter.set(0);
    startTime.set(clock.nanoTime());
  }
  
  private double convertToMillis(long delayNanos){
    return unit.convertFrom(delayNanos, StatsTimeUnit.NANOSECONDS);
  }
}

