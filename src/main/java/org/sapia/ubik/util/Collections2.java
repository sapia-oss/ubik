package org.sapia.ubik.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides various collection-related methods.
 * 
 * @author yduchesne
 *
 */
public final class Collections2 {
  
  private Collections2() {
  }
  
  /**
   * Traverses the elements of the given collection, applying the condition
   * to each element. Traversal stops as soon as the given condition returns <code>false</code>.
   * @param collection the {@link Iterable} over which to iterate.
   * @param condition the {@link Condition} to apply to each element.
   */
  public static <T> void forEach(Iterable<T> collection, Condition<T> condition) {
    for(T element : collection) {
      if(!condition.apply(element)) {
        break;
      }
    }
  }

  /**
   * Traverses the elements of the given array, applying the condition
   * to each element. Traversal stops as soon as the given condition returns <code>false</code>.
   * @param collection the array over which to iterate.
   * @param condition the {@link Condition} to apply to each element.
   */
  public static <T> void forEach(T[] array, Condition<T> condition) {
    for(T element : array) {
      if(!condition.apply(element)) {
        break;
      }
    }
  }
  
  /**
   * Converts the given collection, using the given converter to convert
   * that collection's values.
   * <p>
   * Whenever the given converter returns <code>null</code>, that return value is skipped 
   * (i.e.: it is not added to the resulting list of converted elements).
   * 
   * @param <R> the generic type to convert to.
   * @param <T> the generic type to convert from.
   * @param collection the {@link Iterable} to filter.
   * @param converter the {@link Function} to use as a converter.
   * @return the {@link List} resulting from the filtering.
   */
  public static <R, T> List<R> convertAsList(Iterable<T> collection, Function<R, T> converter) {
    List<R> converted = new ArrayList<R>();
    for(T element : collection) {
      R val = converter.call(element);
      if(val != null) {
        converted.add(val);
      }
    }
    return converted;
  }
  
  /**
   * Converts the given collection to a {@link Set}.
   * 
   * @see #convertAsList(Iterable, Function)
   */
  public static <R, T> Set<R> convertAsSet(Iterable<T> collection, Function<R, T> converter) {
    Set<R> converted = new HashSet<R>();
    for(T element : collection) {
      R val = converter.call(element);
      if(val != null) {
        converted.add(val);
      }
    }
    return converted;
  }
  
  /**
   * Internally creates a new {@link List} to which the given array's elements are copied.
   * The list is then returned.
   * 
   * @param array an array of elements.
   * @return the {@link List} into which the given array's elements have been copied.
   */
  public static <T> List<T> arrayToList(T...array) {
  	List<T> list = new ArrayList<T>(array.length);
  	for(T a : array) {
  		list.add(a);
  	}
  	return list;
  }  
  
  /**
   * Internally creates a new {@link Set} to which the given array's elements are copied.
   * The set is then returned.
   * 
   * @param array an array of elements.
   * @return the {@link Set} into which the given array's elements have been copied.
   */
  public static <T> Set<T> arrayToSet(T...array) {
  	Set<T> set = new HashSet<T>();
  	for(T a : array) {
  		set.add(a);
  	}
  	return set;
  }  
  
  /**
   * Converts the given {@link Collection} to an array. The collection is expected to contain non-null
   * elements. The length of the array that will be returned will match the given collection's size.
   * 
   * @param the {@link Collection} to convert.
   * @param the type of array.
   * @param the converter {@link Function}
   * 
   * @see #convertAsList(Iterable, Function)
   */
  @SuppressWarnings(value="unchecked")
  public static <R, T> R[] convertAsArray(Collection<T> collection, Class<R> arrayType, Function<R, T> converter) {
    
    R[] array = (R[]) Array.newInstance(arrayType, collection.size());
    int i = 0;
    for(T element : collection) {
      array[i++] = converter.call(element);
    }
    return array;
  }
  
  /**
   * Removes an element from the set and returns it.
   * 
   * @param elements a {@link Set}.
   * @return the element that's been removed from the set.
   */
  public static <T>  T removeAndGet(Set<T> elements) {
  	Iterator<T> itr = elements.iterator();
  	if(itr.hasNext()) {
  		T element = itr.next();
  		elements.remove(element);
  		return element;
  	}
  	throw new IllegalStateException("Set is empty, cannot remove element");
  }
  
  /**
   * @param toSplit the Collection to split.
   * @param batchSize the size of the batches into which the given collection should be split.
   * @return a {@link List} of batches (each batch itself being represented as a {@link List}).
   */
  public static <T> List<List<T>> splitAsLists(Collection<T> toSplit, int batchSize) {
  	
  	int count = 0;
  	List<List<T>> aggregate = new ArrayList<List<T>>();
  	
  	List<T> batch = new ArrayList<T>(batchSize);
  	for(T item : toSplit) {
  		if(count >= batchSize) {
  			aggregate.add(batch);
  			batch = new ArrayList<T>(batchSize);
  			count = 0;
  		}
  		batch.add(item);
  		count++;
  	}
  	
  	if(batch.size() > 0) {
  		aggregate.add(batch);
  	}

  	return aggregate;
  }
  
  /**
   * @param toSplit the Collection to split.
   * @param batchSize the size of the batches into which the given collection should be split.
   * @return a {@link List} of batches (each batch itself being represented as a {@link Set}).
   */
  public static <T> List<Set<T>> splitAsSets(Collection<T> toSplit, int batchSize) {
  	int count = 0;
  	List<Set<T>> aggregate = new ArrayList<Set<T>>();
  	
  	Set<T> batch = new HashSet<T>(batchSize);
  	for(T item : toSplit) {
  		if(count >= batchSize) {
  			aggregate.add(batch);
  			batch = new HashSet<T>(batchSize);
  			count = 0;
  		}
  		batch.add(item);
  		count++;
  	}
  	
  	if(batch.size() > 0) {
  		aggregate.add(batch);
  	}

  	return aggregate;
  }
  
}
