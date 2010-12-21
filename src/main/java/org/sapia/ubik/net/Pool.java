package org.sapia.ubik.net;

import java.util.List;
import java.util.Vector;


/**
 * Implements a basic object pool. Pooled objects must be returned to the pool
 * once done with.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class Pool<T> {
  public static final long NO_TIME_OUT    = -1;
  public static final int NO_MAX = 0;
  protected List<T>        _objects = new Vector<T>(50);
  protected int            _maxSize = NO_MAX;
  protected int            _currentCount;
  protected long           _lastUsageTime = System.currentTimeMillis();

  public Pool() {
  }

  public Pool(int maxSize) {
    _maxSize = maxSize;
  }

  /**
   * Acquires an object from the pool; waits indefinitely that an
   * object becomes available if the pool is empty and its maximum
   * created object count has been reach. If the maximum number of objects has
   * not been reached, or if the pool has no such maximum defined, an object
   * is internally created and immediately returned.
   *
   * @return an <code>Object</code>
   * @throws an <code>Exception</code> if a problem occurs acquiring the object.
   */
  public synchronized T acquire() throws InterruptedException, Exception {
    return acquire(NO_TIME_OUT);
  }

  /**
   * Acquires an object from this pool; if the pool is empty and its maximum
   * created object count has been reach, this method waits for the specified
   * timeout that an object becomes ready. If the maximum number of objects has
   * not been reached, or if the pool has no such maximum defined, an object
   * is internally created and immediately returned.
   *
   * @param timeout a timeout to wait for until an object becomes available (in millis).
   * @return an <code>Object</code>
   * @throws NoObjectAvailableException if an object could not be acquired within
   * the specified amount of time.
   * @throws Exception if a problem occurs creating the object.
   */
  public synchronized T acquire(long timeout)
    throws InterruptedException, NoObjectAvailableException, Exception {
    _lastUsageTime = System.currentTimeMillis();

    T obj;

    if (_objects.size() == 0) {
      if (_maxSize <= NO_MAX) {
        obj = newObject();
      } else {
        long start = System.currentTimeMillis();

        while (_objects.size() == 0) {
          if (timeout > 0) {
            wait(timeout);

            if ((System.currentTimeMillis() - start) > timeout) {
              break;
            }
          } else {
            if (_currentCount > _maxSize) {
              wait();
            } else {
              break;
            }
          }
        }

        if (_objects.size() == 0) {
          if (_currentCount >= _maxSize) {
            throw new NoObjectAvailableException();
          } else {
            obj = newObject();
          }
        } else {
          obj = _objects.remove(0);
        }
      }
    } else {
      obj = _objects.remove(0);
    }

    return onAcquire(obj);
  }

  protected T onAcquire(T o) throws Exception {
    return o;
  }

  protected void onRelease(T o) {
  }

  /**
   * Releases the given object to the given pool.
   *
   * @param obj an object to put back into
   * the pool.
   */
  public synchronized void release(T obj) {
    _objects.add(obj);
    onRelease(obj);
    notify();
  }

  /**
   * Returns the time an object was last acquired from this
   * pool.
   */
  public long getLastUsageTime() {
    return _lastUsageTime;
  }

  /**
   * Returns the number of objects that have been created by this
   * pool so far.
   *
   * @return the number of created object.
   */
  public int getCreatedCount() {
    return _currentCount;
  }

  /**
   * Returns the number of objects currently in the pool.
   *
   * @return the number of objects currently in the pool.
   */
  public int size() {
    return _objects.size();
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
    while ((_objects.size() > size) && (_objects.size() > 0)) {
      _currentCount--;

      if (_currentCount < 0) {
        _currentCount = 0;
      }

      cleanup(_objects.remove(0));
    }
  }
  
  /***
   * Fills the pool up to the given size, or up to this pool's
   * specified maximum size (if the latter was specified).
   *
   * @param toSize the size up to which this pool should be filled.
   */
  public synchronized void fill(int toSize) throws Exception {
    for (int i = 0; i < toSize; i++) {
      if ((_maxSize > NO_MAX) && (_objects.size() >= _maxSize)) {
        break;
      }

      _objects.add(newObject());
    }
  }

  /**
   * This method attempts to acquire an object from this pool. If
   * this pool is currently empty and its maximum number of created
   * objects has been reached, then <code>null</code>. If this pool
   * is currently empty but no maximum number of created objects has been 
   * defined (at construction time), then a new object will be created and
   * returned.
   * 
   * @return an <code>Object</code>, or <code>null</code> if the pool is currently empty
   * and has reached the maximum number of objects it can create. 
   * @throws Exception if no object could be acquired/created.
   */
  public synchronized T acquireCreate() throws Exception{
    if(_objects.size() == 0){
      if(getCreatedCount() >= _maxSize && _maxSize > NO_MAX){
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
   * @return an <code>Object</code> to pool.
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
  protected void cleanup(Object pooled) {
  }

  private T newObject() throws Exception {
    T toReturn = doNewObject();
    _currentCount++;

    return toReturn;
  }
}
