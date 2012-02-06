package org.sapia.ubik.rmi.examples.jndi;

import java.util.Properties;

import javax.naming.InitialContext;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JndiConnectEg {
  public static void main(String[] args) {
    try {
      Properties props = new Properties();

      // ENABLES MARSHALLING      
      System.setProperty(Consts.MARSHALLING, "true");

      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
