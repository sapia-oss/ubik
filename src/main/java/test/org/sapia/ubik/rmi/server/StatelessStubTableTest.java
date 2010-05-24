/*
 * StatelessStubTableTest.java
 * JUnit based test
 *
 * Created on October 31, 2005, 2:18 PM
 */

package org.sapia.ubik.rmi.server;

import junit.framework.*;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Name;
import org.sapia.archie.impl.DefaultNameParser;
import org.sapia.archie.jndi.JndiNameParser;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.naming.remote.archie.SyncPutEvent;

/**
 *
 * @author yduchesne
 */
public class StatelessStubTableTest extends TestCase {
  
  public StatelessStubTableTest(String testName) {
    super(testName);
  }

  protected void setUp() throws Exception {
  }

  protected void tearDown() throws Exception {
  }

  /**
   * Test of doRegister method, of class org.sapia.ubik.rmi.server.StatelessStubTable.
   */
  public void testDoRegister() throws Exception{
    JndiNameParser parser = new JndiNameParser(new DefaultNameParser());
    Name name = parser.parse("stub");
    RemoteRefStateless ref = new RemoteRefStateless(name, "test");
    StatelessStubTable.doRegister(ref);
    super.assertEquals(1, StatelessStubTable.getSiblings("test").size());
    
  }
  
}
