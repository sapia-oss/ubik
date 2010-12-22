package org.sapia.ubik.util;

/**
 * An instance of this class is used to synchronized start/stop operations. It is common for such operations
 * to be wholly or in part performed in separate threads. An instance of this class can be used to
 * synchronized threads that invoke start or stop operations, and those that actually perform these
 * operations.
 * <p>
 * USAGE:
 * <p>
 * Suppose we are implementing a server that can be embedded. The server itself will run in a
 * separate thread. The application that will use the server will be responsible for starting and
 * stopping it.
 * <p>
 * First, our server thread implementation:
 * <pre>
 * public class ServerThread extends Thread{
 *   private StartStopLock _lock
 *   
 *   public ServerThread(StartStopLock lock){
 *     _lock = lock;
 *   }
 *   
 *   public void run(){
 *   
 *     boolean started = false;
 *     while(!_lock.stopRequested && !interrupted()){
 *       if(!started){
 *        _lock.notifyStarted(null);
 *        started = true;
 *       }
 *       // do work...
 *     }
 *     
 *     // here do shutdown stuff...
 *     
 *     _lock.notifyStopped(null);
 *   }
 * }
 * </pre>
 * <p>
 * Third, we create the actual server class that will be used by application code:
 * <pre>
 * public class Server{
 *   private StartStopLock _lock = new StartStopLock();
 *   private ServerThread _thread;
 *   
 *   public void start() throws Exception{
 *     _thread = new ServerThread(_lock);
 *     _thread.start();
 *     _lock.waitStarted();
 *   }
 *   
 *   public void stop() throws Exception{
 *     _thread.interrupt();
 *     _lock.triggerStopped();
 *     _lock.waitStopped();
 *   }
 * }
 * </pre>
 * <p>
 * 
 * 
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class StartStopLock {
  
  public boolean started, stopped, stopRequested;
  
  private Throwable _startErr, _stopErr;
  
  private StopRequestListener _listener;
  
  
  /**
   * @param listener a <code>StopRequestListener</code> that is called
   * upon this instance's <code>triggerStop</code> method being called.
   * 
   * @see StopRequestListener
   */
  public StartStopLock(StopRequestListener listener){
    _listener = listener;
  }

  public StartStopLock(){
  }  
  
  /**
   * @return <code>true</code> if the <code>started</code> flag is on.
   */
  public boolean isStarted(){
    return started;
  }

  /**
   * @return <code>true</code> if the <code>stopped</code> flag is on.
   */  
  public boolean isStopped(){
    return stopped;
  }
  
  /**
   * @return <code>true</code> if the <code>stopRequested</code> flag is on.
   */  
  public boolean isStopRequested(){
    return stopRequested;
  }
  
  /**
   * This method internally sets the <code>stopRequested</code> flag
   * to <code>true</code>, calls this instance's encapsulated 
   * <code>StopRequestListener</code>, and notifies threads that are
   * currently blocking on the <code>waitStopped()</code> method.
   */
  public synchronized void triggerStop(){
    stopRequested = true;
    
    if(_listener != null){
      try{
        _listener.onStopRequested();
      }catch(Throwable t){
        notifyStopped(t);
        return;
      }
    }
    notifyStopped(null);
  }
  
  /**
   * Blocks until this instance's <code>started</code> flag is set to <code>true</code>,
   * or until a startup error is signaled. If the given timeout has elapsed, this method exits.
   * 
   * @see #notifyStarted(Throwable)
   * 
   * @param timeout a given amount of time to wait for this instance's
   *  <code>started</code> flag to be <code>true</code>.
   * @throws InterruptedException if the calling thread is interrupted while blocking.
   * @throws Throwable if an error occurs at startup.
   */
  public synchronized void waitStarted(long timeout) throws InterruptedException, Throwable{
    while(!started && _startErr == null){
      wait(timeout);
    }
    if(_startErr != null)
      throw _startErr;    
  }
  
  /**
   * Blocks until this instance's <code>started</code> flag is set to <code>true</code>,
   * or until a startup error is signaled.
   * 
   * @see #notifyStarted(Throwable)
   * 
   * @throws InterruptedException if the calling thread is interrupted while blocking.
   * @throws Throwable if an error occurs at startup.
   */
  public synchronized void waitStarted() throws InterruptedException, Throwable{
    while(!started && _startErr == null){
      wait();
    }
    if(_startErr != null)
      throw _startErr;    
  }

  /**
   * Blocks until this instance's <code>stopped</code> flag is set to <code>true</code>,
   * or until a startup error is signaled. If the given timeout has elapsed, this method exits.
   * 
   * @see #notifyStopped(Throwable)
   * 
   * @param timeout a given amount of time to wait for this instance's
   *  <code>stopped</code> flag to be <code>true</code>.
   * @throws InterruptedException if the calling thread is interrupted while blocking.
   * @throws Throwable if an error occurs while stopping.
   */  
  public synchronized void waitStopped(long timeout) throws InterruptedException, Throwable{
    while(!stopped && _stopErr == null){
      wait(timeout);
    }    
    if(_stopErr != null)
      throw _stopErr;
  }    
  
  /**
   * Blocks until this instance's <code>stopped</code> flag is set to <code>true</code>,
   * or until a startup error is signaled.
   * 
   * @see #notifyStopped(Throwable)
   * 
   * @throws InterruptedException if the calling thread is interrupted while blocking.
   * @throws Throwable if an error occurs while stopping.
   */  
  public synchronized void waitStopped() throws InterruptedException, Throwable{
    while(!stopped && _stopErr == null){
      wait();
    }    
    if(_stopErr != null)
      throw _stopErr;    
  }
  
  /**
   * @param err a <code>Throwable</code>
   */
  public synchronized void notifyStarted(Throwable err){
    _startErr = err;
    if(err == null){
      started = true;
    }
    notifyAll();
  }
  
  /**
   * @param err a <code>Throwable</code>
   */
  public synchronized void notifyStopped(Throwable err){
    _stopErr = err;
    if(err == null){
      stopped = true;
    }
    notifyAll();
  } 
  
  public interface StopRequestListener{
      public void onStopRequested() throws Throwable;
  }

}
