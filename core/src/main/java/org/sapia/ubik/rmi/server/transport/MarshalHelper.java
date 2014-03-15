package org.sapia.ubik.rmi.server.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.ServerTable;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.serialization.SerializationStreams;

/**
 * Holds utility methods pertaining to the serialization/deserialization of remote objects.
 *
 * @author yduchesne
 *
 */
public class MarshalHelper {

  private static Category LOG = Log.createCategory(MarshalHelper.class);

  private MarshalHelper() {
  }

  /**
   * This method performs the serialization of the given object, attempting to perform the conversion of remote objects
   * to stubs in the process. This conversion will be attempted only if there's one, and only one, currently active server
   * in the {@link ServerTable}. Otherwise, serialization proceeds as normal.
   * <p>
   * This method closes the provided stream once done.
   *
   * @param toSerialize the Object to serialize.
   * @param destination the {@link OutputStream} to write to.
   * @throws IOException if
   */
  public static void serialize(Object toSerialize, OutputStream destination) throws IOException {
    ObjectOutputStream oos = null;
    if (!Hub.isShutdown()) {
      ServerTable st = Hub.getModules().getServerTable();
      if (st.getServerCount() == 1) {
        oos = MarshalStreamFactory.createOutputStream(destination);
        String transportType = st.getServerTypes().iterator().next();
        ((RmiObjectOutput) oos).setUp(VmId.getInstance(), transportType);
      } else {
        LOG.debug("Could not perform remote object conversion to stub: current server could not be guessed");
        oos = SerializationStreams.createObjectOutputStream(destination);
      }
    } else {
      oos = SerializationStreams.createObjectOutputStream(destination);
    }
    oos.writeObject(toSerialize);
    oos.flush();
    oos.close();
  }

  /**
   * Performs deserialization, closes the provided stream once done.
   *
   * @param is the {@link InputStream} from which to deserialize.
   * @return the {@link Object} resulting from deserialization.
   */
  public static Object deserialize(InputStream is) throws ClassNotFoundException, IOException {
    ObjectInputStream ois = MarshalStreamFactory.createInputStream(is);
    try {
      return ois.readObject();
    } finally {
      is.close();
    }
  }

  /**
   * @param input an array of bytes, holding data do deserialize.
   * @return the {@link Object} resulting from deserialization.
   */
  public static Object deserialize(byte[] input) throws ClassNotFoundException, IOException {
    ObjectInputStream ois = MarshalStreamFactory.createInputStream(new ByteArrayInputStream(input));
    return ois.readObject();
  }
}
