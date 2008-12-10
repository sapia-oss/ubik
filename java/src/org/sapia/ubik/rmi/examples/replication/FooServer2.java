package org.sapia.ubik.rmi.examples.replication;

import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.UbikFoo;
import org.sapia.ubik.rmi.replication.ReplicationEvent;
import org.sapia.ubik.rmi.server.Hub;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class FooServer2 {
  public FooServer2() {
  }

  public static void main(String[] args) {
    TCPAddress addr    = new TCPAddress("localhost", 9001);
    TCPAddress sibling = new TCPAddress("localhost", 9000);

    Set        siblings = new HashSet();
    siblings.add(sibling);

    try {
      Foo                   f = new UbikFoo();

      ServerSideInterceptor serverSide = new ServerSideInterceptor(siblings, f);
      Hub.serverRuntime.addInterceptor(ReplicationEvent.class, serverSide);

      Hub.exportObject(f, addr.getPort());

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
