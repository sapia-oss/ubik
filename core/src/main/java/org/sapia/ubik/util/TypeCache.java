package org.sapia.ubik.util;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.server.Stateless;
import org.sapia.ubik.rmi.server.stub.Stub;

/**
 * An instance of this class caches interfaces and annotations for given
 * classes/interfaces.
 *
 * @author yduchesne
 *
 */
public class TypeCache {

  private Category log = Log.createCategory(getClass());
  private Map<Class<?>, Set<Class<?>>> interfaceCache = new ConcurrentHashMap<Class<?>, Set<Class<?>>>();
  private Map<Class<?>, Set<Class<?>>> annotationCache = new ConcurrentHashMap<Class<?>, Set<Class<?>>>();

  /**
   * Clears this instance's entries.
   *
   */
  public void clear() {
    interfaceCache.clear();
    annotationCache.clear();
  }

  /**
   * Clears the entries whose class has the given {@link ClassLoader} in its
   * hierarchy.
   *
   * @param loader
   */
  public void clearFor(final ClassLoader loader) {
    Collects.forEach(interfaceCache.keySet(), new Condition<Class<?>>() {
      @Override
      public boolean apply(java.lang.Class<?> item) {
        if (hasClassloader(item, loader)) {
          interfaceCache.remove(item);
        }
        return true;
      }
    });
    Collects.forEach(annotationCache.keySet(), new Condition<Class<?>>() {
      @Override
      public boolean apply(java.lang.Class<?> item) {
        if (hasClassloader(item, loader)) {
          annotationCache.remove(item);
        }
        return true;
      }
    });
  }

  // --------------------------------------------------------------------------
  // Interfaces

  /**
   * Returns the interfaces that the given class/interface implements/extends.
   * Caches the resulting set, using the given class as a key.
   *
   * @param clazz
   *          the {@link Class} instance corresponding to the class or interface
   *          whose implemented/inherited interfaces should be returned.
   * @return a {@link Set} of {@link Class} instances corresponding to
   *         interfaces.
   */
  public Set<Class<?>> getInterfacesFor(Class<?> clazz) {
    log.trace("Getting interfaces for %s", clazz);
    Set<Class<?>> cachedInterfaces = interfaceCache.get(clazz);

    if (cachedInterfaces == null) {
      log.trace("No cached interfaces for %s. Processing...", clazz);

      Remote remoteAnno = clazz.getAnnotation(Remote.class);
      if (remoteAnno != null) {
        log.trace("@Remote specicied for %s", clazz);
        Class<?>[] remoteInterfaces = remoteAnno.interfaces();
        HashSet<Class<?>> set = new HashSet<Class<?>>();
        for (Class<?> remoteInterface : remoteInterfaces) {
          set.add(remoteInterface);
        }
        log.trace("Specified remote interfaces for %s: %s", clazz, set);
        set.add(Stub.class);
        if (Stateless.class.isAssignableFrom(clazz)) {
          log.trace("Class %s implements Stateless", clazz);
          set.add(Stateless.class);
        }
        cachedInterfaces = Collections.unmodifiableSet(set);
      } else {
        log.trace("@Remote not specicied for %s. Collecting interfaces through reflection", clazz);

        HashSet<Class<?>> set = new HashSet<Class<?>>();
        collectInterfaces(clazz, set);
        log.trace("Collected class %s interfaces: %s", clazz, set);
        set.add(Stub.class);
        if (Stateless.class.isAssignableFrom(clazz)) {
          log.trace("%s implements Stateless", clazz);
          set.add(Stateless.class);
        }
        cachedInterfaces = Collections.unmodifiableSet(set);
      }
      interfaceCache.put(clazz, cachedInterfaces);
    }
    log.trace("Got interfaces for %s: %s", clazz, cachedInterfaces);
    return cachedInterfaces;
  }

  /**
   * @see #getInterfacesFor(Class)
   */
  public Class<?>[] getInterfaceArrayFor(Class<?> clazz) {
    Set<Class<?>> cachedInterfaces = getInterfacesFor(clazz);
    return cachedInterfaces.toArray(new Class<?>[cachedInterfaces.size()]);
  }

  /**
   * This method collects the interfaces for the given class or interface.
   *
   * @param current
   *          the class whose interfaces should be collected.
   * @param collector
   *          the {@link Set} to which the collected interfaces are added.
   */
  public void collectInterfaces(Class<?> current, Set<Class<?>> collector) {
    Class<?>[] ifs = current.getInterfaces();

    for (int i = 0; i < ifs.length; i++) {
      collectInterfaces(ifs[i], collector);
      collector.add(ifs[i]);
    }

    current = current.getSuperclass();

    if (current != null) {
      collectInterfaces(current, collector);
    }
  }

  // --------------------------------------------------------------------------
  // Annotations

  /**
   * This method collects the interfaces for the given class or interface, and
   * caches the resulting set, using the given class as a key.
   *
   * @param current
   *          the class/interface whose annotations should be collected.
   * @param collector
   *          the {@link Set} to which the collected annotations are added.
   */
  public Set<Class<?>> getAnnotationsFor(Class<?> clazz) {
    Set<Class<?>> annotations = this.annotationCache.get(clazz);
    if (annotations == null) {
      annotations = new HashSet<Class<?>>();
      collectAnnotationsFor(clazz, annotations);
      annotationCache.put(clazz, Collections.unmodifiableSet(annotations));
    }
    return annotations;
  }

  /**
   * Returns the annotations that the given class/interface implements/extends.
   * Caches the resulting set, using the given class as a key.
   *
   * @param clazz
   *          the {@link Class} instance corresponding to the class or interface
   *          whose annotations should be returned.
   * @return a {@link Set} of {@link Annotation}s.
   */
  public void collectAnnotationsFor(Class<?> clazz, Set<Class<?>> collector) {
    Class<?> current = clazz;
    do {
      for (Annotation anno : current.getAnnotations()) {
        collector.add(anno.annotationType());
      }
      for (Class<?> intf : current.getInterfaces()) {
        collectAnnotationsFor(intf, collector);
      }
    } while ((current = current.getSuperclass()) != null && !current.equals(Object.class));
  }

  // --------------------------------------------------------------------------
  // Misc private methods

  private boolean hasClassloader(Class<?> clazz, ClassLoader loader) {
    ClassLoader current = loader;
    do {
      if (clazz.getClassLoader().equals(current)) {
        return true;
      }
      current = loader.getParent();
    } while (current != null);
    return false;
  }
}
