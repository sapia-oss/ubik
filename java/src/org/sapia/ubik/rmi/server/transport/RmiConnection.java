package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.rmi.server.VmId;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface RmiConnection extends Connection {
  /**
   * @see org.sapia.ubik.net.Connection#send(Object)
   */
  public void send(Object o, VmId associated, String transportType)
    throws IOException, RemoteException;
}
