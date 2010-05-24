package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalInputStream;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * Implements the <code>RmiClientConnection</code> over the JDK's
 * <code>URL</code> class.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JdkRmiClientConnection implements RmiConnection {
  private static final int    DEFAULT_BUFSZ         = 1024;
  private static final String POST_METHOD           = "POST";
  private static final String CONTENT_LENGTH_HEADER = "Content-Length";
  private HttpAddress         _address;
  private URL                 _url;
  private boolean             _closed;
  private HttpURLConnection   _conn;
  private int                 _bufsz = DEFAULT_BUFSZ;

  public JdkRmiClientConnection() {
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object, org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType)
    throws IOException, RemoteException {
    _conn = (HttpURLConnection) _url.openConnection();
    _conn.setDoInput(true);
    _conn.setDoOutput(true);
    _conn.setUseCaches(false);
    _conn.setRequestMethod(POST_METHOD);

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

    _conn.setRequestProperty(CONTENT_LENGTH_HEADER, "" + data.length);

    OutputStream os = _conn.getOutputStream();
    os.write(data);
    os.flush();
    os.close();
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    if ((_conn != null) && !_closed) {
      _conn.disconnect();
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
    if (_conn == null) {
      throw new IllegalStateException("Cannot receive; data was not posted");
    }

    MarshalInputStream is = new MarshalInputStream(_conn.getInputStream());

    try {
      return is.readObject();
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

  JdkRmiClientConnection setUp(HttpAddress addr) throws RemoteException {
    _closed = false;

    if (_url == null) {
      try {
        _url = new URL(addr.toString());
      } catch (MalformedURLException e) {
        throw new RemoteException(addr.toString(), e);
      }
    }

    _address = addr;

    return this;
  }
}
