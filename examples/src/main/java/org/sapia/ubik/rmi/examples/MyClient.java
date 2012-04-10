package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.net.SocketConnection;


/**
 * @author Yanick Duchesne
 */
public class MyClient {
  /**
   * Constructor for MyClient.
   */
  public MyClient() {
    super();
  }

  public static void main(String[] args) {
    try {
      SocketConnection conn = new SocketConnection("test", new java.net.Socket("localhost", 6666), 512);

      while (true) {
        System.out.println("Sending...");

        conn.send("This is foo!!!");

        System.out.println("Receiving...");

        System.out.println(conn.receive());

        Thread.sleep(3000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
