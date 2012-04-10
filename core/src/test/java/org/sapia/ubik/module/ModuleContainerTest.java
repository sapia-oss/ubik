package org.sapia.ubik.module;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class ModuleContainerTest {
  
  ModuleContainer container;
  Module          module;

  @Before
  public void setUp() throws Exception {
    container = new ModuleContainer();
    module = mock(TestModule.class);
  }

  @Test
  public void testBindForClass() {
    container.bind(TestModule.class, module);
    container.lookup(TestModule.class);
  }
  
  @Test
  public void testBindForName() {
    container.bind("test", module);
    container.lookup(TestModule.class, "test");
  }  
  
  @Test(expected = ModuleAlreadyBoundException.class)
  public void testModuleAlreadyBound() {
    container.bind("test", module).bind("test", module);
  }  

  @Test
  public void testInit() {
    container.bind(TestModule.class, module).init();
    verify(module).init(any(ModuleContext.class));
  }
  
  @Test
  public void testStart() {
    container.bind(TestModule.class, module).init();
    container.start();
    verify(module).init(any(ModuleContext.class));    
    verify(module).start(any(ModuleContext.class));
  }

  @Test
  public void testStop() {
    container.bind(TestModule.class, module).init();
    container.start();
    container.stop();
    verify(module).init(any(ModuleContext.class));    
    verify(module).start(any(ModuleContext.class));
    verify(module).stop();
  }
  
  @Test
  public void testStopAfterInit() {
    container.bind(TestModule.class, module).init();
    container.stop();
    verify(module).stop();
  }
  
  @Test
  public void testInitAfterStop() {
    container.bind(TestModule.class, module).init();
    container.stop();
    container.init();
    verify(module).stop();
    verify(module, times(2)).init(any(ModuleContext.class));
  }    
  
  
  public interface TestModule extends Module{
    
  }

}
