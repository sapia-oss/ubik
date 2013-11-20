package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import org.sapia.ubik.net.SocketConnection;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;

/**
 * A connection over a {@link Socket} - the connection uses the
 * {@link MarshalOutputStream} class to serialize outgoing objects.
 * 
 * @author Yanick Duchesne
 */
public class SocketRmiConnection extends SocketConnection implements RmiConnection {

  public SocketRmiConnection(String transportType, Socket sock, ClassLoader loader, int bufsize) {
    super(transportType, sock, loader, bufsize);
  }

  public SocketRmiConnection(String transportType, Socket sock, int bufsize) {
    super(transportType, sock, bufsize);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(Object, VmId,
   *      String)
   */
  public void send(Object o, VmId vmId, String tranportType) throws IOException, RemoteException {
    try {
      writeHeader(sock.getOutputStream(), loader);
      ((RmiObjectOutput) os).setUp(vmId, tranportType);
      super.doSend(o, os);
    } catch (java.net.SocketException e) {
      throw new RemoteException("communication with server interrupted; server probably disappeared", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.SocketConnection#newOutputStream(OutputStream,
   *      ClassLoader)
   */
  protected ObjectOutputStream newOutputStream(OutputStream os, ClassLoader loader) throws IOException {
    return MarshalStreamFactory.createOutputStream(os);
  }

  /**
   * @see org.sapia.ubik.net.SocketConnection#newInputStream(InputStream,
   *      ClassLoader)
   */
  protected ObjectInputStream newInputStream(InputStream is, ClassLoader loader) throws IOException {
    return MarshalStreamFactory.createInputStream(is);
  }
}
