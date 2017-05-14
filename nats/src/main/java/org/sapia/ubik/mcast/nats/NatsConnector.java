package org.sapia.ubik.mcast.nats;

import java.io.IOException;

import io.nats.client.*;

/**
 * Encapsulates direct interaction with the connection to the Nats daemon.
 * 
 * @author yduchesne
 *
 */
class NatsConnector {
  
  private String url;
  private Connection connection;
  
  NatsConnector(String url) {
    this.url = url;
  }
  
  /**
   * @throws IOException create a new connection to the Nats daemon.
   */
  void connect() throws IOException {
    connection = Nats.connect(url);
  }
  
  /**
   * Disconnects from the Nats daemon.
   */
  void disconnect() {
    if (connection != null) {
      connection.close();
    }
  }
  
  /**
   * @return the connection to the Nats daemon.
   * @throws IOException
   */
  Connection getConnection() throws IOException {
    if (connection == null) {
      throw new IOException("Connection to Avis router not set");
    }
    return connection;
  }

}
