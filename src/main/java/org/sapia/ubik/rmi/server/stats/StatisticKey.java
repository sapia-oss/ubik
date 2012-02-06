package org.sapia.ubik.rmi.server.stats;

/**
 * Identifies a {@link Statistic} uniquely. 
 * @author yduchesne
 *
 */
public class StatisticKey {

  private String source;
  private String name;
  private int    counter;
  
  public StatisticKey(String source, String name) {
    this.source = source;
    this.name   = name;
  }
  
  public String getSource() {
    return source;
  }
  
  public String getName() {
    if(counter == 0) {
      return name;
    } else { 
      return name + "-" + counter;
    }
  }
  
  void setCounter(int counter) {
    this.counter = counter;
  }
  
  @Override
  public int hashCode() {
    return source.hashCode() * 31 + name.hashCode() * 31 + counter * 31;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof StatisticKey) {
      StatisticKey other = (StatisticKey)obj;
      return source.equals(other.source) && name.equals(other.name) && counter == other.counter;
    }
    return false;
  }
  
  @Override
  public String toString() {
    return "[source=" + source + ", name=" + name + "]";
  }
}
