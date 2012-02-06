package org.sapia.ubik.net;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.pool.Pool;


/**
 * Implements a pool of {@link Connection} instances.
 *
 * @author Yanick Duchesne
 */
public class ConnectionPool extends Pool<Connection>{
 
  /**
   * A builder that should be used to created {@link ConnectionPool} instances.
   */
  public static class Builder  {
    
    private String                 host;
    private int                    port;
    private int                    maxSize            = ConnectionPool.NO_MAX;
    private ConnectionFactory      connectionFactory;

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder port(int port) {
      this.port = port;
      return this;
    }
    
    public Builder maxSize(int maxSize) {
      this.maxSize = maxSize;
      return this;
    }
    
    public Builder connectionFactory(ConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
      return this;
    }
    
    public ConnectionPool build() {
      if(connectionFactory == null) {
        connectionFactory = new SocketConnectionFactory();
      }
      Assertions.notNull(host, "Host not set");
      return new ConnectionPool(host, port, connectionFactory, maxSize);
    }
  }
  
  // --------------------------------------------------------------------------

  protected ConnectionFactory factory;
  protected String            host;
  protected int               port;

  /**
   * @param host the host to which connections should be made. 
   * @param port the port to which to connect, at the given host.
   * @param factory the {@link ConnectionFactory} implementation to use to internally create
   * {@link Connection} instances.
   * @param maxSize the pool's maximum size (if less than or equal to 0, will be interpreted
   * has no maximum).
   */
  protected ConnectionPool(
      String host, 
      int port, 
      ConnectionFactory factory,
      int maxSize) {
    super(maxSize);
    this.factory = factory;
    this.host    = host;
    this.port    = port;
  }

  @Override
  protected final Connection doNewObject() throws Exception {
    return factory.newConnection(host, port);
  }
  
  @Override
  protected void cleanup(Connection connection) {
    try {
      connection.close();
    } catch (RuntimeException e) {
      // noop
    }
  }
  
}
