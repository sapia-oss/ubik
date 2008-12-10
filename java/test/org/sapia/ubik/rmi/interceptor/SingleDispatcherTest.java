package org.sapia.ubik.rmi.interceptor;

import junit.framework.*;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SingleDispatcherTest extends TestCase {
  /**
   * Constructor for SingleDispatcherTest.
   * @param arg0
   */
  public SingleDispatcherTest(String arg0) {
    super(arg0);
  }

  public void testRegister() throws Exception {
    SingleDispatcher d = new SingleDispatcher();
    TestInterceptor  t = new TestInterceptor();
    d.registerInterceptor(TestEvent.class, t);
  }

  public void testDuplicateRegister() throws Exception {
    SingleDispatcher d  = new SingleDispatcher();
    TestInterceptor  t1 = new TestInterceptor();
    TestInterceptor  t2 = new TestInterceptor();
    d.registerInterceptor(TestEvent.class, t1);

    try {
      d.registerInterceptor(TestEvent.class, t2);
      throw new Exception("duplicate registration not signaled");
    } catch (InvalidInterceptorException e) {
      // ok
    }
  }

  public void testDispatch() throws Exception {
    SingleDispatcher d = new SingleDispatcher();
    TestInterceptor  t = new TestInterceptor();
    d.registerInterceptor(TestEvent.class, t);
    d.dispatch(new TestEvent());
    super.assertEquals(1, t.count);
  }
}
