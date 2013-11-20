package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * This class is used to unmarshal incoming responses from servers.
 * 
 * @see org.sapia.ubik.rmi.server.Server
 * @see org.sapia.ubik.rmi.server.transport.RmiConnection
 * @see org.sapia.ubik.rmi.server.transport.TransportProvider
 * 
 * @author Yanick Duchesne
 */
public class MarshalInputStream extends ObjectInputStream {
  MarshalInputStream(InputStream is) throws IOException {
    super(is);
    super.enableResolveObject(true);
  }

  /**
   * @see java.io.ObjectInputStream#resolveObject(Object)
   */
  protected Object resolveObject(Object arg) throws IOException {
    return arg;
  }

  protected Object readObjectOverride() throws IOException, ClassNotFoundException {
    return super.readUnshared();
  }

}
