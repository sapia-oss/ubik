package org.sapia.ubik.rmi.examples.jndi;

import javax.naming.Context;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;

public class EmbeddedJndi2 {

  public static void main(String[] args) throws Exception {
    Log.setDebug();

    final EventChannel channel = new EventChannel("default");
    final EmbeddableJNDIServer server = new EmbeddableJNDIServer(channel.getReference(), 1099);
    final DiscoveryHelper discoHelper = new DiscoveryHelper(channel.getReference());
    discoHelper.addJndiDiscoListener(new JndiDiscoListener() {
      @Override
      public void onJndiDiscovered(Context ctx) {
        System.out.println("Discovered JNDI context");
      }
    });

    discoHelper.addServiceDiscoListener(new ServiceDiscoListener() {
      @Override
      public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
        System.out.println("Discovered service: " + evt);
      }
    });

    channel.start();
    server.start(true);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        server.stop();
        channel.close();
      }
    });

    Thread.sleep(5000);

    server.getLocalContext().lookup("foo");

    System.out.println("Started");

    while (true) {
      Thread.sleep(10000);
    }
  }
}
