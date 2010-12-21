package org.sapia.ubik.rmi.server.perf;


/**
 * An instance of this class is used to calculate the duration of
 * an arbitrary operation. Durations are averaged over time.
 * <p>
 * Usage:
 * <pre>
 *  for(int i = 0; i &lt; 1000;`i++){
 *    Topic t = ....
 *    t.start();
 *    // do something
 *    t.end();
 *  }
 *  System.out.println(t.duration());
 * </pre>
 * 
 * @see PerfAnalyzer
 * 
 * @author Yanick Duchesne
 */
public class Topic implements Comparable<Topic> {
  private static int _count     = 0;
  private long       _startTime = -1;
  private long       _endTime   = -1;
  private Statistic  _stat;
  private int        _id;

  /**
   * Constructor for Topic.
   */
  public Topic(String name) {
    _stat = new Statistic(name);    
    _stat.setEnabled(true);
    _id     = inc();
  }
  
  /**
   * @return the name of this instance.
   */
  public String getName() {
    return _stat.getName();
  }

  /**
   * Internally sets the start time of this instance.
   */
  public void start() {
    _startTime = System.currentTimeMillis();
  }

  /**
   * Calculates the current duration corresponding to this
   * instance, based on its internal start time and system
   * time.
   * 
   * @see #start()
   */
  public void end() {
    _endTime = System.currentTimeMillis();
    long newDuration = _endTime - _startTime;
    _stat.incrementLong(newDuration);
  }

  /**
   * @return the computed average duration corresponding to 
   * this instance.
   */
  public double duration() {
    return _stat.getStat();
  }
  
  /**
   * @param enabled if <code>true</code>, turns this instance's
   * sampling mode to "on".
   */
  public void setEnabled(boolean enabled){
    _stat.setEnabled(enabled);
  }
  
  /**
   * @return <code>true</code> if stat calculation is performed by this
   * instance.
   */
  public boolean isEnabled(){
    return _stat.isEnabled();
  }
  
  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Topic other) {
    return _id - other._id;
  }

  static synchronized int inc() {
    return _count++;
  }
}
