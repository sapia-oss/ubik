package org.sapia.ubik.rmi.server.perf;

/**
 * Calculates hits per second. 
 * 
 * @author yduchesne
 *
 */
public class HitsPerSecStatistic extends Statistic{
  
  private long _sampleRate = 1000;
  private long _startTime = System.currentTimeMillis();
  private int _hits;
  
  /**
   * @param name the name of this statistic.
   */
  HitsPerSecStatistic(String name){
    super(name);
  }

  /**
   * @param name the name of this statistic.
   * @param sampleRate the rate at which hits are sampled (in millis).
   */
  HitsPerSecStatistic(String name, long sampleRate){
    super(name);
    _sampleRate = sampleRate;
  }  
  
  /**
   * This method must be called by client applications to increment
   * the int counter.
   */
  public synchronized void hit(){
    _hits++;
  }
  
  /**
   * This method must be called by client applications to increment
   * the int counter.
   * 
   * @param increment
   */
  public synchronized void hit(int increment){
    _hits+=increment;
  }  
  
  /**
   * @return the start time of the current sample interval, in millis.
   */
  public long getStartTime(){
    return _startTime;
  }
  
  public synchronized double getStat() {
    if(System.currentTimeMillis() - _startTime > _sampleRate){
      double timebase = convertMillis((System.currentTimeMillis()-_startTime));
      double ratio    = _hits/timebase;      
      super.incrementDouble(ratio);
      _hits = 0;
      _startTime = System.currentTimeMillis();
    }
    return super.getStat();
  }
  
  protected synchronized void onPostReset() {
    _hits = 0;
    _startTime = System.currentTimeMillis();
  }
  
  protected double convertMillis(long delay){
    return ((double)delay)/ 1000;
  }

}

