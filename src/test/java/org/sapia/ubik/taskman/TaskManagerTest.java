package org.sapia.ubik.taskman;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.module.ModuleContext;

public class TaskManagerTest { 

  MultiThreadedTaskManager tm;

  @Before
  public void setUp() throws Exception {
    tm = new MultiThreadedTaskManager();
    ModuleContext ctx = mock(ModuleContext.class);
    tm.init(mock(ModuleContext.class));
    tm.start(ctx);
  }
  
  @After
  public void tearDown() throws Exception {
    tm.stop();
  }

  @Test
  public void testProcessTasks() throws Exception{
    TestTask tt = new TestTask();
    TestTask tt2 = new TestTask();
    long time = System.currentTimeMillis();
    tm.addTask(new TaskContext("Test1", 1000), tt);
    tm.addTask(new TaskContext("Test2", 2000), tt2);
    Thread.sleep(6000);
    tm.stop();
    assertTrue(tt.count > 3);
    assertTrue(tt.time > time+1000);
    assertTrue(tt2.count > 1);    
    
  }
  
  public static class TestTask implements Task{
    
    int count;
    long time;
    
    public void exec(TaskContext ctx) {
      if(count > 0){
        System.out.println(ctx.getName() + " executing ... last occurred " + (System.currentTimeMillis() - time) + " ms. ago");
      }
      else{
        System.out.println(ctx.getName() + " executing");  
      }
      time = System.currentTimeMillis();
      count ++;      
    }
    
  }

}
