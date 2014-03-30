package org.sapia.ubik.rmi.examples.jndi;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.ReliableFoo;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;

public class EmbeddedJndi1 {

  public static void main(String[] args) throws Exception {
    Log.setInfo();

    final EventChannel channel = new EventChannel("default");
    final EmbeddableJNDIServer server = new EmbeddableJNDIServer(channel.getReference(), 1097);
    channel.start();
    server.start(true);

    Foo service = new ReliableFoo();
    server.getLocalContext().bind("/my/services/foo", service);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        server.stop();
        channel.close();
      }
    });

    System.out.println("Started");

    while (true) {
      Thread.sleep(10000);
    }
  }
}
