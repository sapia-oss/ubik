package org.sapia.ubik.net.netty;

import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.sapia.ubik.log.Log;

/**
 * Creates {@link InternalLoggerAdapter}s.
 * 
 * @author yduchesne
 *
 */
public class InternalLoggerFactoryAdapter extends InternalLoggerFactory {
  
  @Override
  public InternalLogger newInstance(String arg0) {
    return new InternalLoggerAdapter(Log.createCategory(arg0));
  }

}
