package org.sapia.ubik.rmi.examples.jndi;

import org.sapia.ubik.log.IncludeClassFilter;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.StatelessFoo;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;
import org.sapia.ubik.rmi.naming.remote.JndiSyncTask;
import org.sapia.ubik.rmi.server.gc.CommandGc;
import org.sapia.ubik.rmi.server.stub.StatelessStubTable;

public class EmbeddedJndi1 {

  public static void main(String[] args) throws Exception {
    Log.setDebug();
    Log.setLogFilter(IncludeClassFilter.newInstance(EmbeddableJNDIServer.class, StatelessStubTable.class, JndiSyncTask.class, CommandGc.class));

    final EventChannel channel = new EventChannel("default");
    final EmbeddableJNDIServer server = new EmbeddableJNDIServer(channel.getReference(), 1097);
    channel.start();
    server.start(true);

    Foo service = new StatelessFoo();
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
