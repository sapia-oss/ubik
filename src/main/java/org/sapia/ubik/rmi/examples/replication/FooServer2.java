package org.sapia.ubik.rmi.examples.replication;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.UbikFoo;
import org.sapia.ubik.rmi.replication.ReplicationEvent;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;


/**
 * @author Yanick Duchesne
 */
public class FooServer2 {
  public FooServer2() {
  }

  public static void main(String[] args) {
    TCPAddress addr    = new TcpSocketAddress("localhost", 9001);
    TCPAddress sibling = new TcpSocketAddress("localhost", 9000);

    Set        siblings = new HashSet();
    siblings.add(sibling);

    try {
      Foo                   f = new UbikFoo();

      ServerSideInterceptor serverSide = new ServerSideInterceptor(siblings, f);
      Hub.getModules().getServerRuntime().getDispatcher().addInterceptor(ReplicationEvent.class, serverSide);

      Hub.exportObject(f, addr.getPort());

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
