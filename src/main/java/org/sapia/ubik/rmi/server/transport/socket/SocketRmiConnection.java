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
import org.sapia.ubik.rmi.server.transport.MarshalInputStream;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * A connection over a <code>Socket</code> - the connection uses the
 * <code>MarshalOutputStream</code> class to serialize outgoing objects.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketRmiConnection extends SocketConnection
  implements RmiConnection {
  /**
   * Constructor for RMIConnection.
   * @param sock
   * @param loader
   */
  public SocketRmiConnection(Socket sock, ClassLoader loader) {
    super(sock, loader);
  }

  /**
   * Constructor for RMIConnection.
   * @param sock
   */
  public SocketRmiConnection(Socket sock) {
    super(sock);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(Object, VmId, String)
   */
  public void send(Object o, VmId vmId, String tranportType)
    throws IOException, RemoteException {
    try {
      writeHeader(_sock.getOutputStream(), _loader);
      ((MarshalOutputStream) _os).setUp(vmId, tranportType);
      super.doSend(o, _os);
    } catch (java.net.SocketException e) {
      throw new RemoteException("communication with server interrupted; server probably disappeared",
        e);
    }
  }

  /**
   * @see org.sapia.ubik.net.SocketConnection#newOutputStream(OutputStream, ClassLoader)
   */
  protected ObjectOutputStream newOutputStream(OutputStream os,
    ClassLoader loader) throws IOException {
    return new MarshalOutputStream(os);
  }

  /**
   * @see org.sapia.ubik.net.SocketConnection#newInputStream(InputStream, ClassLoader)
   */
  protected ObjectInputStream newInputStream(InputStream is, ClassLoader loader)
    throws IOException {
    return new MarshalInputStream(is);
  }
}
