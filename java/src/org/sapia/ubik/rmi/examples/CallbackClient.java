package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;

import java.util.Properties;

import javax.naming.InitialContext;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CallbackClient {
  /**
   * Constructor for LookUpEg.
   */
  public CallbackClient() {
    super();
  }

  public static void main(String[] args) {
    try {
      Log.setWarning();

      Properties props = new Properties();

      // ENABLES CALL BACK      
      System.setProperty(Consts.CALLBACK_ENABLED, "true");

      // ENABLES MARSHALLING      
      System.setProperty(Consts.MARSHALLING, "true");

      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1100");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);
      Foo            f = (Foo) ctx.lookup("Foo");

      for (int i = 0; i < 10; i++) {
        System.out.println(f.getBar().getMsg());
        Thread.sleep(2000);
      }

      Hub.shutdown(30000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
