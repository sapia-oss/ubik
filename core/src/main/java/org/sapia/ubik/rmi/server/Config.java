package org.sapia.ubik.rmi.server;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;


/**
 * A command's configuration.
 * 
 * @author Yanick Duchesne
 */
public class Config {
  private ServerAddress addr;
  private Connection    conn;

  public Config(ServerAddress addr, Connection conn) {
    this.addr = addr;
    this.conn = conn;
  }

  public ServerAddress getServerAddress() {
    return addr;
  }

  public Connection getConnection() {
    return conn;
  }
}
