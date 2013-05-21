package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.serialization.JBossSerializationDetector;

/**
 * This class creates {@link ObjectOutputStream} and {@link ObjectInputStream} instances, which are based either on the
 * JBoss serialization library, or on the default implementations that come with the JDK.
 * 
 * @see JBossSerializationDetector
 * 
 * @author yduchesnes
 */
public class MarshalStreamFactory {
  
  private static Category log = Log.createCategory(MarshalStreamFactory.class);
	
  public static ObjectOutputStream createOutputStream(OutputStream out) throws IOException {
    if(JBossSerializationDetector.isJbossSerializationDetected()) {
      log.debug("JBoss serialization lib detected in classpath, returning JBossMarshalOutputStream");
      return new JBossMarshalOutputStream(out);
    } else {
      return new MarshalOutputStream(out);
    }
  }
  
  public static ObjectInputStream createInputStream(InputStream in) throws IOException {
    if(JBossSerializationDetector.isJbossSerializationDetected()) {
      log.debug("JBoss serialization lib detected in classpath, returning JBossMarshalInputStream");      
      return new JBossMarshalInputStream(in);
    } else {
      return new MarshalInputStream(in);
    }
  } 
}
