package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;

/**
 * This class creates {@link ObjectOutputStream} and {@link ObjectInputStream} instances, which are based either on the
 * JBoss serialization library, or on the default implementations that come with the JDK.
 * <p>
 * The JBoss implementation will be used if the corresponding library is found in the classpath, and the {@link Consts#SERIALIZATION_PROVIDER}
 * system property does not specify otherwise.
 * 
 * @author yduchesnes
 */
public class MarshalStreamFactory {
	
  private static boolean isJbossSerializationEnabled;
  
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

  public static ObjectOutputStream createOutputStream(OutputStream out) throws IOException {
    if(isJbossSerializationEnabled) {
      return new JBossMarshalOutputStream(out);
    } else {
      return new MarshalOutputStream(out);
    }
  }
  
  public static ObjectInputStream createInputStream(InputStream in) throws IOException {
    if(isJbossSerializationEnabled) {
      return new JBossMarshalInputStream(in);
    } else {
      return new MarshalInputStream(in);
    }
  } 
}
