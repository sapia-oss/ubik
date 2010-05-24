package org.sapia.ubik.rmi.server.transport.http.servlet;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.Server;

import java.rmi.RemoteException;


/**
 * Implementation of the <code>Server</code> interface. Instances of this
 * class are created by a <code>ServletTransportProvider</code>.
 *
 * @see org.sapia.ubik.rmi.server.transport.http.servlet.ServletTransportProvider#newServer(Properties)
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServletServer implements Server {
  private ServletAddress _addr;

  ServletServer(ServletAddress addr) {
    _addr = addr;
  }

  ServletServer(String servletUrl) throws UriSyntaxException {
    _addr = new ServletAddress(servletUrl);
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#close()
   */
  public void close() {
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() throws RemoteException {
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return _addr;
  }
}
