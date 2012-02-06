package org.sapia.ubik.rmi.server.transport.http.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;


/**
 * Implements the {@link RmiConnection} interface over HTTP - more precisely,
 * over {@link HttpServletRequest} and {@link HttpServletResponse} instances - from the Servlet
 * API.
 * </p>
 * An instance of this class is used on the servlet-side only.
 *
 * @author Yanick Duchesne
 */
public class ServletRmiConnection implements RmiConnection {
  
  static final int            DEFAULT_BUFSZ = 1024;
  
  private volatile int        bufsz   = DEFAULT_BUFSZ;
  private HttpServletRequest  req;
  private HttpServletResponse res;
  private ServletAddress      address;

  /**
   * Creates an instance of this class with the given request and
   * response objects, as well as http server address.
   */
  ServletRmiConnection(ServletAddress address, HttpServletRequest req, HttpServletResponse res) {
    this.req       = req;
    this.res       = res;
    this.address   = address;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object, org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType)
    throws IOException, RemoteException {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsz);
      ObjectOutputStream    mos = MarshalStreamFactory.createOutputStream(bos);

      if ((associated != null) && (transportType != null)) {
        ((RmiObjectOutput)mos).setUp(associated, transportType);
      }

      mos.writeObject(o);
      mos.flush();
      mos.close();

      byte[] data = bos.toByteArray();

      if (data.length > bufsz) {
        bufsz = data.length;
      }

      res.setContentLength(data.length);

      OutputStream os = res.getOutputStream();
      os.write(data);
      os.flush();
      os.close();
    } catch (java.net.SocketException e) {
      throw new RemoteException("communication with server interrupted; server probably disappeared", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    try {
      res.getOutputStream().close();
    } catch (Exception e) {
      // noop
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return address;
  }

  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive()
    throws IOException, ClassNotFoundException, RemoteException {
    ObjectInputStream is = MarshalStreamFactory.createInputStream(req.getInputStream());

    return is.readObject();
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    try {
      ObjectOutputStream os = MarshalStreamFactory.createOutputStream(res.getOutputStream());
      os.writeObject(o);
      os.flush();
    } catch (java.net.SocketException e) {
      throw new RemoteException("communication with server interrupted; server probably disappeared", e);
    }
  }
}
