package org.sapia.ubik.rmi.server.stats;


/**
 * Wraps a {@link HitsStatistic}: provides {@link #hit()} methods that perform the necessary logic and
 * hides {@link HitsStatistic} mutators.
 * 
 * @author yduchesne
 *
 */
public class Hits implements StatCapable {
  
  private HitsStatistic  stat;
  
  /**
   * @param name the name of this statistic.
   */
  Hits(HitsStatistic stat){
    this.stat = stat;
  }
  
  /**
   * This method must be called by client applications to increment
   * the int counter.
   */
  public void hit(){
    stat.increment();
  }
  
  /**
   * This method must be called by client applications to increment
   * the int counter.
   * 
   * @param increment
   */
  public void hit(int increment){
    stat.increment(increment);
  }    
  
  // --------------------------------------------------------------------------
  // StatsCapable interface
  
  @Override
  public boolean isEnabled() {
    return stat.isEnabled();
  }
  
  @Override
  public StatisticKey getKey() {
    return stat.getKey();
  }
  
  @Override
  public String getDescription() {
    return stat.getDescription();
  }
  
  @Override
  public double getValue() {
    return stat.getStat();
  }
  
  @Override
  public int compareTo(StatCapable other) {
    return stat.getKey().compareTo(other.getKey());
  }
}

