package org.sapia.ubik.module;

public class TestModuleContext implements ModuleContext {
  
  private ModuleContainer container = new ModuleContainer();
  
  public ModuleContainer getContainer() {
    return container;
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
