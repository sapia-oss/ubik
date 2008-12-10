package org.sapia.ubik.rmi.server.transport.udp;

import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.udp.UDPServer;
import org.sapia.ubik.net.udp.UDPServerAddress;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.RMICommand;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

import java.io.IOException;

import java.net.DatagramSocket;
import java.net.SocketException;


/**
 * @author Yanick Duchesne
 * 17-Jun-2003
 */
public class UdpRmiServer extends UDPServer implements Server {
  /**
   * Constructor for UdpRmiServer.
   */
  public UdpRmiServer() throws SocketException {
    super(new DatagramSocket(0));
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return new UDPServerAddress(_server.getInetAddress(), _server.getPort());
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() {
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.setName("ubik.rmi.server");
    t.start();
  }

  /**
   * @see org.sapia.ubik.net.udp.UDPServer#handleRequest(Request, Object)
   */
  protected void handleRequest(Request req, Object data) {
    if (Log.isDebug()) {
      Log.debug(getClass(), "receiving command");
    }

    RMICommand cmd = (RMICommand) data;

    if (Log.isDebug()) {
      Log.debug(getClass(),
        "command received: " + cmd.getClass().getName() + " from " +
        req.getConnection().getServerAddress() + '@' + cmd.getVmId());
    }

    cmd.init(new Config(req.getServerAddress(), req.getConnection()));

    Object resp;

    try {
      resp = cmd.execute();
    } catch (Throwable t) {
      t.printStackTrace();
      resp = t;
    }

    try {
      ((RmiConnection) req.getConnection()).send(resp, cmd.getVmId(),
        cmd.getServerAddress().getTransportType());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
