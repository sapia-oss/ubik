package org.sapia.ubik.jmx;

import javax.management.ObjectName;

/**
 * Holds a MBean and its {@link ObjectName}
 * 
 * @author yduchesne
 *
 */
public class MBeanContainer {
  
  private ObjectName _name;
  private Object _mbean;
  
  public MBeanContainer(ObjectName name, Object mbean){
    _name = name;
    _mbean = mbean;
  }
  
  /**
   * @return the {@link ObjectName} of the MBean that his held
   * in this instance.
   */
  public ObjectName getName(){
    return _name;
  }
  
  /**
   * @return a JMX Bean.
   */
  public Object getMBean(){
    return _mbean;
  }

}
