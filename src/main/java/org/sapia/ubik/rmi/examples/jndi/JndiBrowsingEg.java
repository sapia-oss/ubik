package org.sapia.ubik.rmi.examples.jndi;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.ReliableFoo;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;


/**
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 * @author Yanick Duchesne
 */
public class JndiBrowsingEg {
  /** Creates a new instance of JndiBrowsingEg */
  public JndiBrowsingEg() {
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      Properties props = new Properties();

      // ENABLES MARSHALLING      
      System.setProperty(Consts.MARSHALLING, "true");

      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);
      ReliableFoo    foo = new ReliableFoo();

      System.out.println("Creating subcontext...");

      ctx.createSubcontext("/path1");

      Context child = ctx.createSubcontext("/path1/path2");

      System.out.println("Binding Foo to subcontext...");

      child.bind("foo", foo);
      child.bind("foo", foo);

      System.out.println("Looking up...");

      Foo server = (Foo) ctx.lookup("/path1/path2/foo");

      System.out.println("Looked up; now listing bindings...");

      NamingEnumeration en = ctx.listBindings("/path1");
      Binding           b;

      while (en.hasMore()) {
        b = (Binding) en.next();
        System.out.println("Got child context from enumeration: " +
          b.getName());
        child = (Context) b.getObject();
        child.lookup("foo");
        System.out.println("Looked up Foo from child context");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
