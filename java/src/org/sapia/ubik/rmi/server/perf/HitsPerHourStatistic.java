package org.sapia.ubik.rmi.server.perf;

/**
 * Calculates hits per hour.
 * 
 * @author yduchesne
 *
 */
public class HitsPerHourStatistic extends HitsPerSecStatistic{
  
  HitsPerHourStatistic(String name){
    super(name);
  }
  
  HitsPerHourStatistic(String name, long sampleRate){
    super(name, sampleRate);
  }  
  
  protected double convertMillis(long delay) {
    return ((double)delay) / 1000 / 60 / 60;
  }

}
