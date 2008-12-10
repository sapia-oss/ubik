package org.sapia.ubik.util;

import java.io.PrintStream;

/**
 * A <code>Debug</code> implementation that logs to stdout.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class StdoutDebug implements Debug{
  
  private boolean _on;
  
  /**
   * @see org.sapia.ubik.net.nio.util.Debug#out()
   */
  public PrintStream out() {
    return System.out;
  }
  
  /**
   * @see org.sapia.ubik.net.nio.util.Debug#on()
   */
  public boolean on() {
    return _on;
  }
  
  public void on(boolean on){
    _on = on;
  }
  
  /**
   * @see org.sapia.ubik.net.nio.util.Debug#out(java.lang.Class, java.lang.String)
   */
  public void out(Class caller, String msg) {
    if(_on){
      StringBuffer buf = new StringBuffer("[");
      buf.append(caller.getName());
      buf.append("] ");
      buf.append(msg);
      System.out.println(buf.toString());
    }
  }
  
  /**
   * @see org.sapia.ubik.net.nio.util.Debug#out(java.lang.Class, java.lang.String, java.lang.Throwable)
   */
  public void out(Class caller, String msg, Throwable err) {
    if(_on){
      StringBuffer buf = new StringBuffer("[");
      buf.append(caller.getName());
      buf.append("] ");
      if(msg != null)
        buf.append(msg);
      System.out.println(buf.toString());
      if(err != null)
        err.printStackTrace(System.out);
    }
  }

}
