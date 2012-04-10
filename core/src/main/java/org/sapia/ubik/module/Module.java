package org.sapia.ubik.module;

/**
 * This interfaces specifies the behavior of Ubik's internal modules.
 *   
 * @author yduchesne
 *
 */
public interface Module {
  
  /**
   * Called upon Ubik startup. This method is called only once in this
   * instance's lifetime.
   * 
   * @param the {@link ModuleContext} that is passed to this instance.
   */
  public void init(ModuleContext context);
  
  /**
   * Called after {@link #init()}, when all modules have initialized.
   * 
   * @param the {@link ModuleContext} that is passed to this instance.
   */
  public void start(ModuleContext context);
  
  /**
   * Called upon Ubik shutdown.
   */
  public void stop();
 

}
