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
  
  private StatisticKey  key;
  private String        description;
  private StatValue     value    = new StatValue();
  private AtomicInteger count    = new AtomicInteger();
  volatile boolean      enabled;
  
  /**
   * @param source the stat's source.
   * @param name the stat's name.
   * @param description the stat's description.
   */
  public Statistic(String source, String name, String description){
    this.key = new StatisticKey(source, name);
    this.description = description;
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
   * @return this instance's average.
   */
  public double getStat(){
    double stat = value.avg(count.get());
    reset();
    return stat;
  }
  
  /**
   * Internally resets this instance's counter.
   */
  public void reset(){
    onPreReset();      
    value.set(0);      
    count.set(0);
    onPostReset();      
  } 
  
  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(StatCapable other) {
    return getKey().compareTo(other.getKey());
  }
  
  public String toString(){
    return key.toString();
  }
  
  private void doIncrementLong(long inc){
    count.incrementAndGet();
    value.incrementLong(inc);
  }
  
  private void doIncrementDouble(double inc){
    count.incrementAndGet();
    value.incrementDouble(inc);
  }   
  
  private void doIncrementInt(int inc){    
    count.incrementAndGet();
    value.incrementInt(inc);
  }
  
  protected void onPreReset(){}
  protected void onPostReset(){}  
  
}
