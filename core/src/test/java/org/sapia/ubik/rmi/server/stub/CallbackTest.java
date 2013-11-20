package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;

public class CallbackTest {

  private TestCallBack callback;

  @Before
  public void setUp() {
    System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
    System.setProperty(Consts.CALLBACK_ENABLED, "true");
    callback = new TestCallBack();
  }

  @After
  public void tearDown() {
    System.clearProperty(Consts.CALLBACK_ENABLED);
    System.clearProperty(Consts.COLOCATED_CALLS_ENABLED);
    Hub.shutdown();
  }

  @Test
  public void testCallbackInvocation() throws Exception {
    TestCallbackInterface remote = (TestCallbackInterface) Hub.exportObject(this.callback, 7070);

    for (int i = 0; i < 5; i++) {
      System.out.println(remote.callMethod());
    }

    assertEquals(5, callback.counter);

  }

}
