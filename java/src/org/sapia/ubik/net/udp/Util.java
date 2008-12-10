package org.sapia.ubik.net.udp;

import java.io.*;

import java.net.DatagramPacket;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Util {
  /**
   * Constructor for Util.
   */
  public Util() {
    super();
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
