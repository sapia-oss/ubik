package org.sapia.ubik.net;


/**
 * Models a request to a server. Encapsulates the connection corresponding
 * to the network link with the client making the request, as well as the
 * address of the server that received the request.
 *
 * @author Yanick Duchesne
 */
public class Request {
  
  private Connection    client;
  private ServerAddress localHost;

  /**
   * Creates an instance of this class.
   *
   * @param client the {@link Connection} which consists of the
   * network link with the client.
   *
   * @param localHost the {@link ServerAddress} of the server
   * that received the request.
   */
  public Request(Connection client, ServerAddress localHost) {
    this.client      = client;
    this.localHost   = localHost;
  }

  /**
   * Returns the address of the server that received the request.
   *
   * @return a {@link ServerAddress}.
   */
  public ServerAddress getServerAddress() {
    return localHost;
  }

  /**
   * Returns the connection which is linking to the client that made
   * the request.
   *
   * @return a {@link Connection} instance.
   */
  public Connection getConnection() {
    return client;
  }
}
