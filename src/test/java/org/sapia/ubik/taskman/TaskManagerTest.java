package org.sapia.ubik.taskman;

import junit.framework.TestCase;

public class TaskManagerTest extends TestCase {

  public TaskManagerTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testProcessTasks() throws Exception{
    TaskManager tm = new MultithreadedTaskManager();
    TestTask tt = new TestTask();
    TestTask tt2 = new TestTask();
    long time = System.currentTimeMillis();
    tm.addTask(new TaskContext("Test1", 1000), tt);
    tm.addTask(new TaskContext("Test2", 2000), tt2);
    Thread.sleep(6000);
    tm.shutdown();
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
