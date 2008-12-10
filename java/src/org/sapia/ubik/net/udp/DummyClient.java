package org.sapia.ubik.net.udp;

import org.sapia.ubik.net.Connection;

import java.net.InetAddress;


/**
 * @author Yanick Duchesne
 * 17-Jun-2003
 */
public class DummyClient {
  /**
   * Constructor for DummyClient.
   */
  public DummyClient() {
    super();
  }

  public static void main(String[] args) {
    try {
      UDPConnectionFactory _fac = new UDPConnectionFactory(1000, 2000);
      Connection           conn = _fac.newConnection(InetAddress.getLocalHost(),
          6666);
      System.out.println("Sending...");
      conn.send("FOO");
      System.out.println("Receiving...");
      System.out.println(conn.receive());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
