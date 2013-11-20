package org.sapia.ubik.rmi.server.transport.memory;

import java.rmi.RemoteException;

import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

/**
 * Implements the {@link Connections} interface by creating instances of
 * {@link InMemoryRequestConnection}.
 * 
 * @author yduchesne
 * 
 */
public class InMemoryConnections implements Connections {

  private boolean useMarshalling;
  private InMemoryAddress address;
  private InMemoryTransportProvider provider;

  /**
   * @param useMarshalling
   *          <code>true</code> if the connections used by this instance should
   *          marshal their data, <code>false</code> otherwise.
   * @param address
   *          the {@link InMemoryAddress} corresponding to the
   *          {@link InMemoryServer} to which this instance should create
   *          connections to.
   * @param provider
   *          the {@link InMemoryTransportProvider} to which this instance
   *          corresponds.
   */
  InMemoryConnections(boolean useMarshalling, InMemoryAddress address, InMemoryTransportProvider provider) {
    this.useMarshalling = useMarshalling;
    this.address = address;
    this.provider = provider;
  }

  @Override
  public String getTransportType() {
    return address.getTransportType();
  }

  @Override
  public RmiConnection acquire() throws RemoteException {
    return new InMemoryRequestConnection(useMarshalling, address, provider);
  }

  @Override
  public void release(RmiConnection conn) {
  }

  @Override
  public void clear() {
  }

  @Override
  public void invalidate(RmiConnection conn) {
  }

}
