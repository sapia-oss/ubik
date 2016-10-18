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
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;

import org.apache.mina.core.buffer.IoBuffer;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.util.MinaByteBufferOutputStream;

/**
 * A connection over a Socket the connection uses the
 * {@link MarshalOutputStream} class to serialize outgoing objects.
 */
public class MinaRmiClientConnection implements RmiConnection {

  private static Stopwatch serializationTime = Stats.createStopwatch(MinaRmiClientConnection.class, "SerializationDuration",
      "Time required to serialize an object");

  private static Stopwatch sendTime = Stats.createStopwatch(MinaRmiClientConnection.class, "SendDuration",
      "Time required to send an object over the network");

  private Socket sock;
  private int bufsize;
  private ServerAddress address;
  private IoBuffer byteBuffer;
  private ObjectOutputStream oos;
  private ObjectInputStream ois;

  public MinaRmiClientConnection(Socket sock, int bufsize) throws IOException {
    this.sock = sock;
    this.address = new MinaAddress(sock.getInetAddress().getHostAddress(), sock.getPort());
    this.bufsize = bufsize;
    this.byteBuffer = IoBuffer.allocate(bufsize);
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
  public void send(Object o, VmId vmId, String transportType) throws IOException, RemoteException {
    byteBuffer.clear();
    if (oos == null) {
      oos = MarshalStreamFactory.createOutputStream(new MinaByteBufferOutputStream(byteBuffer));
    }
    ((RmiObjectOutput) oos).setUp(vmId, transportType);

    Split split = serializationTime.start();
    oos.writeObject(o);
    oos.flush();
    split.stop();

    try {
      doSend();
    } catch (java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    byteBuffer.clear();
    if (oos == null) {
      oos = MarshalStreamFactory.createOutputStream(new MinaByteBufferOutputStream(byteBuffer));
    }

    Split split = serializationTime.start();
    oos.writeObject(o);
    oos.flush();
    split.stop();

    try {
      doSend();
    } catch (java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
    try {
      sock.setSoTimeout(0);
      DataInputStream dis = new DataInputStream(sock.getInputStream());
      dis.readInt();
      if (ois == null) {
        ois = MarshalStreamFactory.createInputStream(new BufferedInputStream(sock.getInputStream(), bufsize));
      }
      return ois.readObject();
    } catch (EOFException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    } catch (SocketException e) {
      throw new RemoteException("Connection could not be opened; server is probably down", e);
    }
  }
  
  /**
   * @see org.sapia.ubik.net.Connection#receive(long)
   */
  public Object receive(long timeout) throws IOException,
      ClassNotFoundException, RemoteException, SocketTimeoutException {
    try {
      sock.setSoTimeout((int) timeout);
      DataInputStream dis = new DataInputStream(sock.getInputStream());
      dis.readInt();
      if (ois == null) {
        ois = MarshalStreamFactory.createInputStream(new BufferedInputStream(sock.getInputStream(), bufsize));
      }
      return ois.readObject();
    } catch (EOFException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    } catch (SocketException e) {
      throw new RemoteException("Connection could not be opened; server is probably down", e);
    }
  }
  

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    try {
      sock.close();
    } catch (Throwable t) {
      // noop
    }
    byteBuffer.free();
  }

  private void doSend() throws IOException {
    Split split = sendTime.start();
    OutputStream sos = new BufferedOutputStream(sock.getOutputStream(), bufsize);
    DataOutputStream dos = new DataOutputStream(sos);
    byte[] toWrite = new byte[byteBuffer.position()];
    dos.writeInt(toWrite.length);
    byteBuffer.flip();
    byteBuffer.get(toWrite);
    dos.write(toWrite);
    dos.flush();
    split.stop();
  }
}
