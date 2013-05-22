package org.sapia.ubik.rmi.examples;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.InitialContext;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.mina.MinaAddress;
import org.sapia.ubik.rmi.server.transport.mina.MinaTransportProvider;


/**
 * @author Yanick Duchesne
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
        MinaTransportProvider.TRANSPORT_TYPE);
      props.setProperty(MinaTransportProvider.PORT, "6060");
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
