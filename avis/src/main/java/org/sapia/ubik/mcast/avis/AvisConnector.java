package org.sapia.ubik.mcast.avis;

import java.io.IOException;

import org.sapia.ubik.mcast.avis.client.Elvin;

/**
 * Encapsulates direct interaction with the connection to the Avis router.
 * 
 * @author yduchesne
 *
 */
class AvisConnector {
  
  private String url;
  private Elvin  connection;
  
  AvisConnector(String url) {
    this.url = url;
  }
  
  /**
   * @throws IOException create a new Avis connection.
   */
  void connect() throws IOException {
    connection = new Elvin(url);
  }
  
  /**
   * Disconnects from the Avis router.
   */
  void disconnect() {
    if (connection != null) {
      connection.close();
    }
  }
  
  /**
   * @return the connection to the Avis router.
   * @throws IOException
   */
  Elvin getConnection() throws IOException {
    if (connection == null) {
      throw new IOException("Connection to Avis router not set");
    }
    return connection;
  }

}
