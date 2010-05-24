package org.sapia.ubik.rmi.examples.time;

import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;


/**
 * @
 */
public class StressedTimeClient {
  public static int COUNT = 100000;

  public static void main(String[] args) {
    Log.setInfo();

    try {
      int        i       = 0;
      TimeClient aClient = new TimeClient(false);
      long       aStart  = System.currentTimeMillis();

      System.out.println(
        "\n=============================================\n          TEST EXECUTION STATISTICS\n---------------------------------------------");
      System.out.println("Starting test of " + COUNT + " iterations...");

      while (i++ < COUNT) {
        aClient.execute();
      }

      long delta = System.currentTimeMillis() - aStart;

      //      System.out.println("\n=============================================\n          TEST EXECUTION STATISTICS\n---------------------------------------------");
      System.out.println("Test Completed\n");
      System.out.println("Executed " + i + " calls in " + delta + " ms.");
      System.out.println("Average of " + (i / (delta / 1000.0)) +
        " tx per second");
      System.out.println("Average of " + ((delta) / (float) i) +
        " millisecond per tx");
      System.out.println("=============================================\n");
    } catch (RuntimeException re) {
      System.err.println(
        "System error running the stressed time client, exiting...");
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
