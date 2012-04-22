package org.sapia.ubik.rmi.server.stats;

import org.sapia.ubik.util.Clock;

/**
 * An instance of this class is used to calculate the duration of
 * an arbitrary operation. Durations are averaged over time.
 * <p>
 * Usage:
 * <pre>
 *  Timer t = ....
 *  t.start();
 *  // do something
 *  t.end();
 *  System.out.println(t.getValue());
 * </pre>
 * 
 * @see Stats
 * 
 * @author Yanick Duchesne
 */
public class Timer implements StatCapable {
	
	private Clock 						clock;
  private ThreadLocal<Long> start = new ThreadLocal<Long>();
  private Statistic  			  stat;
  
  /**
   * Constructor for Topic.
   */
  Timer(Statistic stat, Clock clock) {
  	this.clock = clock;
    this.stat  = stat;
    start.set(clock.nanoTime());
  }
  
  @Override
  public StatisticKey getKey() {
    return stat.getKey();
  }
  
  /**
   * @return this instance's description.
   */
  @Override
  public String getDescription() {
    return stat.getDescription();
  }

  /**
   * Internally sets the start time of this instance.
   */
  public void start() {
    if(stat.enabled) {
      start.set(clock.nanoTime());
    }
  }

  /**
   * Calculates the current duration corresponding to this
   * instance, based on its internal start time and system
   * time.
   * 
   * @see #start()
   */
  public double end() {
    if(stat.enabled) {
      double duration = StatsTimeUnit.MILLISECONDS.convertFrom(clock.nanoTime() - start.get(), StatsTimeUnit.NANOSECONDS);
      stat.incrementDouble(duration);
      return duration;
    }
    return 0;
  }

  /**
   * Adds the given duration to this instance.
   * 
   * @param duration a duration, in millis.
   */
  public void increase(long duration) {
    if(stat.enabled) {
      if(duration > 0) { 
        stat.incrementLong(duration);
      }
    }
  }

  /**
   * @return the computed average duration corresponding to 
   * this instance.
   */
  public double getValue() {
    return stat.getStat();
  }
  
  /**
   * @param enabled if <code>true</code>, turns this instance's
   * sampling mode to "on".
   */
  public void setEnabled(boolean enabled){
    stat.setEnabled(enabled);
  }
  
  /**
   * @return <code>true</code> if stat calculation is performed by this
   * instance.
   */
  public boolean isEnabled(){
    return stat.isEnabled();
  }
  
  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(StatCapable other) {
  	return stat.getKey().compareTo(other.getKey());
  }
}
