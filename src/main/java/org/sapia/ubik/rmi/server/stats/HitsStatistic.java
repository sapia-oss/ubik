package org.sapia.ubik.rmi.server.stats;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Calculates hits per second. 
 * 
 * @author yduchesne
 *
 */
public class HitsStatistic extends Statistic implements StatCapable {
  
  private static final long DEFAULT_SAMPLE_RATE = 1000;

  /**
   * Use to build an instance of this class.
   * @author yduchesne
   *
   */
  public static class Builder {
    
    private String        source;
    private String        name;
    private String        description;
    private long          sampleRate   = DEFAULT_SAMPLE_RATE;
    private StatsTimeUnit unit         = StatsTimeUnit.SECONDS;
    
    Builder(String source, String name, String description) {
      this.source      = source;
      this.name        = name;
      this.description = description;      
    }
    
    public Builder sampleRate(long rate) {
      this.sampleRate = rate;
      return this;
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
    
    public Hits build() {
      HitsStatistic stat = new HitsStatistic(source, name, description, sampleRate, unit);
      Stats.getInstance().add(stat);
      return new Hits(stat);
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  private long           sampleRate = DEFAULT_SAMPLE_RATE;
  private AtomicLong     startTime  = new AtomicLong(System.currentTimeMillis());
  private AtomicInteger  counter    = new AtomicInteger();
  private Hits           hits;
  private StatsTimeUnit  unit       = StatsTimeUnit.SECONDS;
  
  /**
   * @see Statistic#Statistic(String, String, String)
   */
  protected HitsStatistic(String source, String name, String description, long sampleRate, StatsTimeUnit unit){
    super(source, name, description);
    this.sampleRate = sampleRate;
    this.unit       = unit;
    hits = new Hits(this);
  }
  
  /**
   * @see Statistic#Statistic(String, String, String)
   */
  protected HitsStatistic(String source, String name, String description, StatsTimeUnit unit){
    this(source, name, description, DEFAULT_SAMPLE_RATE, unit);
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
    Long start = startTime.get();
    if(System.currentTimeMillis() - start > sampleRate){
      double timebase = convertMillis((System.currentTimeMillis() - start));
      double ratio;
      if(timebase == 0) {
        ratio = 0;
      } else {
        ratio = counter.doubleValue() / timebase;
      }
      super.incrementDouble(ratio);
      startTime.set(System.currentTimeMillis());
      double value = super.getStat();
      counter.set(0);
      return value;
    } else {
      return super.getStat();
    }
  }
  
  protected synchronized void onPostReset() {
    counter.set(0);
    startTime.set(System.currentTimeMillis());
  }
  
  private double convertMillis(long delay){
    return unit.convertFrom(delay, StatsTimeUnit.MILLISECONDS);
  }

}

