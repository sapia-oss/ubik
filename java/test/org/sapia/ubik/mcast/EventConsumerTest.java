package org.sapia.ubik.mcast;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class EventConsumerTest extends TestCase {
  public EventConsumerTest(String arg0) {
    super(arg0);
  }

  public void testMatchesAll() throws Exception {
    EventConsumer cons = new EventConsumer("123", "default");
    DomainName other = DomainName.parse("local");
    DomainName thisDomain = DomainName.parse("default");
    super.assertTrue(!cons.matchesAll(other, "456"));
    super.assertTrue(!cons.matchesAll(thisDomain, "456"));
    super.assertTrue(cons.matchesAll(null, "456"));    
    super.assertTrue(!cons.matchesAll(null, "123"));        
  }

  public void testMatchesThis() throws Exception {
    EventConsumer cons = new EventConsumer("123", "default");
    DomainName other = DomainName.parse("local");
    DomainName thisDomain = DomainName.parse("default");
    super.assertTrue(!cons.matchesThis(other, "456"));
    super.assertTrue(cons.matchesThis(thisDomain, "456"));
    super.assertTrue(!cons.matchesThis(thisDomain, "123"));
  }
  
  public void testUnregisterAsyncListener() throws Exception {
    EventConsumer     cons     = new EventConsumer("node", "domain");
    TestEventListener listener = new TestEventListener();
    cons.registerAsyncListener("test", listener);
    cons.unregisterListener((AsyncEventListener) listener);
    super.assertTrue(!cons.containsAsyncListener(listener));
    super.assertEquals(0, cons.getCount());
  }

  public void testUnregisterSyncListener() throws Exception {
    EventConsumer     cons     = new EventConsumer("node", "domain");
    SyncEventListener listener = new TestEventListener();
    cons.registerSyncListener("test", listener);
    cons.unregisterListener(listener);
    super.assertEquals(0, cons.getCount());
  }
}
