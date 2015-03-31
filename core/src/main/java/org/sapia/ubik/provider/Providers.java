package org.sapia.ubik.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.sapia.ubik.util.Conf;

/**
 * A singleton that allows registering/unregistering providers directly. The {@link #load(Class, String, Conf)} method
 * will use an internal {@link ReflectionLoader} if a provide is not found/has not been registered by calling
 * {@link #registerProvider(Class, String, Object)}.
 * 
 * @author yduchesne
 *
 */
public class Providers implements Loader {

  private class ProviderKey {
    
    private String interfaceClassName;
    private String hint;
    
    private ProviderKey(String interfaceClassName, String hint) {
      this.interfaceClassName = interfaceClassName;
      this.hint = hint;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ProviderKey) {
        ProviderKey other = (ProviderKey) obj;
        return interfaceClassName.equals(other.interfaceClassName) && hint.equals(other.hint);
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(interfaceClassName, hint);
    }
  }
  
  // ==========================================================================
  
  private static final String HINT_LEFACY_PREFIX = "ubik.rmi.naming.broadcast";
  private static final Providers INSTANCE        = new Providers();
  
  private Map<ProviderKey, Object> providers = new HashMap<Providers.ProviderKey, Object>();
  private ReflectionLoader         delegate  = new ReflectionLoader();
  
  private Providers() {
  }
  
  /**
   * @param interfaceClass the interface with which to register the given provider instance.
   * @param hint the hint with which to register the given provider instance.
   * @param impl a provider instance.
   */
  public synchronized <T> void registerProvider(Class<T> interfaceClass, String hint, T impl) {
    ProviderKey k = new ProviderKey(interfaceClass.getName(), hint);
    providers.put(k, impl);
  }

  /**
   * Unregisters all the providered that were registered using the {@link #registerProvider(Class, String, Object)} method.
   */
  public synchronized void unregisterAllProviders() {
    providers.clear();
  }

  
  @Override
  public synchronized <T> T load(Class<T> interfaceClass, String hint) {
    String realHint = hint;
    if (hint.startsWith(HINT_LEFACY_PREFIX) && hint.length() > HINT_LEFACY_PREFIX.length()) {
      realHint = hint.substring(HINT_LEFACY_PREFIX.length() + 1);
    }
    ProviderKey k = new ProviderKey(interfaceClass.getName(), realHint);
    Object provider = providers.get(k);
    if (provider == null) {
      return delegate.load(interfaceClass, realHint);
    }
    return interfaceClass.cast(provider);
  }
  
  /**
   * @return this class' singleton instance.
   */
  public static Providers get() {
    return INSTANCE;
  }
  
}

