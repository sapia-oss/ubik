package org.sapia.ubik.rmi.examples.time;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.log.LogFilter;
import org.sapia.ubik.rmi.server.Hub;

public class EndlessTimeClient {
  public static void main(String[] args) {
    Log.setLogFilter(new LogFilter() {
      @Override
      public boolean accepts(String source) {
        return source.startsWith("org.sapia.ubik.rmi.server.stub");
      }
    });
    Log.setTrace();

    try {
      int        i       = 0;
      TimeClient aClient = new TimeClient();

      while (true) {
        System.out.print((++i) + " - ");
        try {
          Log.debug(EndlessTimeClient.class, "performing invocation");
          aClient.execute();
        } catch (Exception e) {
          Log.error(EndlessTimeClient.class, "Caught error, will retry", e);
        }
        Thread.sleep(5000);
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
