package org.sapia.ubik.rmi.server;

import java.io.PrintStream;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Debug;


/**
 * A logger that logs to System.out, used by the RMI runtime.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Log {
  static final int              DEBUG   = 0;
  static final int              INFO    = 1;
  static final int              WARNING = 2;
  static final int              ERROR   = 3;
	static final int              REPORT  = 4;  
  private static int            _lvl    = ERROR;
  private static final String[] LABELS  = new String[] {
      "debug", "info", "warning", "error"
    };

  static {
    String label = System.getProperty(Consts.LOG_LEVEL);

    if (label != null) {
      for (int i = 0; i < LABELS.length; i++) {
        if (LABELS[i].equals(label)) {
          _lvl = i;

          break;
        }
      }
    }
  }

  public static final void setDebug() {
    _lvl = DEBUG;
  }

  public static final void setInfo() {
    _lvl = INFO;
  }

  public static final void setWarning() {
    _lvl = WARNING;
  }

  public static final void setError() {
    _lvl = ERROR;
  }

  public static final void debug(Class<?> caller, Object msg) {
    if (_lvl <= DEBUG) {
      debug(caller.getName(), msg);
    }
  }

  public static final void debug(String caller, Object msg) {
    if (_lvl <= DEBUG) {
      display(caller, msg);
    }
  }

  public static final void info(Class<?> caller, Object msg) {
    if (_lvl <= INFO) {
      info(caller.getName(), msg);
    }
  }

  public static final void info(Class<?> caller, Object msg, Throwable t) {
    if (_lvl <= INFO) {
      display(caller.getName(), msg, t);
    }
  }
  
  public static final void info(String caller, Object msg) {
    if (_lvl <= INFO) {
      display(caller, msg);
    }
  }

  public static final void warning(Class<?> caller, Object msg) {
    if (_lvl <= WARNING) {
      warning(caller.getName(), msg);
    }
  }

  public static final void warning(Class<?> caller, Object msg, Throwable err){
    if (_lvl <= WARNING) {
      warning(caller.getName(), msg, err);
    }
  }

  public static final void warning(String caller, Object msg) {
    if (_lvl <= WARNING) {
      display(caller, msg);
    }
  }

  public static final void warning(String caller, Object msg, Throwable err) {
    if (_lvl <= WARNING) {
      display(caller, msg, err);
    }
  }
  public static final void error(Class<?> caller, Object msg) {
    if (_lvl <= ERROR) {
      error(caller.getName(), msg);
    }
  }

  public static final void error(String caller, Object msg) {
    if (_lvl <= ERROR) {
      display(caller, msg);
    }
  }

  public static final void error(Class<?> caller, Object msg, Throwable t) {
    if (_lvl <= ERROR) {
      error(caller.getName(), msg, t);
    }
  }

  public static final void error(String caller, Object msg, Throwable t) {
    if (_lvl <= ERROR) {
      display(caller, msg, t);
    }
  }
  
	public static final void report(Class<?> caller, Object msg){
		display(caller.getName(), msg);
	}  
  
  public static final void report(String caller, Object msg){
  	display(caller, msg);
  }

  public static boolean isDebug() {
    return _lvl <= DEBUG;
  }

  public static boolean isInfo() {
    return _lvl <= INFO;
  }

  public static boolean isWarning() {
    return _lvl <= WARNING;
  }

  public static boolean isError() {
    return _lvl <= ERROR;
  }

  public static int getLevel() {
    return _lvl;
  }
  
  public static Debug getDebugImpl(){
    return new RmiDebug();
  }

  private static void display(String caller, Object msg) {
    if (msg instanceof Throwable) {
      System.out.println("[" + new java.util.Date().toString() + "][" + caller +
        "] " + ((Throwable) msg).getMessage());
      ((Throwable) msg).printStackTrace();
    } else {
      System.out.println("[" + new java.util.Date().toString() + "][" + caller +
        "] " + msg);
    }
  }

  private static void display(String caller, Object msg, Throwable t) {
    System.out.println("[" + new java.util.Date().toString() + "][" + caller +
      "] " + msg + " - " + t.getMessage());

    t.printStackTrace();
  }
  
  /////////////////////////////// Debug Impl ///////////////////////////////
  
  @SuppressWarnings(value="unchecked")
  static final class RmiDebug implements Debug{
  
    public void out(Class caller, String msg){
      Log.debug(caller, msg);
    }
  
    public void out(Class caller, String msg, Throwable err){
      Log.error(caller, msg, err);        
    }
  
    public PrintStream out(){
      return System.out;
    }
   
    public boolean on(){
      return true;
    }

    public void on(boolean on){
    }
  }
}
