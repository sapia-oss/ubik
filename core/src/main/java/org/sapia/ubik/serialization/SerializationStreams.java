package org.sapia.ubik.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;

/**
 * Uses the {@link JBossSerializationDetector} class to determine the
 * {@link ObjectInputStream} and {@link ObjectOutputStream} classes provided as
 * part of the JBoss serialization library should be used.
 * 
 * @author yduchesne
 * 
 */
public class SerializationStreams {

  private SerializationStreams() {
  }

  public static ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
    if (JBossSerializationDetector.isJbossSerializationDetected()) {
      return new JBossObjectOutputStream(os);
    } else {
      return new ObjectOutputStream(os);
    }
  }

  public static ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
    if (JBossSerializationDetector.isJbossSerializationDetected()) {
      return new JBossObjectInputStream(is);
    } else {
      return new ObjectInputStream(is);
    }
  }
}
