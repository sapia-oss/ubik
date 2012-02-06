package org.sapia.ubik.mcast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;


/**
 * @author Yanick Duchesne
 */
public final class McastUtil {

  /**
   * Private constructor.
   */
  private McastUtil() {
  }
  
  public static Object fromDatagram(DatagramPacket pack)
    throws IOException, ClassNotFoundException {
    ByteArrayInputStream bis = new ByteArrayInputStream(pack.getData(),
        pack.getOffset(), pack.getLength());
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
  
  public static Object fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
    ObjectInputStream    ois = null;
    
    try {
      ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
      Object o = ois.readObject();
      return o;
    } finally {
      if (ois != null) {
        ois.close();
      }
    }
  }  

  public static byte[] toBytes(Object o, int bufsize) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsize);
    ObjectOutputStream    ous = new ObjectOutputStream(bos);

    ous.writeObject(o);
    ous.flush();
    ous.close();

    return bos.toByteArray();
  }

  public static int getSizeInBytes(Object o) throws IOException {
    return toBytes(o, 1000).length;
  }
}
