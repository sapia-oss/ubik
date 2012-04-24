package org.sapia.ubik.rmi.server.stats;

/**
 * Encapsulates logic pertaining to manipulation of a statistic value. An instance of this class
 * keeps a statistic's current value as a <code>double</code>. Operations on the value (such as averaging and
 * incrementing) are not atomic, since this class does not provide synchronized method, and there is no
 * <code>AtomicDouble</code> type.
 * <p>
 * Therefore, the value held by an instance of this class should at no time be considered exact - it will 
 * always be an approximation, due to the non-deterministic nature this class support.
 * <p>
 * For performing monitoring/performance calculations, this approach is deemed good enough, since in that
 * context trends are more important that fine-grained results. 
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
