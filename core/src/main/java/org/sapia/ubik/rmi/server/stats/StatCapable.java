package org.sapia.ubik.rmi.server.stats;

public interface StatCapable extends Comparable<StatCapable>{

  public boolean isEnabled();
  
  public StatisticKey getKey();
    
  public String getDescription();
  
  public double getValue();

}
