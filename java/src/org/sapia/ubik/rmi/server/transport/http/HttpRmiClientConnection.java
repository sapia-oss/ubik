package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalInputStream;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * Implements the <code>RmiConnection</code> interface over HTTP - more precisely,
 * over a Jakarta HTTP client. Data is sent using the POST method.
 * </p>
 * An instance of this class is used on the client side.
 *
 * @see org.sapia.ubik.rmi.server.transport.http.HttpRmiServerConnection
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpRmiClientConnection implements RmiConnection {
  private static final int DEFAULT_BUFSZ = 1024;
  private HttpAddress      _address;
  private HttpClient       _client;
  private PostMethod       _post;
  private boolean          _closed;
  private int              _bufsz = DEFAULT_BUFSZ;

  /**
   * Creates an instance of this class with the given HTTP client and
   * uri to connect to.
   */
  public HttpRmiClientConnection() {
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object, org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType)
    throws IOException, RemoteException {
    _post = new PostMethod(_address.toString());

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

    _post.setRequestContentLength(data.length);
    _post.setRequestBody(new ByteArrayInputStream(data));
    _client.executeMethod(_post);
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    if ((_post != null) && !_closed) {
      _post.releaseConnection();
      _closed = true;
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
    if (_post == null) {
      throw new IllegalStateException("Cannot receive; data was not posted");
    }

    MarshalInputStream is = new MarshalInputStream(_post.getResponseBodyAsStream());

    try {
      return is.readObject();
    } catch (IOException ioe) {
      System.err.println(((ioe.getMessage() + "\nHTTP RESPONSE:\n" + _post) == null)
        ? "null" : _post.getResponseBodyAsString());
      throw ioe;
    } finally {
      is.close();
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    send(o, null, null);
  }

  HttpRmiClientConnection setUp(HttpClient client, HttpAddress addr) {
    _closed    = false;
    _client    = client;
    _address   = addr;

    return this;
  }
}
