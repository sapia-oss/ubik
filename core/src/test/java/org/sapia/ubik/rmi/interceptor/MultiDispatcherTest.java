package org.sapia.ubik.rmi.interceptor;

import junit.framework.TestCase;

/**
 * @author Yanick Duchesne
 */
public class MultiDispatcherTest extends TestCase {
  /**
   * Constructor for MultiDispatcherTest.
   * 
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
    MultiDispatcher d = new MultiDispatcher();
    TestInterceptor t1 = new TestInterceptor();
    TestInterceptor t2 = new TestInterceptor();
    d.addInterceptor(TestEvent.class, t1);
    d.addInterceptor(TestEvent.class, t2);
  }

  public void testDispatch() throws Exception {
    MultiDispatcher d = new MultiDispatcher();
    TestInterceptor t1 = new TestInterceptor();
    TestInterceptor t2 = new TestInterceptor();
    d.addInterceptor(TestEvent.class, t1);
    d.addInterceptor(TestEvent.class, t2);
    d.dispatch(new TestEvent());
    super.assertEquals(1, t1.count);
    super.assertEquals(1, t2.count);
  }
}
