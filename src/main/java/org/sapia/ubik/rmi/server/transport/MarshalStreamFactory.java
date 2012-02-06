package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class MarshalStreamFactory {
  
  private static boolean isJbossSerializationEnabled;
  
  static {
    try {
      Class.forName("org.jboss.serial.io.JBossObjectInputStream");
      isJbossSerializationEnabled = true;
    } catch (ClassNotFoundException e) {
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
