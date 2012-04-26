package org.sapia.ubik.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.sapia.ubik.jmx.JmxHelper;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.ModuleContext.State;

/**
 * An instance of this class holds and manages {@link Module}s.
 * 
 * @author yduchesne
 *
 */
public class ModuleContainer {

  
  private Category            log           = Log.createCategory(getClass());
  private Map<String, Module> modulesByName = new HashMap<String, Module>();
  private List<ModuleRef>     moduleList    = new ArrayList<ModuleRef>();
  private volatile State      state         = State.CREATED;
  
  /**
   * Binds the given module to this instance.
   * 
   * @param moduleClass the class or interface under which to bind the given module.
   * @param module the {@link Module} to bind.
   * @throws ModuleAlreadyBoundException if a module has already been bound under the given name.
   * @return this instance.
   */
  public ModuleContainer bind(Class<?> moduleClass, Module module) throws ModuleAlreadyBoundException {
    bind(moduleClass.getName(), module);
    return this;
  }
  
  /**
   * Binds the given module to this instance.
   * 
   * @param name the name of the module, which must be unique in the context of this instance.
   * @param module the {@link Module} to bind.
   * @throws ModuleAlreadyBoundException if a module has already been bound under the given name.
   * @return this instance.
   */
  public ModuleContainer bind(String name, Module module) throws ModuleAlreadyBoundException {
    if(modulesByName.containsKey(name)) {
      throw new ModuleAlreadyBoundException(name);
    }
    modulesByName.put(name, module);
    moduleList.add(new ModuleRef(module));
    return this;
  }
  
  /**
   * Binds the given module to this instance.
   * 
   * @param module the {@link Module} to bind.
   * @throws ModuleAlreadyBoundException if the module has already been bound.
   * @return this instance.
   */
  public ModuleContainer bind(Module module) throws ModuleAlreadyBoundException {
    bind(module.getClass(), module);
    return this;
  }  
  
  /**
   * @param <T> the generic type of the module.
   * @param moduleClass the class of the module to look for.
   * @return the module that was found.
   * @throws ModuleNotFoundException if no such module was found.
   */
  public <T> T lookup(Class<T> moduleClass) throws ModuleNotFoundException{
    Module module = modulesByName.get(moduleClass.getName());
    if(module == null) {
      for(Module candidate : modulesByName.values()) {
        if(Module.class.isAssignableFrom(candidate.getClass())) {
          module = candidate;
          break;
        }
      }
      
      if(module == null) {
        throw new ModuleNotFoundException(moduleClass.getName());
      }
    }
    return moduleClass.cast(module);
  }
  
  /**
   * @param <T> the generic type of the module.
   * @param moduleClass the class or interface to internally cast the module to.
   * @param name the name of the module to look for.
   * @return the module that was found.
   * @throws ModuleNotFoundException if no such module was found.
   */  
  public <T> T lookup(Class<T> moduleClass, String name) throws ModuleNotFoundException {
    Module module = modulesByName.get(name);
    if(module == null) {
      throw new ModuleNotFoundException(name);
    }
    return moduleClass.cast(module);
  }
  
  /**
   * Initializes this instance's modules.
   */
  public synchronized void init() {
    if(state == State.CREATED || state == State.STOPPED) {
      ModuleContext ctx = new ModuleContextImpl();
      state = State.INITIALIZING;
      try {
        for(ModuleRef module : moduleList) {
          log.debug("Initializing module %s", module.module.getClass().getSimpleName());
          module.init(ctx);
        }
      } finally {
        state = State.INITIALIZED;
      }
    }
  }
  
  /**
   * Starts this instance's modules.
   */
  public synchronized void start() {
    if(state == State.INITIALIZED || state == State.STOPPED) {
      ModuleContext ctx = new ModuleContextImpl();
      state = State.STARTING;
      try {
        for(ModuleRef module : moduleList) {
          log.debug("Starting module %s", module.module.getClass().getSimpleName());
          if(module.state == State.STOPPED) {
            module.init(ctx); 
          } 
          module.start(ctx);
        }
      } finally {
        state = State.STARTED;
      }
    }
  }
  
  /**
   * Stops this instance's modules.
   */
  public synchronized void stop() {
    if(state == State.STARTED || state == State.INITIALIZED) {
      state = State.STOPPING;
      try {
        for(int i = moduleList.size() - 1; i >= 0; i--) {
          ModuleRef module = moduleList.get(i);
          log.debug("Stopping module %s", module.module.getClass().getSimpleName());
          module.stop();
        }
      } finally {
        state = State.STOPPED;
      }
    }
  }
  
  /**
   * @return this instance's {@link State}.
   */
  public State getState() {
    return state;
  }
  
  /**
   * Registers an object as a MBean.
   * @param name the bean's {@link ObjectName}.
   * @param mbean the MBean {@link Object}
   */
  public void registerMbean(ObjectName name, Object mbean) {
    JmxHelper.registerMBean(name, mbean);
  }
  
  /**
   * @return <code>true</code> if this instance is started.
   */
  public boolean isStarted() {
    return this.state == State.STARTED;
  }
  
  // --------------------------------------------------------------------------
  
  // ModuleContext implementation
  public class ModuleContextImpl implements ModuleContext {
    
    @Override
    public <T> T lookup(Class<T> type) throws ModuleNotFoundException {
      Module module = modulesByName.get(type.getName());
      if(module == null) {
        throw new ModuleNotFoundException(type.getName());
      }
      return type.cast(module);
    }
    
    @Override
    public State getState() {
      return state;
    }
    
    @Override
    public void registerMbean(Class<?> toNameFrom, Object mbean) {
      ModuleContainer.this.registerMbean(JmxHelper.createObjectName(toNameFrom), mbean);
    }
    
    @Override
    public void registerMbean(Object mbean) {
      registerMbean(mbean.getClass(), mbean);
    }
  }
  
  private static class ModuleRef {
    State  state = State.CREATED;
    Module module;
    
    private ModuleRef(Module module) {
      this.module = module;
    }
    
    private void init(ModuleContext context) {
      if(state != State.CREATED && state != State.STOPPED && state != State.INITIALIZED) {
        throw new IllegalStateException(String.format("Cannot initialize at this time, current state is: %s", state));
      }
      if(state != State.INITIALIZED) {
        module.init(context);
      }
      state = State.INITIALIZED;
    }
    
    private void start(ModuleContext context) {
      if(state != State.INITIALIZED && state != State.STOPPED && state != State.STARTED) {
        throw new IllegalStateException(String.format("Cannot start at this time, current state is: %s", state));
      }
      if(state != State.STARTED) {
        module.start(context);
      }
      state = State.STARTED;      
    }    

    private void stop() {
      if(state != State.INITIALIZED && state != State.STARTED && state != State.STOPPED) {
        throw new IllegalStateException(String.format("Cannot stop at this time, current state is: %s", state));
      }
      if(state != State.STOPPED) {
        module.stop();
      }
      state = State.STOPPED;      
    }        
  }
}
