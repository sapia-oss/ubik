package org.sapia.ubik.net.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;


/**
 * Provides UDP-related utility methods.
 * 
 * @author Yanick Duchesne
 */
public final class Util {
  
  private Util() {
  }

  public static Object fromDatagram(DatagramPacket pack) throws IOException, ClassNotFoundException {
    ByteArrayInputStream bis = new ByteArrayInputStream(pack.getData(), pack.getOffset(), pack.getLength());
    ObjectInputStream    ois = null;

    try {
      ois = new ObjectInputStream(bis);

      Object o = ois.readObject();

      return o;
    } finally {
      if (ois != null) {
        ois.close();
      }
    }
  }

  public static byte[] toBytes(Object o, int bufsize, ObjectStreamFactory fac)
    throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsize);
    ObjectOutputStream    ous = fac.toOutput(bos);

    ous.writeObject(o);
    ous.flush();
    ous.close();

    return bos.toByteArray();
  }

  public static int getSizeInBytes(Object o) throws IOException {
    return toBytes(o, 1000, ObjectStreamFactory.DEFAULT_FACTORY).length;
  }
}
