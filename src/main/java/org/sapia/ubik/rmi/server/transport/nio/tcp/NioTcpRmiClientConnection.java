package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.util.ByteVector;
import org.sapia.ubik.util.ByteVectorOutputStream;

/**
 * A connection over a Socket the connection uses the {@link MarshalOutputStream} class to serialize outgoing objects.
 */
public class NioTcpRmiClientConnection implements RmiConnection {
  
  private Socket                sock;
  private ServerAddress         address;
  private ByteVector            vector;
  private ObjectInputStream     mis;
  private ObjectOutputStream    mos;

  /**
   * Constructor for RMIConnection.
   * 
   * @param sock
   */
  public NioTcpRmiClientConnection(Socket sock, int bufsize) throws IOException {
    this.sock = sock;
    this.address = new NioAddress(sock.getInetAddress().getHostAddress(), sock.getPort());
    vector = new ByteVector(bufsize, bufsize);
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return address;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(Object, VmId,
   *      String)
   */
  public void send(Object o, VmId vmId, String transportType)
      throws IOException, RemoteException {
    vector.clear(false);
    if(mos == null){
      mos = MarshalStreamFactory.createOutputStream(new ByteVectorOutputStream(vector));
    }
    ((RmiObjectOutput)mos).setUp(vmId, transportType);
    mos.writeObject(o);
    mos.flush();

    try {
      doSend(vector);
    } catch(java.net.SocketException e) {
      throw new RemoteException(
          "communication with server interrupted; server probably disappeared",
          e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    vector.clear(false);
    if(mos == null){
      mos = MarshalStreamFactory.createOutputStream(new ByteVectorOutputStream(vector));
    }
    mos.writeObject(o);
    mos.flush();
    
    try {
      doSend(vector);
    } catch(java.net.SocketException e) {
      throw new RemoteException(
          "communication with server interrupted; server probably disappeared",
          e);
    }
  }
  
  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive() throws IOException, ClassNotFoundException,
      RemoteException {
    try {
      if(mis == null){
        mis = MarshalStreamFactory.createInputStream(sock.getInputStream());
      }
      return mis.readObject();
    } catch(EOFException e) {
      throw new RemoteException(
          "Communication with server interrupted; server probably disappeared",
          e);
    } catch(SocketException e) {
      throw new RemoteException(
          "Connection could not be opened; server is probably down", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    try {
      sock.close();
    } catch(Throwable t) {
      //noop
    }
  }
  
  private void doSend(ByteVector vector) throws IOException{
    OutputStream sos     = sock.getOutputStream();
    DataOutputStream dos = new DataOutputStream(sos);
    dos.writeInt(vector.length());
    vector.reset();
    vector.read(dos);
    dos.flush();
  }
}
