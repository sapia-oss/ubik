package org.sapia.ubik.rmi.server.stub;

import org.sapia.ubik.module.ModuleContext;

/**
 * Specifies the behavior common to stub-related strategies.
 * 
 * @author yduchesne
 * 
 */
public interface StubStrategy {

  /**
   * Performs initialization.
   * 
   * @param context
   *          the {@link ModuleContext}
   */
  public void init(ModuleContext context);

}
