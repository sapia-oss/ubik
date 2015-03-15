package org.sapia.ubik.rmi.server.transport.memory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

/**
 * This class corresponds to the client-side of the in-memory connection.
 * 
 * @author yduchesne
 * 
 */
public class InMemoryRequestConnection implements RmiConnection {

  private boolean useMarshalling;
  private InMemoryAddress address;
  private InMemoryTransportProvider provider;
  private InMemoryRequest request;

  InMemoryRequestConnection(boolean useMarshalling, InMemoryAddress address, InMemoryTransportProvider provider) {
    this.useMarshalling = useMarshalling;
    this.address = address;
    this.provider = provider;
  }

  @Override
  public void send(Object toSend) throws IOException, RemoteException {
    request = new InMemoryRequest(useMarshalling, toSend);
    InMemoryServer server = (InMemoryServer) provider.getServerFor(address);
    server.handle(request);
  }

  @Override
  public void send(Object toSend, VmId associated, String transportType) throws IOException, RemoteException {
    request = new InMemoryRequest(useMarshalling, toSend);
    InMemoryServer server = (InMemoryServer) provider.getServerFor(address);
    server.handle(request);
  }

  @Override
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
    if (request == null)
      return null;
    try {
      return request.waitForResponse();
    } catch (InterruptedException e) {
      throw new RemoteException("Interrupted while waiting for response", e);
    }
  }
  
  @Override
  public Object receive(long timeout) throws IOException,
      ClassNotFoundException, RemoteException, SocketTimeoutException {
    if (request == null)
      return null;
    try {
      return request.waitForResponse(timeout);
    } catch (InterruptedException e) {
      throw new RemoteException("Interrupted while waiting for response", e);
    }  
  }

  @Override
  public void close() {
  }

  @Override
  public ServerAddress getServerAddress() {
    return address;
  }

}
