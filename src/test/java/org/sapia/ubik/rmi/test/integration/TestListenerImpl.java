package org.sapia.ubik.rmi.test.integration;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestListenerImpl implements TestListener {
  public List errors   = new ArrayList();
  public List failures = new ArrayList();

  /**
   * @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable)
   */
  public void addError(Test arg0, Throwable arg1) {
    arg1.printStackTrace(System.out);
    errors.add(arg1);
  }

  /**
   * @see junit.framework.TestListener#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
   */
  public void addFailure(Test arg0, AssertionFailedError arg1) {
    arg1.printStackTrace(System.out);
    failures.add(arg1);
  }

  /**
   * @see junit.framework.TestListener#startTest(junit.framework.Test)
   */
  public void startTest(Test arg0) {
    System.out.println("========> " + arg0.toString());
  }

  /**
   * @see junit.framework.TestListener#endTest(junit.framework.Test)
   */
  public void endTest(Test arg0) {
    System.out.println("       ... SUCCESSFUL ...");
  }

  public void throwExc() throws Exception {
    if (errors.size() > 0) {
      throw (Exception) errors.get(0);
    } else if (failures.size() > 0) {
      throw (AssertionFailedError) failures.get(0);
    }
  }
}
