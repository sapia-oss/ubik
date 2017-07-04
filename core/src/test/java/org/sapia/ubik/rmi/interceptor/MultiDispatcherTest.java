package org.sapia.ubik.rmi.interceptor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MultiDispatcherTest {

  private MultiDispatcher dispatcher;
  
  @Before
  public void setUp() {
    dispatcher = new MultiDispatcher();
  }
  
  @Test(expected = InvalidInterceptorException.class)
  public void testAddInterceptor_with_wrong_event_class() throws Exception {
    TestInterceptor t = new TestInterceptor();
    dispatcher.addInterceptor(String.class, t);
  }
  
  @Test
  public void testDispatch_with_single_interceptor() throws Exception {
    TestInterceptor t = new TestInterceptor();
    dispatcher.addInterceptor(TestEvent.class, t);
    dispatcher.dispatch(new TestEvent());

    assertEquals(1, t.count);
  }

  @Test
  public void testDispatch_with_multiple_interceptors() throws Exception {
    TestInterceptor t1 = new TestInterceptor();
    TestInterceptor t2 = new TestInterceptor();
    dispatcher.addInterceptor(TestEvent.class, t1);
    dispatcher.addInterceptor(TestEvent.class, t2);
    
    dispatcher.dispatch(new TestEvent());

    assertEquals(1, t1.count);
    assertEquals(1, t2.count);
  }
  
  @Test
  public void testDispatch_with_event_subclass() throws Exception {
    TestInterceptor t = new TestInterceptor();
    dispatcher.addInterceptor(TestEventSubclass.class, t);
    dispatcher.dispatch(new TestEvent());
 
    assertEquals(1, t.count);
  }
  
  @Test
  public void testDispatch_with_unregistered_event_class() throws Exception {
    TestInterceptor t = new TestInterceptor();
    dispatcher.addInterceptor(TestEvent.class, t);
    dispatcher.dispatch(new TestUnregisteredEvent());
 
    assertEquals(1, t.count);
  }
  
  
  @Test
  public void testDispatch_with_unrelated_event() throws Exception {
    TestInterceptor t = new TestInterceptor();
    dispatcher.addInterceptor(TestEvent.class, t);
    dispatcher.dispatch(new TestUnrelatedEvent());
 
    assertEquals(0, t.count);
  }
}
