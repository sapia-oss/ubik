package org.sapia.ubik.rmi.server.stats;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.ubik.jmx.JmxHelper;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.stats.HitsStatistic.Builder;
import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Function;
import org.sapia.ubik.util.Props;


/**
 * This class is used to collect performance data (in fact, average durations) about
 * arbitrary operations - internally kept in the for of {@link Timer}s.
 * <p>
 *   Usage:
 * <pre>
 *   StatsManager stats = ...
 *   Timer timer = stats.createTimer(getClass(), "OperationTime", "Avg operation time");
 *   
 *   public void doSomething(){
 *     t.start();
 *     
 *     // do something
 *     
 *     t.end();
 *   }
 * }
 * </pre>
 * 
 * @author Yanick Duchesne
 */
public final class Stats {
  
  private volatile boolean                       isEnabled;
  private Map<StatisticKey, Statistic>           stats      = Collections.synchronizedMap(
                                                                new HashMap<StatisticKey, Statistic>()
                                                              );
  
  private static Stats       instance  = new Stats();
  
  static {
    instance.isEnabled = new Props().addProperties(System.getProperties()).getBooleanProperty(Consts.STATS_ENABLED, false);
    JmxHelper.registerMBean(JmxHelper.createObjectName(Stats.class), new StatsMBean()); 
  }
  
  private Stats() {
  }
  
  /**
   * @return the singleton {@link Stats} instance.
   */
  public static Stats getInstance() {
    return instance;
  }
  
  /**
   * Removes all statistic from this instance.
   */
  public synchronized void clear() {
    stats.clear();
  }
  
  /**
   * @return <code>true</code> if average duration calculation is turned on.
   */
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Enables average duration calculation.
   */
  public synchronized void enable(){
    isEnabled = true;
    
    Condition<Statistic> enable = new Condition<Statistic>() {
      @Override
      public boolean apply(Statistic item) {
        item.setEnabled(true);
        return true;
      }
    };
    
    Collections2.forEach(stats.values(), enable);
  }

  /**
   * Disables average duration calculation.
   */  
  public synchronized void disable(){
    isEnabled = false;
    
    Condition<Statistic> disable = new Condition<Statistic>() {
      @Override
      public boolean apply(Statistic item) {
        item.setEnabled(false);
        return true;
      }
    };
    
    Collections2.forEach(stats.values(), disable);
  }  
  
  /**
   * @param source a {@link Class} corresponding to the source of the statistic (the short class name will internally be used).
   * @param name the timer name.
   * @param description the timer description.
   * @return a new {@link Timer}. 
   */
  public synchronized Timer createTimer(Class<?> source, String name, String description) {
    return createTimer(source.getSimpleName(), name, description);
  }

  /**
   * @param name the name of the timer to return.
   * @return a new {@link Timer}.
   */
  public synchronized Timer createTimer(String source, String name, String description) {
    TimerStatistic t = new TimerStatistic(source, name, description);
    t.setEnabled(isEnabled);
    add(t);
    return t.getTimer();
  }

  /**
   * @param source the statistic source.
   * @param name the statistic name.
   * @param description the statistic description.
   * 
   * @return a {@link Builder} corresponding to the given parameters.
   */
  public synchronized HitsStatistic.Builder getHitsBuilder(Class<?> source, String name, String description) {
    return new HitsStatistic.Builder(source.getSimpleName(), name, description);
  }  
  
  /**
   * @param source the statistic source.
   * @param name the statistic name.
   * @param description the statistic description.
   * 
   * @return a {@link Builder} corresponding to the given parameters.
   */
  public synchronized HitsStatistic.Builder getHitsBuilder(String source, String name, String description) {
    return new HitsStatistic.Builder(source, name, description);
  }
  
  /**
   * Returns the statistic corresponding to the given source and name.
   * 
   * @param source a statistic source.
   * @param name a statistic name.
   * @return the {@link Statistic} that corresponds to the given parameters.
   * @throws IllegalArgumentException if no such statistic could be found.
   */
  public Statistic getStatisticFor(String source, String name) {
    StatisticKey key  = new StatisticKey(source, name);
    Statistic    stat = stats.get(key);
    if(stat == null) {
      throw new IllegalArgumentException("No statistic found for: " + key + ", got: " + stats.keySet());
    }
    return stat;
  }
  
  /**
   * Returns the statistic that correspond to the given parameters. Uses the given class' simple name
   * as the source.
   * 
   * @param source the source {@link Class}.
   * @param name the statistic name.
   * @return the {@link Statistic} that corresponds to the given parameters.
   * @see #getStatisticFor(String, String)
   */
  public Statistic getStatisticFor(Class<?> source, String name) {
    return getStatisticFor(source.getSimpleName(), name);
  }
  
  /**
   * @return the {@link List} of {@link StatCapable}s instance that this instance holds.
   */
  public synchronized List<StatCapable> getStatistics() {
    List<StatCapable> stats = new ArrayList<StatCapable>();
    
    Function<StatCapable, Statistic> converter = new Function<StatCapable, Statistic>() {
      @Override
      public StatCapable call(Statistic s) {
        return new ImmutableStat(s.getKey(), s.getDescription(), s.getStat(), s.isEnabled());
      }
    };
    
    stats.addAll(Collections2.convertAsList(this.stats.values(), converter));

    Collections.sort(stats);
    return stats;
  }
  
  /**
   * @param customStat an arbitrary, app-specific {@link Statistic}.
   */
  public synchronized Stats add(Statistic stat) {
    if(!stats.containsKey(stat.getKey())) {
      stats.put(stat.getKey(), stat);
      stat.setEnabled(isEnabled);
    }
    return this;
  }
  
  public void dumpStats(PrintStream ps){
    ps.println("================= Ubik Stats Dump at " + new Date() + " =================");
    Collection<StatCapable> stats = getStatistics();
    
    for(StatCapable stat : stats){
      if(stat.isEnabled()){
        dumpStat(ps, stat.getKey().getName(), stat.getValue());
      }
    }
  }
  
  public void dumpStat(PrintStream ps, String name, double value){
    ps.print(name);
    ps.print(":");
    printSpace(ps, name);    
    ps.println(value);    
  }  
  
  private void printSpace(PrintStream ps, String name){
    int space = 60 - name.length();
    for(int i = 0; i < space; i++){
      ps.print(' ');
    }
  }
  
}
