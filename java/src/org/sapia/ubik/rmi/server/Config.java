package org.sapia.ubik.rmi.server;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Config {
  private ServerAddress _addr;
  private Connection    _conn;

  public Config(ServerAddress addr, Connection conn) {
    _addr   = addr;
    _conn   = conn;
  }

  public ServerAddress getServerAddress() {
    return _addr;
  }

  public Connection getConnection() {
    return _conn;
  }
}
