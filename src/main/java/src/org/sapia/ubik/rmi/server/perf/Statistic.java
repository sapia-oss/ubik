package org.sapia.ubik.rmi.server.perf;


/**
 * This class models a statistic. A statistic can be incremented and
 * provides an average over a given number of occurrences.  
 * 
 * @author yduchesne
 *
 */
public class Statistic {
  
  static final int DEFAULT_MAX_COUNT = 25;

  private String _name;
  private StatValue _value = new StatValue();
  private int _count, _maxCount;
  private boolean _enabled;
  
  /**
   * Creates a statistic with a max count of 100 by default.
   * 
   * @see #Statistic(String, StatValue, int)
   */
  public Statistic(String name){
    this(name, DEFAULT_MAX_COUNT);
  }
  
  /**
   * @param name the stat's name.
   * @param maxCount the maximum number of occurrences over which averages
   * are to be calculated. Once that number is reached, the value of the
   * statistic is set to its current average, and the internal count is
   * reset to 0. 
   */
  public Statistic(String name, int maxCount){
    _name = name;
    _maxCount = maxCount;
  }
  
  /**
   * @param enabled if <code>true</code>, stat calculation will
   * be enabled.
   */
  public Statistic setEnabled(boolean enabled){
    _enabled = enabled;
    return this;
  }
  
  /**
   * @see #setEnabled(boolean)
   */
  public boolean isEnabled(){
    return _enabled;
  }  
  
  /**
   * @return this stat's name.
   */
  public String getName(){
    return _name;
  }
  
  /**
   * @return this stat's value.
   */
  public double getValue(){
    return _value.get();
  }
  
  /**
   * Adds the given long to this instance. Also internally increments the
   * counter in order to calculate averages consistently.
   * 
   * @param inc the long to use for the incrementation.
   */
  public void incrementLong(long inc){
    if(_enabled){
      doIncrementLong(inc);
    }
  }
  
  /**
   * @see #incrementLong(long)
   */
  public void incrementDouble(double inc){
    if(_enabled){
      doIncrementDouble(inc);
    }
  }  
  
  /**
   * @see #incrementLong(long)
   */
  public void incrementInt(int inc){
    if(_enabled){
      doIncrementInt(inc);
    }
  }
 
  /**
   * @return this instance's average, in one of the primitive wrappers (Integer, Long, Float, Double).
   */
  public double getStat(){
    return _value.avg(_count);
  }
  
  public void reset(){
    doReset();
  } 
  
  private synchronized void doReset(){
    if(_count > _maxCount){
      onPreReset();      
      _value.set(_value.avg(_count));      
      _count = 0;
      onPostReset();      
    }    
  }  
  
  public String toString(){
    return "["+_name+" = "+_value+"]";
  }
  
  private synchronized void doIncrementLong(long inc){
    _count++;
    _value.incrementLong(inc);
    doReset();
  }
  
  private synchronized void doIncrementDouble(double inc){
    _count++;
    _value.incrementDouble(inc);
    doReset();
  }   
  
  private synchronized void doIncrementInt(int inc){    
    _count++;
    _value.incrementInt(inc);
    doReset();
  }
  
  protected void onPreReset(){}
  protected void onPostReset(){}  
  
}
