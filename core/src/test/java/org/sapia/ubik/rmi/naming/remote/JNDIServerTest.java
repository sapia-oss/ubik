package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.sapia.ubik.rmi.Consts;

public class JNDIServerTest {

	@Test
	public void testDomainArg() {
	  JNDIServer.Args args = JNDIServer.parseArgs(new String[]{"-d", "test"}, false);
	  assertEquals("test", args.properties.getProperty(Consts.UBIK_DOMAIN_NAME));
	}
	
	@Test
	public void testPortArg() {
	  JNDIServer.Args args = JNDIServer.parseArgs(new String[]{"-p", "2000"}, false);
	  assertEquals(2000, args.port);
	}	
	
	@Test
	public void testFileArg() {
	  JNDIServer.Args args = JNDIServer.parseArgs(
	  		new String[]{
  	  		"-f", 
  	  		System.getProperty("user.dir") + File.separator + "etc" + File.separator + "test" + File.separator + "jndiServer.properties"
	  		}, 
	  		false);
	  assertEquals("test", args.properties.getProperty(Consts.UBIK_DOMAIN_NAME));
	}	

}
