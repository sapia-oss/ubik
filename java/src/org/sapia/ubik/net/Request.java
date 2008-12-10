package org.sapia.ubik.net;


/**
 * Models a request to a server. Encapsulates the connection corresponding
 * to the network link with the client making the request, as well as the
 * address of the server that received the request.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Request {
  private Connection    _client;
  private ServerAddress _localHost;

  /**
   * Creates an instance of this class.
   *
   * @param client the <code>Connection</code> which consists of the
   * network link with the client.
   *
   * @param localHost the <code>ServerAddress</code> of the server
   * that received the request.
   */
  public Request(Connection client, ServerAddress localHost) {
    _client      = client;
    _localHost   = localHost;
  }

  /**
   * Returns the address of the server that received the request.
   *
   * @return a <code>ServerAddress</code>.
   */
  public ServerAddress getServerAddress() {
    return _localHost;
  }

  /**
   * Returns the connection which is linking to the client that made
   * the request.
   *
   * @return a <code>Connection</code> instance.
   */
  public Connection getConnection() {
    return _client;
  }
}
