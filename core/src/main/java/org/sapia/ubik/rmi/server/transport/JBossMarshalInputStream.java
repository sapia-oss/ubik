package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.serial.io.JBossObjectInputStream;

public class JBossMarshalInputStream extends JBossObjectInputStream {
  
  JBossMarshalInputStream(InputStream is) throws IOException {
    super(is);
    super.enableResolveObject(true);
  }

  /**
   * @see java.io.ObjectInputStream#resolveObject(Object)
   */
  protected Object resolveObject(Object arg) throws IOException {
    return arg;
  }

}
