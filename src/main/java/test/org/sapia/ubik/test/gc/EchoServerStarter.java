package org.sapia.ubik.test.gc;

import org.sapia.ubik.rmi.server.Hub;

public class EchoServerStarter {
  public static void main(String[] args) {
    System.setProperty("ubik.rmi.log.level", "info");
//    System.setProperty("ubik.rmi.address-pattern", "127.0.0.1");
    System.setProperty("ubik.rmi.server.gc.interval", "20000");
    System.setProperty("ubik.rmi.server.gc.timeout", "41000");

    GarbageGenerator.startDaemon();

    try {
      EchoServiceImpl service = new EchoServiceImpl();
      Hub.exportObject(service, 9800);
      System.out.println("EchoServerStarter ===> Exported echo server at address " + Hub.serverRuntime.server.getServerAddress());

      while (true) {
        Thread.sleep(Long.MAX_VALUE);
      }
    } catch (Exception e) {
      System.err.println("EchoServerStarter ===> error running echo server: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
