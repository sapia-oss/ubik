package org.sapia.ubik.serialization;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.util.Props;

/**
 * Detects if the JBoss serialization implementation be used, based on the required library being found in the classpath, 
 * and if the {@link Consts#SERIALIZATION_PROVIDER} system property does not specify otherwise.
 *
 * @author yduchesne
 *
 */
public final class JBossSerializationDetector {
	
  private static boolean isJbossSerializationEnabled;
  
  private JBossSerializationDetector() {
  }
  
  static {
  	Props props = Props.getSystemProperties();
  	String serializationProvider = props.getProperty(Consts.SERIALIZATION_PROVIDER_JDK, Consts.SERIALIZATION_PROVIDER_JBOSS);
  	if(serializationProvider.equals(Consts.SERIALIZATION_PROVIDER_JBOSS)) {
      try {
        Class.forName("org.jboss.serial.io.JBossObjectInputStream");
        isJbossSerializationEnabled = true;
      } catch (Exception e) {
      	Log.warning(MarshalStreamFactory.class, "JBoss serialization classes not found in classpath, using standard serialization");
        isJbossSerializationEnabled = false;
      }
  	} else {
        isJbossSerializationEnabled = false;
  	}
  }
  
  /**
   * @return <code>true</code> if the JBoss serialization library has been detected and should used.
   */
  public static final boolean isJbossSerializationDetected() {
  	return isJbossSerializationEnabled;
  }

}
