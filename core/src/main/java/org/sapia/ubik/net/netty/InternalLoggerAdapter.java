package org.sapia.ubik.net.netty;

import org.jboss.netty.logging.InternalLogLevel;
import org.jboss.netty.logging.InternalLogger;
import org.sapia.ubik.log.Category;

/**
 * Adapts the {@link Category} class to Netty's {@link InternalLogger} interface. 
 * 
 * @author yduchesne
 *
 */
public class InternalLoggerAdapter implements InternalLogger {
  
  private Category log;
  
  public InternalLoggerAdapter(Category log) {
    this.log = log;
  }
  
  @Override
  public void debug(String arg0) {
    log.debug(arg0);
  }
  
  @Override
  public void debug(String arg0, Throwable arg1) {
    log.debug(arg0, arg1);
  }
  
  @Override
  public void info(String arg0) {
    log.info(arg0);
  }
  
  @Override
  public void info(String arg0, Throwable arg1) {
    log.info(arg0, arg1);
  }
  
  @Override
  public void warn(String arg0) {
    log.warning(arg0);
  }
  
  @Override
  public void warn(String arg0, Throwable arg1) {
    log.warning(arg0, arg1);
  }
    
  @Override
  public void error(String arg0) {
    log.error(arg0);
    
  }
  @Override
  public void error(String arg0, Throwable arg1) {
    log.error(arg0, arg1);
  }  
  
  @Override
  public boolean isDebugEnabled() {
    return log.isDebug();
  }
  
  @Override
  public boolean isErrorEnabled() {
    return log.isError();
  }  
  
  @Override
  public boolean isInfoEnabled() {
    return log.isInfo();
  }
  
  @Override
  public boolean isWarnEnabled() {
    return log.isWarning();
  }
  
  @Override
  public boolean isEnabled(InternalLogLevel arg0) {
    if (arg0 == InternalLogLevel.DEBUG) {
      return log.isDebug();
    } else if (arg0 == InternalLogLevel.INFO) {
      return log.isInfo();
    } else if (arg0 == InternalLogLevel.WARN) {
      return log.isWarning();
    } else {
      return log.isError();
    }     
  }  
  
  @Override
  public void log(InternalLogLevel arg0, String arg1) {
    if (arg0 == InternalLogLevel.DEBUG) {
      log.debug(arg1);
    } else if (arg0 == InternalLogLevel.INFO) {
      log.info(arg1);
    } else if (arg0 == InternalLogLevel.WARN) {
      log.warning(arg1);
    } else {
      log.error(arg1);
    }
  }
  
  @Override
  public void log(InternalLogLevel arg0, String arg1, Throwable arg2) {
    if (arg0 == InternalLogLevel.DEBUG) {
      log.debug(arg1, arg2);
    } else if (arg0 == InternalLogLevel.INFO) {
      log.info(arg1, arg2);
    } else if (arg0 == InternalLogLevel.WARN) {
      log.warning(arg1, arg2);
    } else {
      log.error(arg1, arg2);
    }    
  }

}
