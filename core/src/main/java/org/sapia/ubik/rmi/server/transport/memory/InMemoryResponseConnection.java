package org.sapia.ubik.rmi.server.transport.memory;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

/**
 * This class corresponds to the server-side of the in-memory connection.
 * 
 * @author yduchesne
 * 
 */
public class InMemoryResponseConnection implements RmiConnection {

  private InMemoryAddress address;
  private InMemoryRequest request;

  /**
   * @param address
   *          the {@link InMemoryAddress} of the server corresponding to this
   *          instance.
   * @param request
   *          the {@link InMemoryRequest} that was made.
   */
  public InMemoryResponseConnection(InMemoryAddress address, InMemoryRequest request) {
    this.address = address;
    this.request = request;
  }

  @Override
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
    return request.getData();
  }

  @Override
  public ServerAddress getServerAddress() {
    return address;
  }

  @Override
  public void close() {
  }

  @Override
  public void send(Object toSend) throws IOException, RemoteException {
    request.setResponse(toSend);
  }

  @Override
  public void send(Object toSend, VmId associated, String transportType) throws IOException, RemoteException {
    request.setResponse(toSend);
  }

}
