package org.sapia.ubik.rmi.server.perf;

/**
 * Calculates hits per minute.
 * 
 * @author yduchesne
 *
 */
public class HitsPerMinStatistic extends HitsPerSecStatistic{
  
  HitsPerMinStatistic(String name){
    super(name);
  }
  
  HitsPerMinStatistic(String name, long sampleRate){
    super(name, sampleRate);
  }  
  
  protected double convertMillis(long delay) {
    return ((double)delay) / 1000 / 60;
  }

}
