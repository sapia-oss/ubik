package org.sapia.ubik.rmi.examples.jndi;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.ReliableFoo;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;

public class EmbeddedJndi1 {

  public static void main(String[] args) throws Exception {
    Log.setDebug();
    
    final EventChannel channel = new EventChannel("default");
    final EmbeddableJNDIServer server = new EmbeddableJNDIServer(channel.getReference(), 1098);
    channel.start();
    server.start(true);
    
    Foo service = new ReliableFoo();
    server.getLocalContext().bind("foo", service);
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        server.stop();
        channel.close();
      }
    });
    while (true) {
      Thread.sleep(10000);
    }
  }
}
