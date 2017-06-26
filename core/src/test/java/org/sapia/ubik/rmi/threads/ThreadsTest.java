package org.sapia.ubik.rmi.threads;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ThreadsTest {

  @Mock
  private Runnable task;
  
  
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    Threads.shutdown();
    Threads.setJvmShuttown(false);
  }

  @Test
  public void testCreateWorkerPool_with_task_execution() {
    ExecutorService shared = Threads.createWorkerPool();
    waitForCompletion(shared.submit(task));
    verify(task).run();
  }
  
  @Test
  public void testShutdown_worker_pool() {
    ExecutorService global  = Threads.getGlobalWorkerPool();
    ExecutorService shared1 = Threads.createWorkerPool();
    ExecutorService shared2 = Threads.createWorkerPool();
    ExecutorService shared3 = Threads.createWorkerPool();
    
    shared1.shutdown();
    assertThat(global.isShutdown()).isFalse();
    
    shared2.shutdown();
    assertThat(global.isShutdown()).isFalse();
    
    shared3.shutdown();
    assertThat(global.isShutdown()).isTrue();
  }
  
  @Test
  public void testAccessAfterShutdown_worker_pool() {
    ExecutorService global  = Threads.getGlobalWorkerPool();
    ExecutorService shared  = Threads.createWorkerPool();
    
    shared.shutdown();
    assertThat(global.isShutdown()).isTrue();
    
    global = Threads.getGlobalWorkerPool();
    
    assertThat(global.isShutdown()).isFalse();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testGetGlobalWorkerPool_jvm_shutdown() {
    Threads.setJvmShuttown(true);
    Threads.getGlobalWorkerPool();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testGreateWorkerPool_jvm_shutdown() {
    Threads.setJvmShuttown(true);
    Threads.createWorkerPool();
  }

  @Test
  public void testCreateIoOutboundPool_with_task_execution() {
    ExecutorService shared = Threads.createIoOutboundPool();
    waitForCompletion(shared.submit(task));
    verify(task).run();
  }
  
  @Test
  public void testShutdown_outbound_pool() {
    ExecutorService global  = Threads.getGlobalIoOutboundPool();
    ExecutorService shared1 = Threads.createIoOutboundPool();
    ExecutorService shared2 = Threads.createIoOutboundPool();
    ExecutorService shared3 = Threads.createIoOutboundPool();

    shared1.shutdown();
    assertThat(global.isShutdown()).isFalse();
    
    shared2.shutdown();
    assertThat(global.isShutdown()).isFalse();
    
    shared3.shutdown();
    assertThat(global.isShutdown()).isTrue();
  }
  
  @Test
  public void testAccessAfterShutdown_outbound_pool() {
    ExecutorService global = Threads.getGlobalIoOutboundPool();
    ExecutorService shared = Threads.createIoOutboundPool();
    
    shared.shutdown();
    assertThat(global.isShutdown()).isTrue();
    
    global = Threads.getGlobalIoOutboundPool();
    
    assertThat(global.isShutdown()).isFalse();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testGetGlobalIoOutboundPool_jvm_shutdown() {
    Threads.setJvmShuttown(true);
    Threads.getGlobalIoOutboundPool();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateIoOutboundPool_jvm_shutdown() {
    Threads.setJvmShuttown(true);
    Threads.createIoOutboundPool();
  }

  @Test
  public void testCreateIoInboundPool() {
    ExecutorService pool = Threads.createIoInboundPool("test");
    
    waitForCompletion(pool.submit(task));
    
    pool.shutdown();
    
    assertThat(pool.isShutdown()).isTrue();
    verify(task).run();
  }

  @Test
  public void testCreateIoInboundPool_with_fixed_size() {
    ExecutorService pool = Threads.createIoInboundPool("test", 1);
    
    waitForCompletion(pool.submit(task));
    
    pool.shutdown();
    
    assertThat(pool.isShutdown()).isTrue();
    verify(task).run();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateIoInboundPool_jvm_shutdown() {
    Threads.setJvmShuttown(true);
    Threads.createIoInboundPool("test");
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateIoInboundPool_with_fixed_size_jvm_shutdown() {
    Threads.setJvmShuttown(true);
    Threads.createIoInboundPool("test", 1);
  }

  @Test
  public void testShutdown() {
    ExecutorService workers   = Threads.getGlobalWorkerPool();
    ExecutorService outbound  = Threads.getGlobalIoOutboundPool();

    Threads.shutdown();
    
    assertThat(workers.isShutdown()).isTrue();
    assertThat(outbound.isShutdown()).isTrue();
  }
  
  private void waitForCompletion(Future<?> future) {
    try {
      future.get();
    } catch (Exception e) {
      throw new IllegalStateException("Error waiting for task completion", e);
    }
  }
}
