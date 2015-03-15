package org.sapia.ubik.log;

/**
 * A {@link Category}, which can be used instead of using the {@link Log} class
 * directly. It is more convenient to use an instance of this class, since it
 * spares passing the origin of the log at every method call.
 * <p>
 * Also, an instance of this class provides methods allowing for format
 * specifiers and arguments, such as in {@link String#format(String, Object...)}.
 * 
 * @author yduchesne
 */
public class Category {

  /**
   * An empty to pass to the different log methods when no arguments are passed
   * in.
   */
  public static final Object[] EMPTY_ARGS = new Object[] {};

  /**
   * The name of the entity that uses this instance for logging.
   */
  private String name;

  Category(String name) {
    this.name = name;
  }

  public void trace(Object msg) {
    if (Log.isTrace()) {
      Log.trace(name, msg);
    }
  }

  public void trace(Object msg, Object... args) {
    if (Log.isTrace()) {
      Log.trace(name, String.format(msg.toString(), args));
    }
  }

  public void debug(Object msg) {
    if (Log.isDebug()) {
      Log.debug(name, msg);
    }
  }

  public void debug(Object msg, Object... args) {
    if (Log.isDebug()) {
      Log.debug(name, String.format(msg.toString(), args));
    }
  }

  public void debug(Object msg, Throwable err, Object... args) {
    if (Log.isDebug()) {
      Log.debug(name, String.format(msg.toString(), args));
    }
  }

  public void info(Object msg) {
    if (Log.isInfo()) {
      Log.info(name, msg);
    }
  }

  public void info(Object msg, Object... args) {
    if (Log.isInfo()) {
      Log.info(name, String.format(msg.toString(), args));
    }
  }
  
  public void info(Object msg, Throwable err) {
    if (Log.isInfo()) {
      Log.info(name, msg, err);
    }
  }

  public void info(Object msg, Throwable err, Object... args) {
    if (Log.isInfo()) {
      Log.info(name, String.format(msg.toString(), args), err);
    }
  }

  public void warning(Object msg) {
    if (Log.isWarning()) {
      Log.warning(name, msg);
    }
  }

  public void warning(Object msg, Object... args) {
    if (Log.isWarning()) {
      Log.warning(name, String.format(msg.toString(), args));
    }
  }

  public void warning(Object msg, Throwable err) {
    if (Log.isWarning()) {
      Log.warning(name, msg, err);
    }
  }

  public void warning(Object msg, Throwable err, Object... args) {
    if (Log.isWarning()) {
      Log.warning(name, String.format(msg.toString(), args), err);
    }
  }

  public void error(Object msg) {
    if (Log.isError()) {
      Log.error(name, msg);
    }
  }

  public void error(Object msg, Object... args) {
    if (Log.isError()) {
      Log.error(name, String.format(msg.toString(), args));
    }
  }

  public void error(Object msg, Throwable err) {
    if (Log.isError()) {
      Log.error(name, msg, err);
    }
  }

  public void error(Object msg, Throwable err, Object... args) {
    if (Log.isError()) {
      Log.error(name, String.format(msg.toString(), args), err);
    }
  }

  public void report(Object msg) {
    Log.report(name, msg);
  }

  public void report(Object msg, Object... args) {
    Log.report(name, String.format(msg.toString(), args));
  }

  public boolean isTrace() {
    return Log.isTrace();
  }

  public boolean isDebug() {
    return Log.isDebug();
  }

  public boolean isInfo() {
    return Log.isInfo();
  }

  public boolean isWarning() {
    return Log.isWarning();
  }

  public boolean isError() {
    return Log.isError();
  }

  public boolean isReport() {
    return Log.isReport();
  }
  
  public Object[] noArgs() {
    return EMPTY_ARGS;
  }
}
