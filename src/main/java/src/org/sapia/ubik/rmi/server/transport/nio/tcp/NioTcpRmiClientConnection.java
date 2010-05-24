package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalInputStream;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.ByteVector;
import org.sapia.ubik.util.ByteVectorOutputStream;

/**
 * A connection over a <code>Socket</code>- the connection uses the
 * <code>MarshalOutputStream</code> class to serialize outgoing objects.
 * 
 * @author Yanick Duchesne
 *         <dl>
 *         <dt><b>Copyright: </b>
 *         <dd>Copyright &#169; 2002-2003 <a
 *         href="http://www.sapia-oss.org">Sapia Open Source Software </a>. All
 *         Rights Reserved.</dd>
 *         </dt>
 *         <dt><b>License: </b>
 *         <dd>Read the license.txt file of the jar or visit the <a
 *         href="http://www.sapia-oss.org/license.html">license page </a> at the
 *         Sapia OSS web site</dd>
 *         </dt>
 *         </dl>
 */
public class NioTcpRmiClientConnection implements RmiConnection {
  
  static final int MAX_INCREMENT = 8000;
  
  private Socket                _sock;
  private ServerAddress         _address;
  private ByteVector            _vector;
  private MarshalInputStream    _mis;
  private MarshalOutputStream   _mos;

  /**
   * Constructor for RMIConnection.
   * 
   * @param sock
   */
  public NioTcpRmiClientConnection(Socket sock, int bufsize) throws IOException {
    _sock = sock;
    _address = new NioAddress(sock.getInetAddress().getHostAddress(), sock
        .getPort());
    
    _vector = new ByteVector(bufsize, bufsize > MAX_INCREMENT ? MAX_INCREMENT : bufsize);
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return _address;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(Object, VmId,
   *      String)
   */
  public void send(Object o, VmId vmId, String transportType)
      throws IOException, RemoteException {
    _vector.clear(false);
    if(_mos == null){
      _mos = new MarshalOutputStream(new ByteVectorOutputStream(_vector));
    }
    _mos.setUp(vmId, transportType);
    _mos.writeObject(o);
    _mos.flush();

    try {
      doSend(_vector);
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
    _vector.clear(false);
    if(_mos == null){
      _mos = new MarshalOutputStream(new ByteVectorOutputStream(_vector));
    }
    _mos.writeObject(o);
    _mos.flush();
    
    try {
      doSend(_vector);
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
      if(_mis == null){
      //DataInputStream dis = new DataInputStream(_sock.getInputStream());
      //dis.readI
        _mis = new MarshalInputStream(_sock.getInputStream());
      }
      return _mis.readObject();
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
      _sock.close();
    } catch(Throwable t) {
      //noop
    }
  }
  
  private void doSend(ByteVector vector) throws IOException{
    OutputStream sos = _sock.getOutputStream();
    DataOutputStream dos = new DataOutputStream(sos);
    dos.writeInt(vector.length());
    vector.reset();
    vector.read(dos);
    dos.flush();
  }
}
