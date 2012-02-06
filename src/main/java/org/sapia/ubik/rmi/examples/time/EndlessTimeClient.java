package org.sapia.ubik.rmi.examples.time;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.Hub;


/**
 * @
 */
public class EndlessTimeClient {
  public static void main(String[] args) {
    Log.setInfo();

    try {
      int        i       = 0;
      TimeClient aClient = new TimeClient();

      while (true) {
        System.out.print((++i) + " - ");
        aClient.execute();
        Thread.sleep(15000);
      }
    } catch (InterruptedException ie) {
      System.err.println("The endless time client is interrupted, exiting...");
      ie.printStackTrace();
    } catch (RuntimeException re) {
      System.err.println("System error running the time client, exiting...");
      re.printStackTrace();
    } finally {
      try {
        Hub.shutdown(30000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
