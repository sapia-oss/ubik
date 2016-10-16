package org.sapia.ubik.util;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Encapsulates list elements in {@link SoftReference}s. Automatically manages
 * <code>null</code> references by removing them from this instance's internal
 * list at iteration time.
 * <p>
 * This class is thread-safe.
 * 
 * @author yduchesne
 */
public class SoftReferenceList<T> implements Iterable<T> {

  private List<SoftReference<T>> delegate = new Vector<SoftReference<T>>();

  /**
   * @param item
   *          an item to add to this list.
   */
  public void add(T item) {
    synchronized (delegate) {
      delegate.add(new SoftReference<T>(item));
    }
  }

  /**
   * @param toRemove
   *          the item to remove from this list.
   * @return <code>true</code> if the given object was found and removed, or
   *         <code>false</code> if it is not contained in this instance and
   *         therefore could not be removed.
   */
  public boolean remove(T toRemove) {
    synchronized (delegate) {
      for (int i = 0; i < delegate.size(); i++) {
        SoftReference<T> itemRef = delegate.get(i);
        T item = itemRef.get();
        if (item != null && item.equals(toRemove)) {
          delegate.remove(i);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @return this instance's approximate size (calculated without checking for
   *         <code>null</code> soft references).
   */
  public int getApproximateSize() {
    return delegate.size();
  }

  /**
   * @param toCheck
   *          the object to check for containment in this instance.
   * @return <code>true</code> if the given object is contained in this
   *         instance.
   */
  public boolean contains(T toCheck) {
    synchronized (delegate) {
      for (int i = 0; i < delegate.size(); i++) {
        SoftReference<T> itemRef = delegate.get(i);
        T item = itemRef.get();
        if (item != null && item.equals(toCheck)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * An iterator over this instance's items.
   */
  @Override
  public Iterator<T> iterator() {
    return new SoftReferenceIterator(new ArrayList<SoftReference<T>>(delegate).iterator());
  }

  // ----------------------------------------------------------------------

  public class SoftReferenceIterator implements Iterator<T> {

    private Iterator<SoftReference<T>> delegateIterator;
    private T current;

    public SoftReferenceIterator(Iterator<SoftReference<T>> delegateIterator) {
      this.delegateIterator = delegateIterator;
    }

    public boolean hasNext() {

      if (current != null) {
        return true;
      }
      while (current == null && delegateIterator.hasNext()) {
        SoftReference<T> nextRef = delegateIterator.next();
        T next = nextRef.get();
        if (next == null) {
          delegate.remove(nextRef);
        }
        current = next;
      }
      return current != null;
    }

    @Override
    public T next() {
      if (current == null) {
        throw new IllegalStateException("End of iterator reached");
      }
      T next = current;
      current = null;
      return next;
    }

    @Override
    public void remove() {
    }

  }

}
