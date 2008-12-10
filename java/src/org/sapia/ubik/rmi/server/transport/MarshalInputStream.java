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
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MarshalInputStream extends ObjectInputStream {
  public MarshalInputStream(InputStream is) throws IOException {
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
  
  /*
  protected void readStreamHeader() throws IOException, StreamCorruptedException {
  }*/
}
