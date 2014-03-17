package org.sapia.ubik.jmx;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

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
  public static boolean isJmxEnabled() {
    return new Conf().addProperties(System.getProperties()).getBooleanProperty(Consts.JMX_ENABLED, false);
  }

  /**
   * @param name
   *          the {@link ObjectName} under which the register the given MBean.
   * @param mbean
   *          JMX Bean.
   */
  public static void registerMBean(ObjectName name, Object mbean) {
    if (isJmxEnabled()) {
      List<MBeanServer> list = MBeanServerFactory.findMBeanServer(null);
      for (int i = 0; i < list.size(); i++) {
        MBeanServer server = list.get(i);
        try {
          server.registerMBean(mbean, name);
        } catch (Exception e) {
          throw new IllegalStateException("Could not register MBean " + name, e);
        }
      }
    }
  }

  /**
   * Creates an {@link ObjectName} corresponding to the given class'
   * "simple name".
   * 
   * @param clazz
   *          the {@link Class} for which to create a corresponding
   *          {@link ObjectName}.
   * @return the {@link ObjectName} corresponding to the given class.
   * @see #createObjectName(String)
   */
  public static ObjectName createObjectName(Class<?> clazz) {
    return createObjectName(clazz.getSimpleName());
  }

  /**
   * Creates an {@link ObjectName} which has a name of the form:
   * 
   * <pre>
   * sapia.ubik.rmi:type=$name
   * </pre>
   * 
   * Where $name corresponds to the name provided by the caller.
   * 
   * @param name
   *          a name.
   * @return the {@link ObjectName} corresponding to the given name.
   */
  public static ObjectName createObjectName(String name) {
    try {
      return new ObjectName("sapia.ubik.rmi:type=" + name);
    } catch (MalformedObjectNameException e) {
      throw new IllegalArgumentException("Could not create JMX object name for " + name, e);
    }
  }

}
