package org.sapia.ubik.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;

/**
 * This {@link Loader} implementation creates provider instances based on Java's reflection
 * capabilities. Provider classes are expected to have an empty, no-args constructor.
 * <p>
 * Given the interface provided to the {@link #load(Class, String)} method, an instance
 * of this class will lookup a corresponding properties file in the classpath. The properties'
 * file name is expected to have the following path:
 * <p>
 * <pre>&lt;interface_qualified_name&gt;-&lt;interface_qualified_name&gt;.properties</pre>
 * <p>
 * The periods in the interface's qualified name are replaced by file separators. Similarly, file separator
 * characters (/) in the hint itself are replaced by dashes (-). 
 * 
 * That is to say, for given interface <code>com.foo.Bar</code> and hint <code>my/hint</code>, the path that will
 * be search will correspond to <code>com/foo/Bar-my-hint.properties</code>.
 * <p>
 * 
 * @author yduchesne
 *
 */
public class ReflectionLoader implements Loader {

  private Category log = Log.createCategory(getClass());
  
  private static final String IMPLEMENTATION_PROPERTY = "implementation";
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> T load(Class<T> interfaceClass, String hint) {
    String resourcePath = interfaceClass.getName().replace(".", "/") + "-" + hint.replace("/", "-") + ".properties";
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    if (is == null) {
      is = getClass().getClassLoader().getResourceAsStream(resourcePath);
    }
    if (is == null) {
      throw new IllegalArgumentException("Could not find resource: " + resourcePath);
    }
    String implClassName = null;
    try {
      Properties props = new Properties();
      props.load(is);
      implClassName = props.getProperty(IMPLEMENTATION_PROPERTY);
      if (implClassName == null) {
        throw new IllegalStateException("Property '" + IMPLEMENTATION_PROPERTY + "' not specified in " + resourcePath);
      }
      Class<T> implClass = Class.class.cast(Class.forName(implClassName));
      log.debug("Got implementation class %s for interface %s, hint %s", implClassName, interfaceClass, hint);
      Constructor<T> emptyArgsCtor = implClass.getDeclaredConstructor(new Class<?>[]{});
      T provider = emptyArgsCtor.newInstance(new Object[]{});
      log.debug("Instantiated provider %s for interface %s, hint %s", implClassName, interfaceClass, hint);
      return provider;
    } catch (IOException e) {
      throw new IllegalStateException("Could not load resource: " + resourcePath, e);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Implementation class not found for interface " + interfaceClass.getName(), e);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Could not find public no-args constructor ", e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Could not instantiate " + implClassName, e.getTargetException());
    } catch (InstantiationException e) {
      throw new IllegalStateException("Could not instantiate " + implClassName, e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Could not instantiate " + implClassName + " - should have public, no-args contructor", e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        // noop
      }
    }
  }
}
