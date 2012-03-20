package org.sapia.ubik.util.pool;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;


/**
 * Implements a basic object pool. Pooled objects must be returned to the pool
 * once done with.
 *
 * @author Yanick Duchesne

 */
public abstract class Pool<T> {
  
  public static final long DEFAULT_ACQUIRE_TIME_OUT  = -1;
  public static final int  NO_MAX                    = 0;
  
  protected List<T>       objects               = new Vector<T>(50);
  protected volatile int  maxSize;
  protected AtomicInteger createdCount          = new AtomicInteger();
  protected volatile long lastUsageTime         = System.currentTimeMillis();
  protected volatile long defaultAcquireTimeOut = DEFAULT_ACQUIRE_TIME_OUT;
  private   Timer         acquireTime           = Stats.getInstance().createTimer(
                                                    getClass(), 
                                                    "AcquireTime", 
                                                    "Avg object acquisition time");
  
  public Pool() {
    this(NO_MAX);
  }
  
  public Pool(int maxSize) {
    this.maxSize = maxSize;
  }
  
  /**
   * Sets the default timeout that is internally respected when attempting to acquire an object
   * using the {@link #acquire()} method (defaults to {@link #DEFAULT_ACQUIRE_TIME_OUT}).
   * 
   * @param timeout a number of milliseconds.
   */
  public void setDefaultAcquireTimeout(long timeout)  {
    this.defaultAcquireTimeOut = timeout;
  }

  /**
   * Acquires an object from the pool; waits indefinitely that an
   * object becomes available if the pool is empty and its maximum
   * created object count has been reach. If the maximum number of objects has
   * not been reached, or if the pool has no such maximum defined, an object
   * is internally created and immediately returned.
   *
   * @see #acquire(long)
   */
  public synchronized T acquire() 
    throws InterruptedException, 
           NoObjectAvailableException, 
           PooledObjectCreationException {
    return acquire(defaultAcquireTimeOut);
  }

  /**
   * Acquires an object from this pool; if the pool is empty and its maximum
   * created object count has been reach, this method waits for the specified
   * timeout that an object becomes ready. If the maximum number of objects has
   * not been reached, or if the pool has no such maximum defined, an object
   * is internally created and immediately returned.
   *
   * @param timeout a timeout to wait for until an object becomes available (in millis).
   * @return an {@link Object}
   * @throws NoObjectAvailableException if an object could not be acquired within
   * the specified amount of time.
   * @throws Exception if a problem occurs creating the object.
   */
  public synchronized T acquire(long timeout)
    throws InterruptedException, 
    NoObjectAvailableException, 
    PooledObjectCreationException {
    
    acquireTime.start();
    lastUsageTime = System.currentTimeMillis();

    T obj;

    if (objects.size() == 0) {
      if (maxSize <= NO_MAX) {
        try {
          obj = newObject();
        } catch (Exception e) {
          throw new PooledObjectCreationException(e);
        }
      } else {
        long start = System.currentTimeMillis();
        
        while (objects.size() == 0) {
          if (timeout > 0) {
            wait(timeout);

            if ((System.currentTimeMillis() - start) > timeout) {
              break;
            }
          } else {
            if (createdCount.get() >= maxSize) {
              wait();
            } else {
              break;
            }
          }
        }

        if (objects.size() == 0) {
          if (createdCount.get() >= maxSize && maxSize > NO_MAX) {
            throw new NoObjectAvailableException();
          } else {
            try {
              obj = newObject();
            } catch (Exception e) {
              throw new PooledObjectCreationException(e);
            }
          }
        } else {
          obj = objects.remove(0);
        }
      }
    } else {
      obj = objects.remove(0);
    }

    acquireTime.end();
    try {
      return onAcquire(obj);
    } catch (Exception e) {
      throw new PooledObjectCreationException(e);
    }
  }

  /**
   * Releases the given object to the given pool.
   *
   * @param obj an object to put back into
   * the pool.
   */
  public synchronized void release(T obj) {
    objects.add(obj);
    onRelease(obj);
    notify();
  }

  /**
   * Returns the time an object was last acquired from this
   * pool.
   */
  public long getLastUsageTime() {
    return lastUsageTime;
  }

  /**
   * Returns the number of objects that have been created by this
   * pool so far.
   *
   * @return the number of created object.
   */
  public int getCreatedCount() {
    return createdCount.get();
  }
  
  /**
   * @return the number of available objects that are currently pooled.
   */
  public int getAvailableCount() {
    return objects.size();
  }
  
  /**
   * @return the number of objects that have been borrowed.
   */
  public int getBorrowedCount() {
    return createdCount.get() - objects.size();
  }

  /**
   * Shrinks the pool to the specified size, or until the pool is
   * empty. This method internally calls the <code>cleanup()</code>
   * method for each object in the pool, so that the cleaned objects
   * are properly disposed of.
   *
   * @param size the size to which to clean the pool.
   * @see #cleanup(Object)
   */
  public synchronized void shrinkTo(int size) {
    while ((objects.size() > size) && (objects.size() > 0)) {
      if(createdCount.decrementAndGet() < 0) {
        createdCount.set(0);
      }

      cleanup(objects.remove(0));
    }
  }
  
  /**
   * Clears all objects from this instance's internal list.
   * @see #cleanup(Object)
   */
  public synchronized void clear() {
    shrinkTo(0);
  }
  
  /**
   * This method internally passes the given object to {@link #cleanup(Object)} and decrements
   * this instance's created object count.
   * 
   * @param object an {@link Object} that was borrowed from this pooled, but is returned in
   * order to be disposed of.
   */
  public synchronized void invalidate(T object) {
    cleanup(object);
    createdCount.decrementAndGet();
  }
  
  /***
   * Fills the pool up to the given size, or up to this pool's
   * specified maximum size (if the latter was specified).
   *
   * @param toSize the size up to which this pool should be filled.
   */
  public synchronized void fill(int toSize) throws Exception {
    for (int i = 0; i < toSize; i++) {
      if ((maxSize > NO_MAX) && (objects.size() >= maxSize)) {
        break;
      }

      objects.add(newObject());
    }
  }

  /**
   * This method attempts to acquire an object from this pool. If
   * this pool is currently empty and its maximum number of created
   * objects has been reached, then <code>null</code> is returned. If this pool
   * is currently empty but no maximum number of created objects has been 
   * defined (at construction time), then a new object will be created and
   * returned.
   * 
   * @return an {@link Object}, or <code>null</code> if the pool is currently empty
   * and has reached the maximum number of objects it can create. 
   * @throws Exception if no object could be acquired/created.
   */
  public synchronized T acquireCreate() throws Exception{
    if(objects.size() == 0){
      if(getCreatedCount() >= maxSize && maxSize > NO_MAX){
        return null;
      }
      else{
        return acquire();
      }      
    }
    else{
      return acquire();
    }
  }

  /*////////////////////////////////////////////////////////////////////
                           RESTRICTED METHODS
  ////////////////////////////////////////////////////////////////////*/

  /**
   * This template method should be overridden by inheriting classes to
   * provide object instances that will be pooled.
   *
   * @return an {@link Object} to pool.
   * @throws Exception if an error occurs while creating the object to be
   * returned.
   */
  protected abstract T doNewObject() throws Exception;

  /**
   * Inheriting classes should override this method to implement proper
   * cleanup behavior for pooled objects. This method has an empty
   * implementation by default.
   *
   * @see #shrinkTo(int)
   */
  protected void cleanup(T pooled) {
  }
  
  protected T onAcquire(T o) throws Exception {
    return o;
  }

  protected void onRelease(T o) {
  }

  private T newObject() throws Exception {
    T toReturn = doNewObject();
    createdCount.incrementAndGet();

    return toReturn;
  }
}
