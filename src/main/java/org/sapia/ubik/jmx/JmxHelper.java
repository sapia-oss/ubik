package org.sapia.ubik.jmx;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;

/**
 * Helper class providing static methods pertaining to JMX manipulations.
 * 
 * @author yduchesne
 *
 */
public class JmxHelper {

  /**
   * @return <code>true</code> if JMX has been enabled
   * @see Consts#JMX_ENABLED
   */
  public static boolean isJmxEnabled(){
    return new PropUtil().addProperties(System.getProperties())
      .getBooleanProperty(Consts.JMX_ENABLED, false);
  }
  
  /**
   * @param name the {@link ObjectName} under which the register the
   * given MBean.
   * @param mbean JMX Bean.
   * @throws Exception if a problem occurs while registering the MBean.
   */
  public static void registerMBean(ObjectName name, Object mbean) throws Exception{
    if(isJmxEnabled()){
      List<MBeanServer> list = MBeanServerFactory.findMBeanServer(null);
      for(int i = 0; i < list.size(); i++){
        MBeanServer server = list.get(i);
        server.registerMBean(mbean, name);
      }
    }
  }
}
