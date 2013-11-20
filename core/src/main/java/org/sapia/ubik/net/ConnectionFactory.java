package org.sapia.ubik.net;

import java.io.IOException;

/**
 * This interface specifies the behavior of a factory of {@link Connection}
 * instances.
 * 
 * @author Yanick Duchesne
 */
public interface ConnectionFactory {
  /**
   * Returns a client {@link Connection} instance that connects to the server at
   * the given host and port.
   * 
   * @param host
   *          the host to connect to.
   * @param port
   *          the port on which the server is listening at the given host.
   * @throws IOException
   *           if the connection instance could not be created.
   */
  public Connection newConnection(String host, int port) throws IOException;

  /**
   * @return the transport type corresponding to the connections that this
   *         instance creates.
   */
  public String getTransportType();
}
