package org.sapia.ubik.rmi.server.stats;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class models a statistic. A statistic can be incremented and
 * provides an average over a given number of occurrences.  
 * 
 * @author yduchesne
 *
 */
public class Statistic {
  
  static final int DEFAULT_MAX_COUNT = 25;

  private StatisticKey  key;
  private String        description;
  private StatValue     value    = new StatValue();
  private AtomicInteger count    = new AtomicInteger();
  private int           maxCount;
  volatile boolean      enabled;
  
  /**
   * Creates a statistic with a max count of 100 by default.
   * 
   * @see #Statistic(String, int)
   */
  public Statistic(String source, String name, String description){
    this(source, name, description, DEFAULT_MAX_COUNT);
  }
  
  /**
   * @param source the stat's source.
   * @param name the stat's name.
   * @param description the stat's description.
   * @param maxCount the maximum number of occurrences over which averages
   * are to be calculated. Once that number is reached, the value of the
   * statistic is set to its current average, and the internal count is
   * reset to 0. 
   */
  public Statistic(String source, String name, String description, int maxCount){
    this.key = new StatisticKey(source, name);
    this.description = description;
    this.maxCount = maxCount;
  }
  
  /**
   * @return this stat's name.
   */
  public String getName(){
    return key.getName();
  }  
  
  /**
   * @return this instance's "source" (identifying the originator)
   */
  public String getSource() {
    return key.getSource();
  }
  
  /**
   * @return this instance's key.
   */
  public StatisticKey getKey() {
    return key;
  }
  
  /**
   * @return this instance's description.
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * @param enabled if <code>true</code>, stat calculation will
   * be enabled.
   */
  public Statistic setEnabled(boolean enabled){
    this.enabled = enabled;
    return this;
  }
  
  /**
   * @see #setEnabled(boolean)
   */
  public boolean isEnabled(){
    return enabled;
  }  
  
  /**
   * @return this stat's value.
   */
  public double getValue(){
    return value.get();
  }
  
  /**
   * Adds the given long to this instance. Also internally increments the
   * counter in order to calculate averages consistently.
   * 
   * @param inc the long to use for the incrementation.
   */
  public void incrementLong(long inc){
    if(enabled){
      doIncrementLong(inc);
    }
  }
  
  /**
   * @see #incrementLong(long)
   */
  public void incrementDouble(double inc){
    if(enabled){
      doIncrementDouble(inc);
    }
  }  
  
  /**
   * @see #incrementLong(long)
   */
  public void incrementInt(int inc){
    if(enabled){
      doIncrementInt(inc);
    }
  }
 
  /**
   * @return this instance's average, in one of the primitive wrappers (Integer, Long, Float, Double).
   */
  public double getStat(){
    return value.avg(count.get());
  }
  
  /**
   * Internally resets this instance's counter.
   */
  public void reset(){
    doReset();
  } 
  
  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(StatCapable other) {
    return getName().compareTo(other.getName());
  }
  
  private void doReset(){
    if(count.get() > maxCount){
      onPreReset();      
      value.set(value.avg(count.get()));      
      count.set(0);
      onPostReset();      
    }    
  }  
  
  public String toString(){
    return "[" + key.getName() + " = "+ value + "]";
  }
  
  private void doIncrementLong(long inc){
    count.incrementAndGet();
    value.incrementLong(inc);
    doReset();
  }
  
  private void doIncrementDouble(double inc){
    count.incrementAndGet();
    value.incrementDouble(inc);
    doReset();
  }   
  
  private void doIncrementInt(int inc){    
    count.incrementAndGet();
    value.incrementInt(inc);
    doReset();
  }
  
  protected void onPreReset(){}
  protected void onPostReset(){}  
  
}
