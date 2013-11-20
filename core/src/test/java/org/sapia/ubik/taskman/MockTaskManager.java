package org.sapia.ubik.taskman;

import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;

public class MockTaskManager implements TaskManager, Module {

  @Override
  public void init(ModuleContext context) {
  }

  @Override
  public void start(ModuleContext context) {
  }

  @Override
  public void stop() {
  }

  @Override
  public void addTask(TaskContext ctx, Task task) {
  }

}
