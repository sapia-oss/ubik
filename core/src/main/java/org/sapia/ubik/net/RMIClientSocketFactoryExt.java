package org.sapia.ubik.net;

import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

/**
 * Extends the JDK's {@link RMIClientSocketFactory} interface to specify an additional method for creating
 * {@link Socket}s. This is a workaround in order to allow binding the socket (through {@link Socket#connect(java.net.SocketAddress, int)})
 * within a given timeout.
 * 
 * @author yduchesne
 *
 */
public interface RMIClientSocketFactoryExt extends RMIClientSocketFactory {
  
  /**
   * @return a new {@link Socket}, which has not been bound to a given host/port yet.
   */
  public Socket createSocket();

}
