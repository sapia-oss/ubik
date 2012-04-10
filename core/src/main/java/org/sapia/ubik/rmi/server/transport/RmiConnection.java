package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.rmi.server.VmId;


/**
 * Inherits the {@link Connection} interface by adding the {@link #send(Object, VmId, String)} method.
 * 
 * @author Yanick Duchesne
 */
public interface RmiConnection extends Connection {
  /**
   * This method sends an object associated with the given {@link VmId}, and specifying
   * the given transport type.
   * 
   * @param toSend the {@link Object} to send.
   * @param associated the {@link VmId} corresponding to the JVM from which the given
   * object comes.
   * @param transportType the identifier of the {@link TransportProvider} to use to send the given
   * object.
   * 
   * @see org.sapia.ubik.net.Connection#send(Object)
   */
  public void send(Object toSend, VmId associated, String transportType)
    throws IOException, RemoteException;
}
