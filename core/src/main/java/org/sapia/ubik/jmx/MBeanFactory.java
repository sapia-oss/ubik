package org.sapia.ubik.jmx;

import javax.management.ObjectName;

/**
 * This interface specifies a method for MBean creation.
 * 
 * @author yduchesne
 * 
 */
public interface MBeanFactory {

  /**
   * Internally creates a MBean and its {@link ObjectName}, that are returned in
   * a {@link MBeanContainer}.
   * 
   * @return a {@link MBeanContainer}
   * @throws Exception
   *           if a problem occurs while creating the MBean or its object name.
   */
  public MBeanContainer createMBean() throws Exception;

}
