package org.sapia.ubik.rmi.server.transport.mina;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.apache.mina.common.ByteBuffer;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.util.MinaByteBufferOutputStream;

/**
 * A connection over a Socket the connection uses the {@link MarshalOutputStream} class to serialize outgoing objects.
 */
public class NioTcpRmiClientConnection implements RmiConnection {
	
  private Socket         		 sock;
  private int            		 bufsize;
  private ServerAddress  		 address;
  private ByteBuffer		 		 byteBuffer;
  private ObjectOutputStream oos;
  private ObjectInputStream  ois;
  
  public NioTcpRmiClientConnection(Socket sock, int bufsize) throws IOException {
    this.sock 			= sock;
    this.address 		= new NioAddress(sock.getInetAddress().getHostAddress(), sock.getPort());
    this.bufsize 		= bufsize;
    this.byteBuffer = ByteBuffer.allocate(bufsize);
    byteBuffer.setAutoExpand(true);
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
    byteBuffer.clear();
    if(oos == null) {
    	oos = MarshalStreamFactory.createOutputStream(new MinaByteBufferOutputStream(byteBuffer));
    }
    ((RmiObjectOutput)oos).setUp(vmId, transportType);
    oos.writeObject(o);
    oos.flush();

    try {
      doSend();
    } catch(java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    byteBuffer.clear();
    if(oos == null) {
    	oos = MarshalStreamFactory.createOutputStream(new MinaByteBufferOutputStream(byteBuffer));
    }
    oos.writeObject(o);
    oos.flush();
    
    try {
      doSend();
    } catch(java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    }
  }
  
  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive() throws IOException, ClassNotFoundException,
      RemoteException {
    try {
    	DataInputStream dis = new DataInputStream(sock.getInputStream());
    	dis.readInt();
    	if(ois == null) {
    		ois = MarshalStreamFactory.createInputStream(new BufferedInputStream(sock.getInputStream(), bufsize));
    	}
      return ois.readObject();
    } catch(EOFException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    } catch(SocketException e) {
      throw new RemoteException("Connection could not be opened; server is probably down", e);
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
    byteBuffer.release();
  }
  
  private void doSend() throws IOException{
    OutputStream sos     = new BufferedOutputStream(sock.getOutputStream(), bufsize);
    DataOutputStream dos = new DataOutputStream(sos);
    byte[] toWrite 			 = new byte[byteBuffer.position()];
    dos.writeInt(toWrite.length);
    byteBuffer.flip();
    byteBuffer.get(toWrite);
    dos.write(toWrite);
    dos.flush();
  }
}
