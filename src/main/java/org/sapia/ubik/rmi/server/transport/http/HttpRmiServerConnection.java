package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalInputStream;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;


/**
 * Implements the <code>RmiConnection</code> interface over HTTP - more precisely,
 * over <code>Request</code> and <code>Response</code> instances - from the Simple
 * API - see the <a href="http://simpleweb.sf.net/">Simple website</a> for more info.
 * </p>
 * An instance of this class is used on the server side.
 *
 * @see HttpRmiClientConnection
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class HttpRmiServerConnection implements RmiConnection {
  static final int    DEFAULT_BUFSZ = 1024;
  private int         _bufsz   = DEFAULT_BUFSZ;
  private Request     _req;
  private Response    _res;
  private HttpAddress _address;

  /**
   * Creates an instance of this class with the given request and
   * response objects, as well as http server address.
   */
  HttpRmiServerConnection(HttpAddress address, Request req, Response res) {
    _req       = req;
    _res       = res;
    _address   = address;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object, org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType)
    throws IOException, RemoteException {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(_bufsz);
      MarshalOutputStream   mos = new MarshalOutputStream(bos);

      if ((associated != null) && (transportType != null)) {
        mos.setUp(associated, transportType);
      }

      mos.writeObject(o);
      mos.flush();
      mos.close();

      byte[] data = bos.toByteArray();

      if (data.length > _bufsz) {
        _bufsz = data.length;
      }

      _res.setContentLength(data.length);

      OutputStream os = _res.getOutputStream(data.length);
      os.write(data);
      os.flush();
      os.close();
    } catch (java.net.SocketException e) {
      throw new RemoteException("communication with server interrupted; server probably disappeared", e);
    } catch (Exception e){
      throw new RemoteException("communication with server interrupted; server probably disappeared", e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    try {
      _res.commit();
    } catch (Exception e) {
      //noop
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return _address;
  }

  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive()
    throws IOException, ClassNotFoundException, RemoteException {
    try{
      MarshalInputStream is = new MarshalInputStream(_req.getInputStream());
      return is.readObject();
    }catch(SocketException e){
      throw new RemoteException("communication with server interrupted; server probably disappeared",
          e);
    }catch(Exception e){
      throw new IOException(e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    try {
      MarshalOutputStream os = new MarshalOutputStream(_res.getOutputStream());
      os.writeObject(o);
      os.flush();
    } catch (java.net.SocketException e) {
      throw new RemoteException("communication with server interrupted; server probably disappeared",
        e);
    }catch(Exception e){
      throw new IOException(e);
    }

  }
}
