package org.sapia.ubik.rmi.interceptor;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MultiDispatcherTest extends TestCase {
  /**
   * Constructor for MultiDispatcherTest.
   * @param arg0
   */
  public MultiDispatcherTest(String arg0) {
    super(arg0);
  }

  public void testAdd() throws Exception {
    MultiDispatcher d = new MultiDispatcher();
    TestInterceptor t = new TestInterceptor();
    d.addInterceptor(TestEvent.class, t);
  }

  public void testMultiAdd() throws Exception {
    MultiDispatcher d  = new MultiDispatcher();
    TestInterceptor t1 = new TestInterceptor();
    TestInterceptor t2 = new TestInterceptor();
    d.addInterceptor(TestEvent.class, t1);
    d.addInterceptor(TestEvent.class, t2);
  }

  public void testDispatch() throws Exception {
    MultiDispatcher d  = new MultiDispatcher();
    TestInterceptor t1 = new TestInterceptor();
    TestInterceptor t2 = new TestInterceptor();
    d.addInterceptor(TestEvent.class, t1);
    d.addInterceptor(TestEvent.class, t2);
    d.dispatch(new TestEvent());
    super.assertEquals(1, t1.count);
    super.assertEquals(1, t2.count);
  }
}
