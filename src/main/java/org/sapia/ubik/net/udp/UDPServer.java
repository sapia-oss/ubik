package org.sapia.ubik.net.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Request;


/**
 * Implements a basic {@link UDPServer}. Inheriting classes must override
 * {@link #handleRequest(Request, Object)}, which is called by an instance
 * of this class when receiving a datagram packet.
 * 
 * @author Yanick Duchesne
 */
public abstract class UDPServer implements Runnable {
  
  protected static final int   DEFAULT_BUFSZ   = 1000;
  protected static final int   DEFAULT_TIMEOUT = 30000;
  
  protected DatagramSocket     server;
  private int                  bufsize;
  private UDPConnectionFactory fac;

  /**
   * @param server the {@link DatagramSocket} that this instance should wrap.
   */
  protected UDPServer(DatagramSocket server) {
    this(server, DEFAULT_BUFSZ);
  }
  
  /**
   * @param server the {@link DatagramSocket} that this instance should wrap.
   * @param bufsize the internal buffer size to use when reading data packets.
   */
  protected UDPServer(DatagramSocket server, int bufsize) {
    this(server, bufsize, DEFAULT_TIMEOUT);
  }
  
  /**
   * @param server  the {@link DatagramSocket} that this instance should wrap.
   * @param bufsize the internal buffer size to use when reading data packets.
   * @param timeout the connection timeout.
   */
  protected UDPServer(DatagramSocket server, int bufsize, int timeout) {
    this.server  = server;
    fac          = new UDPConnectionFactory(bufsize, timeout);
    this.bufsize = bufsize;
  }

  public void run() {
    while (true) {
      try {
        DatagramPacket pack = new DatagramPacket(new byte[bufsize], bufsize);
        UDPConnection  conn = null;
        server.receive(pack);
        conn = fac.newConnection(server, pack);
          handleRequest(
              new Request(
                  conn,
                  new UDPServerAddress(server.getLocalAddress(), 
                  server.getLocalPort())
          ), 
          Util.fromDatagram(pack)
        );
      } catch (Throwable t) {
        if (handleError(t)) {
          close();

          break;
        }
      }
    }
  }

  protected abstract void handleRequest(Request req, Object data);

  public void close() {
    if (server != null) {
      server.close();
    }
  }

  protected boolean handleError(Throwable t) {
    Log.error(UDPServer.class, t);
    return true;
  }
}
