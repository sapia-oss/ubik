package org.sapia.ubik.net.udp;

import org.sapia.ubik.net.Request;

import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * @author Yanick Duchesne
 * 16-Jun-2003
 */
public class UDPServer implements Runnable {
  protected static final int   DEFAULT_BUFSZ   = 1000;
  protected static final int   DEFAULT_TIMEOUT = 30000;
  protected DatagramSocket     _server;
  private int                  _bufsize        = DEFAULT_BUFSZ;
  private UDPConnectionFactory _fac;

  /**
   * Constructor for UDPServer.
   */
  public UDPServer(DatagramSocket server) {
    _server   = server;
    _fac      = new UDPConnectionFactory(DEFAULT_BUFSZ, DEFAULT_TIMEOUT);
  }

  public void run() {
    while (true) {
      try {
        DatagramPacket pack = new DatagramPacket(new byte[_bufsize], _bufsize);
        UDPConnection  conn = null;
        _server.receive(pack);
        conn = _fac.newConnection(_server, pack);
        handleRequest(new Request(conn,
            new UDPServerAddress(_server.getLocalAddress(),
              _server.getLocalPort())), Util.fromDatagram(pack));
      } catch (Throwable t) {
        if (handleError(t)) {
          close();

          break;
        }
      }
    }
  }

  protected void handleRequest(Request req, Object data) {
    try {
      System.out.println("received " + data);
      req.getConnection().send("BAR");
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public void close() {
    if (_server != null) {
      _server.close();
    }
  }

  protected boolean handleError(Throwable t) {
    t.printStackTrace();

    return true;
  }

  public static void main(String[] args) {
    try {
      System.out.println("Starting server...");

      UDPServer server = new UDPServer(new DatagramSocket(6666));
      Thread    t = new Thread(server);
      t.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
