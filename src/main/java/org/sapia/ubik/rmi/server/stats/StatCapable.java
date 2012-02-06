package org.sapia.ubik.rmi.server.stats;

public interface StatCapable extends Comparable<StatCapable>{

  public boolean isEnabled();
  
  public String getSource();
  
  public String getName();
  
  public String getDescription();
  
  public double getValue();

}
