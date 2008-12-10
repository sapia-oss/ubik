package org.sapia.ubik.rmi.test.integration;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.UbikFoo;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;
import org.sapia.ubik.rmi.server.Hub;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JndiTest extends TestCase{
	
  static final int PORT = 5152;
	static final int WRONG_PORT = 5153;
  static final String DOMAIN = "JndiTest";
	static final String WRONG_DOMAIN = "default";  
  
  private EmbeddableJNDIServer _server; 
	
	public JndiTest(String name){
		super(name);
	}
	
	/**
   * @see junit.framework.TestCase#setUp()
   */
  private void doSetUp() throws Exception {
		_server = new EmbeddableJNDIServer(DOMAIN, PORT);
		_server.start(false);
	}
	
	/**
   * @see junit.framework.TestCase#tearDown()
   */
  private void doTearDown() throws Exception {
  	_server.stop();
		Hub.shutdown(10000);
  }
  
  public void testAll() throws Exception{
  	doSetUp();
		TestSuite suite = new TestSuite();
		suite.addTest(new JndiTest("doTestDiscovery"));
    suite.addTest(new JndiTest("doTestBind"));			
		suite.addTest(new JndiTest("doTestLookup"));
		suite.addTest(new JndiTest("doTestDiscoveryHelper"));
		TestResult res = super.createResult();
		TestListenerImpl listener = new TestListenerImpl(); 
		res.addListener(listener);
		suite.run(res);
		doTearDown();		
		listener.throwExc();
  }

	public void doTestDiscovery() throws Exception{
		InitialContext ctx = new InitialContext(props(WRONG_PORT, DOMAIN));
		ctx.close();
	}
	
	public void doTestBind() throws Exception{
		InitialContext ctx = new InitialContext(props(PORT, DOMAIN));
		Foo f = new UbikFoo();
		ctx.bind("intg/test/foo", f);
		ctx.close();
	}
	
	public void doTestLookup() throws Exception{
		InitialContext ctx = new InitialContext(props(PORT, DOMAIN));
		Foo f = (Foo)ctx.lookup("intg/test/foo");
		
		try{
			ctx.lookup("intg/test/none");
			throw new Exception("Name should not have been found");
		}catch(NameNotFoundException e){
			//ok
		}

		ctx.close();		
	}
	
	public void doTestDiscoveryHelper() throws Exception{
		DiscoveryHelper helper = new DiscoveryHelper(DOMAIN);
		DiscoListenerImpl listener = new DiscoListenerImpl();
		helper.addServiceDiscoListener(listener);
		helper.addJndiDiscoListener(listener);		
		EmbeddableJNDIServer server = new EmbeddableJNDIServer(DOMAIN, WRONG_PORT);
		server.start(true);
		InitialContext ctx = new InitialContext(props(PORT, DOMAIN));
		Foo f = new UbikFoo();
		ctx.rebind("intg/test/foo", f);
		server.stop();
		ctx.close();
		Thread.sleep(2000);
		super.assertTrue("Service not discovered", listener.serviceDiscovered);
		super.assertTrue("JNDI not discovered", listener.jndiDiscovered);		

	}

	private static Properties props(int port, String domain){
		Properties props = new Properties();
		props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
		props.setProperty(Context.PROVIDER_URL, "ubik://localhost:" + port);
		props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, domain);
		return props;
	}
	
	public static class DiscoListenerImpl 
	  implements ServiceDiscoListener, JndiDiscoListener{
		
		boolean serviceDiscovered, jndiDiscovered;
		/**
     * @see org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener#onServiceDiscovered(org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent)
     */
    public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
			serviceDiscovered = true;
    }
    
    /**
     * @see org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener#onJndiDiscovered(javax.naming.Context)
     */
    public void onJndiDiscovered(Context ctx) {
			jndiDiscovered = true;
    }

	
	}
}
