package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.transport.nio.tcp.NioAddress;
import org.sapia.ubik.rmi.server.transport.nio.tcp.NioTcpTransportProvider;

import java.rmi.RemoteException;

import java.util.Properties;

import javax.naming.*;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class NioUbikFoo implements Foo {
  public NioUbikFoo() throws java.rmi.RemoteException {
  }

  /**
   * @see org.sapia.ubik.rmi.Foo#getBar()
   */
  public Bar getBar() throws RemoteException {
    return new UbikBar();
  }

  public static void main(String[] args) {
    try {
      Log.setWarning();

      //PerfAnalyzer.getInstance().setEnabled(true);
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);
      
      Foo foo = new NioUbikFoo();
      
      props = new Properties();
      props.setProperty(Consts.TRANSPORT_TYPE,
        NioAddress.TRANSPORT_TYPE);
      props.setProperty(NioTcpTransportProvider.PORT, "6060");
      props.setProperty(Consts.SERVER_MAX_THREADS, "10");
      
      foo = (Foo)Hub.exportObject(foo, props);

      ctx.rebind("NioFoo", foo);

      System.out.println("Ubik NIO Foo started...");

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
