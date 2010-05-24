package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

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
public class FailOverEg {
  /**
   * Constructor for LookUpEg.
   */
  public FailOverEg() {
    super();
  }

  public static void main(String[] args) {
    try {
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);

      Object         obj = ctx.lookup("Foo");
      System.out.println("Looked up: " + obj.getClass().getName());

      Foo f = (Foo) obj;

      System.out.println("Waiting 10s...");
      Thread.sleep(10000);
      System.out.println("Performing call...");
      System.out.println(f.getBar().getMsg());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
