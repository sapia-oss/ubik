package org.sapia.ubik.rmi.server.stats;

/**
 * An immutable {@link StatCapable} implementation.
 * 
 * @author yduchesne
 *
 */
class ImmutableStat implements StatCapable, Comparable<StatCapable>{
	
	private   StatisticKey key;
  private   String  		 description;
  protected double  		 value;
  private   boolean 		 isEnabled;
  
  ImmutableStat(StatisticKey key, String description, double value, boolean isEnabled) {
  	this.key         = key;
    this.description = description;
    this.value       = value;
    this.isEnabled   = isEnabled;
  }

  @Override
  public StatisticKey getKey() {
    return key;
  }
  
  @Override
  public String getDescription() {
    return description;
  }
  
  @Override
  public double getValue() {
    return value;
  }
  
  @Override
  public boolean isEnabled() {
    return isEnabled;
  }
  
  public int compareTo(StatCapable other) {
    int c = key.getSource().compareTo(other.getKey().getSource());
    if(c == 0) {
      c = key.getName().compareTo(other.getKey().getName());
    }
    return c;
  }
}
