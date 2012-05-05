package org.sapia.ubik.rmi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.Hub;

public class RemoteAnnotationTest {
  
  private SelectedInterfacesRemoteObject remote1;
  
  @Before
  public void setUp() {
    Hub.shutdown();
    System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
    this.remote1 = new SelectedInterfacesRemoteObject();
  }
  
  @After
  public void teardown() {
    System.clearProperty(Consts.COLOCATED_CALLS_ENABLED);
    Hub.shutdown();
  }

  @Test
  public void testRemoteAnnotationWithSelectedInterfaces() throws Exception {
    Object exported = Hub.exportObject(remote1);
    assertTrue("Stub should implement selected interface", exported instanceof SelectedInterface);
    assertFalse("Stub should not implement non-selected interface", exported instanceof NonSelectedInterface);
    
    SelectedInterface intf = (SelectedInterface) exported;
    ReturnValue value = intf.getValue();
    assertFalse("Return value should not implement non-selected interface", value instanceof NonSelectedInterface);
  }
  
  @Remote(interfaces=ReturnValue.class)
  public static class ReturnValueImpl implements ReturnValue, NonSelectedInterface {
    
  }
  
  @Remote(interfaces=SelectedInterface.class)
  public static class SelectedInterfacesRemoteObject implements SelectedInterface, NonSelectedInterface {
    
    private ReturnValue value;
    @Override
    public ReturnValue getValue() {
      return value = new ReturnValueImpl();
    }
  }

}
