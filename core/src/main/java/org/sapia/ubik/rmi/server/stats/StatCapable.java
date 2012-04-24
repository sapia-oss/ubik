package org.sapia.ubik.rmi.server.stats;

/**
 * Specifies a read-only interface for objects that provide stats data.
 * 
 * @author yduchesne
 *
 */
public interface StatCapable extends Comparable<StatCapable>{

	/**
	 * @return <code>true</code> if this instance is enabled.
	 */
  public boolean isEnabled();
  
  /**
   * @return this instance's {@link StatisticKey}.
   */
  public StatisticKey getKey();
    
  /**
   * @return this instance's description.
   */
  public String getDescription();
  
  /**
   * @return this instance's value.
   */
  public double getValue();

}
