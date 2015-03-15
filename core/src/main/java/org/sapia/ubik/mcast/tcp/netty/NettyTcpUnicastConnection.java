package org.sapia.ubik.mcast.tcp.netty;

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
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.serialization.SerializationStreams;

/**
 * A client-connection used to connect to a {@link NettyTcpUnicastDispatcher}.
 */
public class NettyTcpUnicastConnection implements Connection {

  private Socket sock;
  private int bufsize;
  private ServerAddress address;
  private ChannelBuffer byteBuffer;

  public NettyTcpUnicastConnection(Socket sock, int bufsize) throws IOException {
    this.sock = sock;
    this.address = new NettyTcpUnicastAddress(sock.getInetAddress().getHostAddress(), sock.getPort());
    this.bufsize = bufsize;
    this.byteBuffer = ChannelBuffers.dynamicBuffer(bufsize);
  }

  @Override
  public ServerAddress getServerAddress() {
    return address;
  }

  @Override
  public void send(Object o) throws IOException, RemoteException {
    byteBuffer.clear();
    ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(new ChannelBufferOutputStream(byteBuffer));
    oos.writeObject(o);
    oos.flush();
    oos.close();

    try {
      doSend();
    } catch (java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    }
  }

  @Override
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
    try {
      sock.setSoTimeout(0);
      DataInputStream dis = new DataInputStream(sock.getInputStream());
      dis.readInt();
      ObjectInputStream ois = SerializationStreams.createObjectInputStream(new BufferedInputStream(sock.getInputStream(), bufsize));
      return ois.readObject();
    } catch (EOFException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    } catch (SocketException e) {
      throw new RemoteException("Connection could not be opened; server is probably down", e);
    }
  }
  
  @Override
  public Object receive(long timeout) throws IOException,
      ClassNotFoundException, RemoteException, SocketTimeoutException {
    try {
      sock.setSoTimeout((int) timeout);
      DataInputStream dis = new DataInputStream(sock.getInputStream());
      dis.readInt();
      ObjectInputStream ois = SerializationStreams.createObjectInputStream(new BufferedInputStream(sock.getInputStream(), bufsize));
      return ois.readObject();
    } catch (EOFException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    } catch (SocketException e) {
      throw new RemoteException("Connection could not be opened; server is probably down", e);
    }
  }

  @Override
  public void close() {
    try {
      sock.close();
    } catch (Exception e) {
      // noop
    }
  }

  private void doSend() throws IOException {
    OutputStream sos = new BufferedOutputStream(sock.getOutputStream(), bufsize);
    DataOutputStream dos = new DataOutputStream(sos);
    byte[] toWrite = new byte[byteBuffer.writerIndex()];
    dos.writeInt(toWrite.length);
    byteBuffer.readBytes(toWrite);
    dos.write(toWrite);
    dos.flush();
  }
}
