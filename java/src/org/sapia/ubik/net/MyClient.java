package org.sapia.ubik.net;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
      SocketConnection conn = new SocketConnection(new java.net.Socket(
            "localhost", 6666));

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
