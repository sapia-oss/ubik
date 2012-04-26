package org.sapia.ubik.module;

public class TestModuleContext implements ModuleContext {
  
  private State           state = State.STARTED;
  private ModuleContainer container = new ModuleContainer();
  
  public ModuleContainer getContainer() {
    return container;
  }
  
  @Override
  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }
    
  @Override
  public <T> T lookup(Class<T> type) throws ModuleNotFoundException {
    return container.lookup(type);
  }
  
  @Override
  public void registerMbean(Class<?> toNameFrom, Object mbean) {
  }

  @Override
  public void registerMbean(Object mbean) {
  }
}
