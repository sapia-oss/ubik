package org.sapia.ubik.rmi.examples.replication;

import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Client {
  public static void main(String[] args) {
    try {
      Hub.clientRuntime.addInterceptor(ClientPreInvokeEvent.class,
        new ClientSideInterceptor(new ReplicatedInvokerImpl()));

      Foo f = (Foo) Hub.connect("localhost", 9000);
      f.getBar();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
