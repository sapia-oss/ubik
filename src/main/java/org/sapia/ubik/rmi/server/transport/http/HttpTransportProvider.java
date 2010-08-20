package org.sapia.ubik.rmi.server.transport.http;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;


/**
 * An instance of this class creates <code>HttpRmiServer</code> instances,
 * as well as client-side connections (using Jakartas HTTP client). It is the
 * entry-point into Ubik's HTTP tranport layer.
 * <p>
 * For the properties that an instance of this class takes (and their default values),
 * see the <code>HttpConsts</code> interface.
 *
 * @see org.sapia.ubik.rmi.server.transport.http.HttpConsts
 * @see org.sapia.ubik.rmi.server.transport.http.HttpRmiServer
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpTransportProvider implements TransportProvider, HttpConsts {
  private static boolean _usesJakarta;

  static {
    try {
      Class.forName("org.apache.commons.httpclient.HttpClient");
      _usesJakarta = true;
    } catch (Exception e) {
    }
  }

  private String        _transportType;
  private ServiceMapper _services = new ServiceMapper();
  private Map           _pools = Collections.synchronizedMap(new HashMap());

  public HttpTransportProvider() {
    this(HttpConsts.DEFAULT_HTTP_TRANSPORT_TYPE,
      new File(System.getProperty("user.dir")));
  }

  /**
   * @param transportType a "transport type" identifier.
   */
  public HttpTransportProvider(String transportType) {
    this(transportType, new File(System.getProperty("user.dir")));
  }

  /**
   * @param transportType a "transport type" identifier.
   */
  public HttpTransportProvider(String transportType, File baseDir) {
    _transportType = transportType;
    _services.setBaseDir(baseDir);
  }
  
  /**
   * @return the <code>ServiceMapper</code> that holds this instance`s request handlers ("services", in
   * the Simple API's terms).
   */
  public ServiceMapper getServices() {
    return _services;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(org.sapia.ubik.net.ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address)
    throws RemoteException {
    Connections conns;

    if ((conns = (Connections) _pools.get(address)) == null) {
      try {
        if (_usesJakarta) {
          conns = new HttpClientConnectionPool((HttpAddress) address);
        } else {
          conns = new JdkClientConnectionPool((HttpAddress) address);
        }

        _pools.put(address, conns);
      } catch (UriSyntaxException e) {
        throw new RemoteException("Could not process given address", e);
      }
    }

    return conns;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   */
  public String getTransportType() {
    return _transportType;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newDefaultServer()
   */
  public Server newDefaultServer() throws RemoteException {
    throw new UnsupportedOperationException(
      "Transport provider does not support anonymous servers/dynamic ports");
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(java.util.Properties)
   */
  public Server newServer(Properties props) throws RemoteException {
    Uri    serverUrl;
    String portStr     = props.getProperty(HTTP_PORT_KEY);
    String contextPath;
    int    port        = DEFAULT_HTTP_PORT;

    if (portStr == null) {
      Log.warning(getClass(),
        "Property '" + HTTP_PORT_KEY + "' not specified; using default: " +
        port);
    } else {
      port = Integer.parseInt(portStr);
    }

    if (props.getProperty(SERVER_URL_KEY) != null) {
      try {
        serverUrl = Uri.parse(props.getProperty(SERVER_URL_KEY));

        if (serverUrl.getPort() == Uri.UNDEFINED_PORT) {
          serverUrl.setPort(DEFAULT_HTTP_PORT);
        }

        contextPath = serverUrl.getPath();
      } catch (UriSyntaxException e) {
        throw new RemoteException("Could not parse server URL", e);
      }
    } else {
      contextPath = props.getProperty(PATH_KEY, DEFAULT_CONTEXT_PATH);

      try {
        serverUrl = Uri.parse("http://" +
            InetAddress.getLocalHost().getHostAddress() + ":" + port + "/" +
            contextPath);
      } catch (UriSyntaxException e) {
        throw new RemoteException("Could not parse server URL", e);
      } catch (UnknownHostException e) {
        throw new RemoteException("Could not acquire local address", e);
      }
    }

    String maxThreads = props.getProperty(Consts.SERVER_MAX_THREADS);
    int    max = 0;

    if (maxThreads != null) {
      try {
        max = Integer.parseInt(maxThreads);
      } catch (NumberFormatException e) {
        throw new RemoteException("Invalid value for '" +
          Consts.SERVER_MAX_THREADS + "' : " + maxThreads);
      }

      if (max < 0) {
        max = 0;
      }
    }

    HttpRmiServer svr = new HttpRmiServer(_services, _transportType, serverUrl,
        contextPath, port);
    svr.setMaxThreads(max);

    return svr;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public void shutdown() {
  }

  /**
   * Returns the service mapper that is held within this instance. Other
   * services can be added, associated to different context paths. This allows
   * other types of requests (non-Ubik ones) to be processed by the same HTTP
   * server.
   *
   * @return the <code>ServiceMapper</code> that this instance holds.
   */
  public ServiceMapper getServiceMapper() {
    return _services;
  }
}
