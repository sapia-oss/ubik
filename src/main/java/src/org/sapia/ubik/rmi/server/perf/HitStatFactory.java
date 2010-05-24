package org.sapia.ubik.rmi.server.perf;

/**
 * This factory is used to create instances of {@link HitsPerHourStatistic}, {@link HitsPerMinStatistic},
 * and {@link HitsPerSecStatistic}.
 * <p>
 * A {@link StatsCollector} may optionally be passed to each <code>createXXXX</code> method of this class.
 * If the passed in collector is not <code>null</code>, the created statistic is added to it.
 * <p>
 * Each <code>createXXXX</code> method also takes a sample rate (in millis). If the sample rate is <= 0, the
 * default one is used.
 * 
 * @author yduchesne
 *
 */
public class HitStatFactory {
  
  public static HitsPerHourStatistic createHitsPerHour(String name, long sampleRate, StatsCollector collector){
    HitsPerHourStatistic stat;
    if(sampleRate <= 0){
      stat = new HitsPerHourStatistic(name);
    }
    else{
      stat = new HitsPerHourStatistic(name, sampleRate);
    }
    doInit(stat, collector);
    return stat;
  }
  
  public static HitsPerMinStatistic createHitsPerMin(String name, long sampleRate, StatsCollector collector){
    HitsPerMinStatistic stat;
    if(sampleRate <= 0){
      stat = new HitsPerMinStatistic(name);
    }
    else{
      stat = new HitsPerMinStatistic(name, sampleRate);
    }
    doInit(stat, collector);
    return stat;
  }  
  
  public static HitsPerSecStatistic createHitsPerSec(String name, long sampleRate, StatsCollector collector){
    HitsPerSecStatistic stat;
    if(sampleRate <= 0){
      stat = new HitsPerSecStatistic(name);
    }
    else{
      stat = new HitsPerSecStatistic(name, sampleRate);
    }
    doInit(stat, collector);
    return stat;
  }    
  
  private static void doInit(Statistic stat, StatsCollector collector){
    stat.setEnabled(true);
    if(collector != null){
      collector.addStat(stat);
    }
  }

}
