package org.sapia.ubik.rmi.server.stats;

import org.sapia.ubik.util.Strings;

/**
 * Identifies a {@link Statistic} uniquely.
 *  
 * @author yduchesne
 *
 */
public class StatisticKey implements Comparable<StatisticKey>{

  private String source;
  private String name;
  
  public StatisticKey(String source, String name) {
    this.source = source;
    this.name   = name;
  }
  
  /**
   * @return this instance's source.
   */
  public String getSource() {
    return source;
  }
  
  /**
   * @return the name that was passed to this instance, concatenated with the counter (if any).
   */
  public String getName() {
    return name;
  }
  
  @Override
  public int hashCode() {
    return source.hashCode() * 31 + name.hashCode() * 31;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof StatisticKey) {
      StatisticKey other = (StatisticKey)obj;
      return source.equals(other.source) && name.equals(other.name);
    }
    return false;
  }
  
  @Override
  public int compareTo(StatisticKey o) {
  	int c = this.source.compareTo(o.getSource());
  	if (c == 0) {
  		c = this.name.compareTo(o.getName());
  	}
    return c;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "source", source, "name", "name");
  }
}
