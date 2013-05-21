package org.sapia.ubik.rmi.server.transport.netty;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.netty.NettyResponse;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.Assertions;

/**
 * @author Yanick Duchesne
 */
public class NettyRmiServerConnection implements RmiConnection {

  private ServerAddress addr;
  private Object        received;
  private NettyResponse response;

  NettyRmiServerConnection(ServerAddress addr, Object received) {
    this.addr     = addr;
    this.received = received; 
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    received = null;
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return addr;
  }

  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive() throws IOException, ClassNotFoundException,
      RemoteException {
    return received;
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    send(o, null, null);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object,
   *      org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType)
      throws IOException, RemoteException {
    
    response = new NettyResponse(associated, transportType, o);
  }
  
  NettyResponse getResponse() {
    Assertions.illegalState(response == null, "Response not set");
    return response;
  }
}
