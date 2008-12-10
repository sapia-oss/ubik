package org.sapia.ubik.rmi.server.perf;

import javax.management.MBeanInfo;

import junit.framework.TestCase;

public class PerfAnalyzerMBeanTest extends TestCase {
  
  PerfAnalyzerMBean mbean;

  public PerfAnalyzerMBeanTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
    PerfAnalyzer.getInstance().getTopic("test");
    mbean = new PerfAnalyzerMBean();
  }

  public void testGetMBeanInfo() {
    MBeanInfo info = mbean.getMBeanInfo();
  }

  public void testGetAttribute() throws Exception{
    Double duration = (Double)mbean.getAttribute("test");
    Boolean enabled = (Boolean)mbean.getAttribute("IsEnabled");
    assertTrue(!enabled.booleanValue());
  }

  public void testGetAttributes() {
  }

  public void testInvoke() throws Exception{
    mbean.invoke("Enable", new Object[0], new String[0]);
    assertTrue(PerfAnalyzer.getInstance().isEnabled());
    mbean.invoke("Disable", new Object[0], new String[0]);
    assertTrue(!PerfAnalyzer.getInstance().isEnabled());    
  }

}
