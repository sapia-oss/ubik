package org.sapia.ubik.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A <code>Debug</code> implementation that outputs nothing.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class NullDebug implements Debug{
  
  public boolean on() {
    return false;
  }
  public void on(boolean on) {
  }
  public PrintStream out() {
    return new PrintStream(new NullOutputStream());
  }
  public void out(Class caller, String msg, Throwable err) {
  }
  public void out(Class caller, String msg) {
  }
  final class NullOutputStream extends OutputStream{
    public void write(int b) throws IOException {
    }
  }
}
