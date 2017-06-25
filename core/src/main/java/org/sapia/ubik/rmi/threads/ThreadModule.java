package org.sapia.ubik.rmi.threads;

import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;

/**
 * Interacts with the {@link Threads} class, managing its lifecycle.
 * 
 * @author yduchesne
 *
 */
public class ThreadModule implements Module {

  @Override
  public void init(ModuleContext context) {
  }
  
  @Override
  public void start(ModuleContext context) {
  }
  
  @Override
  public void stop() {
    Threads.shutdown();
  }
}
