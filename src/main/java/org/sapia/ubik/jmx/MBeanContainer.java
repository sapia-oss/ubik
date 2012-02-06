package org.sapia.ubik.jmx;

import javax.management.ObjectName;

/**
 * Holds a MBean and its {@link ObjectName}
 * 
 * @author yduchesne
 *
 */
public class MBeanContainer {
  
  private ObjectName name;
  private Object     mbean;
  
  public MBeanContainer(ObjectName name, Object mbean){
    this.name = name;
    this.mbean = mbean;
  }
  
  /**
   * @return the {@link ObjectName} of the MBean that his held
   * in this instance.
   */
  public ObjectName getName(){
    return this.name;
  }
  
  /**
   * @return a JMX Bean.
   */
  public Object getMBean(){
    return this.mbean;
  }

}
