package org.sapia.ubik.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
  
}
