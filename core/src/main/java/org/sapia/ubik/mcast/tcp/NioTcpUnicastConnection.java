package org.sapia.ubik.mcast.tcp;

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
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.serialization.SerializationStreams;
import org.sapia.ubik.util.ByteBufferOutputStream;

/**
 * A client-connection used to connect to a {@link NioTcpUnicastDispatcher}.
 */
public class NioTcpUnicastConnection implements Connection {
	
  private Socket         		 sock;
  private int            		 bufsize;
  private ServerAddress  		 address;
  private ByteBuffer		 		 byteBuffer;
  
  public NioTcpUnicastConnection(Socket sock, int bufsize) throws IOException {
    this.sock 			= sock;
    this.address 		= new NioTcpUnicastDispatcher.NioTcpUnicastAddress(sock.getInetAddress().getHostAddress(), sock.getPort());
    this.bufsize 		= bufsize;
    this.byteBuffer = ByteBuffer.allocate(bufsize);
    byteBuffer.setAutoExpand(true);
  }

  @Override
  public ServerAddress getServerAddress() {
    return address;
  }

  @Override
  public void send(Object o) throws IOException, RemoteException {
    byteBuffer.clear();
    ObjectOutputStream	oos = SerializationStreams.createObjectOutputStream(new ByteBufferOutputStream(byteBuffer));
    oos.writeObject(o);
    oos.flush();
    oos.close();
    
    try {
      doSend();
    } catch(java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    }
  }
  
  @Override
  public Object receive() throws IOException, ClassNotFoundException,
      RemoteException {
    try {
    	DataInputStream dis = new DataInputStream(sock.getInputStream());
    	dis.readInt();
    	ObjectInputStream ois = SerializationStreams.createObjectInputStream(new BufferedInputStream(sock.getInputStream(), bufsize));
      return ois.readObject();
    } catch(EOFException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    } catch(SocketException e) {
      throw new RemoteException("Connection could not be opened; server is probably down", e);
    }
  }

  @Override
  public void close() {
    try {
      sock.close();
    } catch(Exception e) {
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
