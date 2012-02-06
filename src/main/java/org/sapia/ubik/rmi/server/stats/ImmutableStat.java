package org.sapia.ubik.rmi.server.stats;

class ImmutableStat implements StatCapable, Comparable<StatCapable>{
  
  private String  source;
  private String  name;
  private String  description;
  private double  value;
  private boolean isEnabled;
  
  public ImmutableStat(String source, String name, String description, double value, boolean isEnabled) {
    this.source      = source;
    this.name        = name;
    this.description = description;
    this.value       = value;
    this.isEnabled   = isEnabled;
  }
  
  @Override
  public String getSource() {
    return source;
  }
   
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public String getDescription() {
    return description;
  }
  
  @Override
  public double getValue() {
    return value;
  }
  
  public boolean isEnabled() {
    return isEnabled;
  }
  
  public int compareTo(StatCapable other) {
    
    int c = source.compareTo(other.getSource());
    if(c == 0) {
      c = name.compareTo(other.getName());
    }
    return c;
  }
}
