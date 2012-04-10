package org.sapia.ubik.rmi.server.stats;

/**
 * Encapsulates logic pertaining to manipulation of a statistic value.
 * 
 * @author yduchesne
 *
 */
public class StatValue {
    
  private volatile double value = 0;
  
  /**
   * Calculates an average, based on the given count 
   * (this instance's value divided by the count).
   * 
   * @param count a count.
   * @return the average.
   */
  public double avg(int count) {
    if(count == 0){
      return value;
    }
    else{
      return value / count;
    }
  }
  
  /**
   * @return this instance's current value.
   */
  public double get() {
    return value;
  }
  
  /**
   * @param val the value to set as this instance's current one.
   */
  public void set(double val) {
    value = val;
  }
  
  /**
   * Increments this instance's current value by the given
   * increment.
   * 
   * @param val an increment.
   */
  public void incrementLong(long val) {
    value = value + val;
  }
  
  /**
   * @see #incrementLong(long)
   */
  public void incrementInt(int val) {
    value = value + val;
  }

  /**
   * @see #incrementLong(long)
   */  
  public void incrementDouble(double val) {
    value = value + val;
  }
  
  public String toString(){
    return Double.toString(value);
  }
}
