package org.sapia.ubik.rmi.server.transport.http.servlet;

import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.Server;


/**
 * Implementation of the {@link Server} interface. Instances of this
 * class are created by a {@link ServletTransportProvider}.
 *
 * @see org.sapia.ubik.rmi.server.transport.http.servlet.ServletTransportProvider#newServer(Properties)
 *
 * @author Yanick Duchesne
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
