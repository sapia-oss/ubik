package org.sapia.ubik.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.sapia.ubik.rmi.Consts;


/**
 * Logs to System.out, used by the RMI runtime.
 *
 * @author Yanick Duchesne
 */
public class Log {
  
  /**
   * Holds the various log levels.
   * 
   */
  public enum Level {

    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    REPORT(5);
    
    private int value;
    
    private Level(int value) {
      this.value = value;
    }
    
  }
  
  // -------------------------------------------------------------------------- 
  // class variables
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS");
  private static       LogFilter  filter      = new LogFilter.DefaultFilter();
  private static       LogOutput  output      = new LogOutput.DefaultLogOutput();
  private static       Level      lvl         = Level.ERROR;

  static {
    String label = System.getProperty(Consts.LOG_LEVEL);

    if (label != null) {
      for (Level l : Level.values()) {
        if (l.name().equalsIgnoreCase(label)) {
          lvl = l;

          break;
        }
      }
    }
  }

  // -------------------------------------------------------------------------- 
  // config setters
  public static synchronized final void setLogFilter(LogFilter someFilter) {
    filter = someFilter;
  }
  
  public static synchronized final void setLogOutput(LogOutput someOutput) {
    LogOutput oldOutput = output;
    output = someOutput;
    oldOutput.close();
  }

  public static final void setTrace() {
    lvl = Level.TRACE;
  }

  public static final void setDebug() {
    lvl = Level.DEBUG;
  }

  public static final void setInfo() {
    lvl = Level.INFO;
  }

  public static final void setWarning() {
    lvl = Level.WARNING;
  }

  public static final void setError() {
    lvl = Level.ERROR;
  }

  // --------------------------------------------------------------------------

  public static final void trace(Class<?> caller, Object msg) {
    trace(caller.getName(), msg);
  }

  public static final void trace(String caller, Object msg) {
    if (lvl.value <= Level.TRACE.value) {
      display(caller, Level.TRACE, msg);
    }
  }
  
  // --------------------------------------------------------------------------
  
  public static final void debug(Class<?> caller, Object msg) {
    debug(caller.getName(), msg);
  }

  public static final void debug(String caller, Object msg) {
    if (lvl.value <= Level.DEBUG.value) {
      display(caller, Level.DEBUG, msg);
    }
  }
  
  // --------------------------------------------------------------------------
  
  public static final void info(Class<?> caller, Object msg) {
    info(caller.getName(), msg);
  }

  public static final void info(Class<?> caller, Object msg, Throwable t) {
    if (lvl.value <= Level.INFO.value) {
      display(caller.getName(), Level.INFO, msg, t);
    }
  }
  
  public static final void info(String caller, Object msg) {
    if (lvl.value <= Level.INFO.value) {
      display(caller, Level.INFO, msg);
    }
  }

  // --------------------------------------------------------------------------
  
  public static final void warning(Class<?> caller, Object msg) {
    warning(caller.getName(), msg);
  }

  public static final void warning(Class<?> caller, Object msg, Throwable err){
    warning(caller.getName(), msg, err);
  }

  public static final void warning(String caller, Object msg) {
    if (lvl.value <= Level.WARNING.value) {
      display(caller, Level.WARNING, msg);
    }
  }

  public static final void warning(String caller, Object msg, Throwable err) {
    if (lvl.value <= Level.WARNING.value) {
      display(caller, Level.WARNING, msg, err);
    }
  }
  
  // --------------------------------------------------------------------------
  // Error
  public static final void error(Class<?> caller, Object msg) {
    error(caller.getName(), msg);
  }

  public static final void error(String caller, Object msg) {
    if (lvl.value <= Level.ERROR.value) {
      display(caller, Level.ERROR, msg);
    }
  }

  public static final void error(Class<?> caller, Object msg, Throwable t) {
    error(caller.getName(), msg, t);
  }

  public static final void error(String caller, Object msg, Throwable t) {
    if (lvl.value <= Level.ERROR.value) {
      display(caller, Level.ERROR, msg, t);
    }
  }
  
  // --------------------------------------------------------------------------
  // Report
	public static final void report(Class<?> caller, Object msg){
		display(caller.getName(), Level.REPORT, msg);
	}  
  
  public static final void report(String caller, Object msg){
  	display(caller, Level.REPORT, msg);
  }

  // --------------------------------------------------------------------------
  // is<Level>
  public static boolean isTrace() {
    return lvl.value <= Level.TRACE.value;
  }
  
  public static boolean isDebug() {
    return lvl.value <= Level.DEBUG.value;
  }

  public static boolean isInfo() {
    return lvl.value <= Level.INFO.value;
  }

  public static boolean isWarning() {
    return lvl.value <= Level.WARNING.value;
  }

  public static boolean isError() {
    return lvl.value <= Level.ERROR.value;
  }

  public static Level getLevel() {
    return lvl;
  }
  
  public static boolean isLoggable(Level level) {
    return lvl.value <= level.value;
  }
  
  public static Category createCategory(String name) {
    return new Category(name);
  }
  
  public static Category createCategory(Class<?> clazz) {
    return new Category(clazz.getName());
  }

  private static void display(String caller, Level level, Object msg) {
    if(level == Level.ERROR || filter.accepts(caller)) {
      if (msg instanceof Throwable) {
        output.log("[" + DATE_FORMAT.format(new java.util.Date()) + "][" + caller +
          "@" + Thread.currentThread().getName() + "] " + ((Throwable) msg).getMessage());
        output.log((Throwable) msg);
      } else {
        output.log("[" + DATE_FORMAT.format(new java.util.Date()) + "][" + caller +
          "@" + Thread.currentThread().getName() + "] " + msg);
      }
    }
  }

  private static void display(String caller, Level level, Object msg, Throwable t) {
    if(level == Level.ERROR || filter.accepts(caller)) {
      output.log("[" + DATE_FORMAT.format(new java.util.Date()) + "][" + caller +
          "@" + Thread.currentThread().getName() + "] " + msg + " - " + t.getMessage());
      output.log(t);
    }
  }
  
}
