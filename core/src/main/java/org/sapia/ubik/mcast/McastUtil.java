package org.sapia.ubik.mcast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.serialization.SerializationStreams;


/**
 * @author Yanick Duchesne
 */
public final class McastUtil {
	
	private static Category log = Log.createCategory(McastUtil.class);

  /**
   * Private constructor.
   */
  private McastUtil() {
  }
  
  public static Object fromDatagram(DatagramPacket pack)
    throws IOException, ClassNotFoundException {
  	
  	log.debug("Deserializing from packet (offset=%s, length=%s)", pack.getOffset(), pack.getLength());
  	
    ByteArrayInputStream bis = new ByteArrayInputStream(pack.getData(), pack.getOffset(), pack.getLength());
    ObjectInputStream    ois = null;
    
    try {
      ois = SerializationStreams.createObjectInputStream(bis);

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

  	log.debug("Deserializing bytes (length=%s)", bytes.length);

    try {
      ois = SerializationStreams.createObjectInputStream(new ByteArrayInputStream(bytes));
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
    ObjectOutputStream    ous = SerializationStreams.createObjectOutputStream(bos);

    ous.writeObject(o);
    ous.flush();
    ous.close();

    byte[] toReturn = bos.toByteArray();
  	log.debug("Serializing %s as bytes (length=%s)", o, toReturn.length);
  	return toReturn;
  }

  public static int getSizeInBytes(Object o) throws IOException {
    return toBytes(o, 1000).length;
  }
  

  
}
