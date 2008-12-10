package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.mina.common.IoSession;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

/**
 * @author Yanick Duchesne
 */
public class NioTcpRmiServerConnection implements RmiConnection {

  private IoSession _session;
  private ServerAddress     _addr;
  private Object _received;

  NioTcpRmiServerConnection(ServerAddress addr, IoSession session, Object received) {
    _addr = addr;
    _session = session;
    _received = received; 
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    _received = null;
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return _addr;
  }

  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive() throws IOException, ClassNotFoundException,
      RemoteException {
    return _received;
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
    
    NioResponse resp = new NioResponse();
    resp.setObject(o);
    if(associated != null && transportType != null) {
      resp.setAssociatedVmId(associated);
      resp.setTransportType(transportType);
    }    
    _session.write(resp);
  }
}
