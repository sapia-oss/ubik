package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.util.Conf;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * Implements the {@link RmiConnection} interface over HTTP - more precisely,
 * over {@link Request} and {@link Response} instances - from the Simple API -
 * see the <a href="http://www.simpleframework.org/">Simple website</a> for more
 * info. </p> An instance of this class is used on the server side.
 * 
 * @see HttpRmiClientConnection
 * 
 * @author Yanick Duchesne
 */
class HttpRmiServerConnection implements RmiConnection {

  private int bufsz = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);
  private Request req;
  private Response res;
  private HttpAddress address;

  /**
   * Creates an instance of this class with the given request and response
   * objects, as well as http server address.
   */
  HttpRmiServerConnection(HttpAddress address, Request req, Response res) {
    this.req = req;
    this.res = res;
    this.address = address;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object,
   *      org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType) throws IOException, RemoteException {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsz);
      ObjectOutputStream mos = MarshalStreamFactory.createOutputStream(bos);

      if ((associated != null) && (transportType != null)) {
        ((RmiObjectOutput) mos).setUp(associated, transportType);
      }

      mos.writeObject(o);
      mos.flush();
      mos.close();

      byte[] data = bos.toByteArray();

      if (data.length > bufsz) {
        bufsz = data.length;
      }

      res.setContentLength(data.length);

      OutputStream os = res.getOutputStream(data.length);
      os.write(data);
      os.flush();
      os.close();
    } catch (java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared", e);
    } catch (Exception e) {
      throw new RemoteException("System exception occurred; server may have disappeared", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
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
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
    try {
      ObjectInputStream is = MarshalStreamFactory.createInputStream(req.getInputStream());
      return is.readObject();
    } catch (SocketException e) {
      throw new RemoteException("Error reading request payload", e);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    try {
      ObjectOutputStream os = MarshalStreamFactory.createOutputStream(res.getOutputStream());
      os.writeObject(o);
      os.flush();
      os.close();
    } catch (java.net.SocketException e) {
      throw new RemoteException("Error writing response payload", e);
    } catch (Exception e) {
      throw new IOException(e);
    }

  }
}
