package org.sapia.ubik.rmi.server.perf;

/**
 * Encapsulates logic pertaining to manipulation of a statistic value.
 * 
 * @author yduchesne
 *
 */
public class StatValue{
  
  private double _value = 0;
  
  /**
   * Calculates an average, based on the given count 
   * (this instance's value divided by the count).
   * 
   * @param count a count.
   * @return the average.
   */
  public double avg(int count) {
    if(count == 0){
      return _value;
    }
    else{
      return _value/count;
    }
  }
  
  /**
   * @return this instance's current value.
   */
  public double get() {
    return _value;
  }
  
  /**
   * @param val the value to set as this instance's current one.
   */
  public void set(double val) {
    _value = val;
  }
  
  /**
   * Increments this instance's current value by the given
   * increment.
   * 
   * @param val an increment.
   */
  public void incrementLong(long val) {
    _value = _value + val;
  }
  
  /**
   * @see #incrementLong(long)
   */
  public void incrementInt(int val) {
    _value = _value + val;
  }

  /**
   * @see #incrementLong(long)
   */  
  public void incrementDouble(double val) {
    _value = _value + val;
  }
  
  public String toString(){
    return Double.toString(_value);
  }
}
