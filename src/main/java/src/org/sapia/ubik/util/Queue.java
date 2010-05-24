package org.sapia.ubik.util;

import java.util.LinkedList;

/**
 * A basic FIFO datastructure.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Queue {
  protected LinkedList _items = new LinkedList();
  private boolean _added;
  
  /**
   * Creates an instance of this class with no maximum size.
   */
  public Queue(){
  }
  
  /**
   * @param o the <code>Object</code> to add to this queue.
   * @param notifyAll if <code>true</code>, all threads waiting on this queue will be
   * internally notified.
   */
  public synchronized void add(Object o, boolean notifyAll){
    
    _items.add(o);
    _added = true;
    if(notifyAll){
      notifyAll();
    }
    else{
      notify();
    }
  }
  
  /**
   * This method retrusn the first object in this queue, or blocks until an object is added.
   * 
   * @return the first <code>Object</code> in the queue - which is internally removed. 
   * @throws InterruptedException
   */
  public synchronized Object remove() throws InterruptedException{
    while(_items.size() == 0){
      wait();
    }
    _added = false;
    return _items.removeFirst();
  }
  
  /**
   * @return the number of items in this queue.
   */
  public int size(){
    return _items.size();
  }
  
  /**
   * @return <code>true</code> if an item was just added to this instance.
   */
  public boolean wasItemAdded(){
    return _added;
  }
  
  protected void resetAddedFlag(){
    _added = false;
  }
}
