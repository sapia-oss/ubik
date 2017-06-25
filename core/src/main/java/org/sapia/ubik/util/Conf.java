package org.sapia.ubik.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class provides utility methods over a list of {@link Properties} or
 * {@link Map} objects that are sequentially looked up for given values.
 *
 * @author Yanick Duchesne
 *
 */
public class Conf {

  /**
   * The {@link List} of {@link PropertyLookup} instances to look up.
   */
  private List<PropertyLookup> props = new ArrayList<PropertyLookup>();

  /**
   * @param props
   *          some {@link Properties} to look up.
   * @return this instance.
   */
  public Conf addProperties(Properties props) {
    this.props.add(new PropertiesPropertyLookup(props));
    return this;
  }
  
  /**
   * @param nameValues one or more name/value pairs.
   * @return this instance.
   */
  public Conf addProperties(String...nameValues) {
    Properties props = new Properties();
    Assertions.isTrue(nameValues.length % 2 == 0, "Properties should consist of array of name-value pairs "
        + " of format [name-0,value-0,name-1,value-1,...,name-N,value-N");
    for (int i = 0; i < nameValues.length; i+=2) {
      props.setProperty(nameValues[i], nameValues[i + 1]);
    }
    return addProperties(props);
  }

  /**
   * @param props
   *          some property {@link Map} to look up.
   * @return this instance.
   */
  public Conf addMap(Map<String, String> props) {
    this.props.add(new MapPropertyLookup(props));
    return this;
  }

  /**
   * Adds the system properties to this instance.
   *
   * @return this instance.
   */
  public Conf addSystemProperties() {
    return addProperties(System.getProperties());
  }

  /**
   * @param lookup
   *          a {@link PropertyLookup} instance.
   * @return this instance.
   */
  public Conf addPropertyLookup(PropertyLookup lookup) {
    props.add(lookup);
    return this;
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the property value.
   * @throws NumberFormatException
   *           if the value could not be converted to an integer.
   * @throws IllegalArgumentException
   *           if no property exists for the given key.
   */
  public int getIntProperty(String key) throws NumberFormatException, IllegalArgumentException {
    String val = lookup(key, true);
    return Integer.parseInt(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @param defaultValue
   *          the default value that must be returned if not property was found
   *          for the given key.
   * @return the property value.
   * @throws NumberFormatException
   *           if the value could not be converted to an integer.
   */
  public int getIntProperty(String key, int defaultValue) throws NumberFormatException {
    String val = lookup(key, false);
    if (val == null) {
      return defaultValue;
    }
    return Integer.parseInt(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the property value.
   * @throws NumberFormatException
   *           if the value could not be converted to a long.
   * @throws IllegalArgumentException
   *           if no property exists for the given key.
   */
  public long getLongProperty(String key) throws NumberFormatException, IllegalArgumentException {
    String val = lookup(key, true);
    return Long.parseLong(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @param defaultValue
   *          the default value that must be returned if not property was found
   *          for the given key.
   * @return the property value.
   * @throws NumberFormatException
   *           if the value could not be converted to a long.
   */
  public long getLongProperty(String key, long defaultValue) throws NumberFormatException {
    String val = lookup(key, false);
    if (val == null) {
      return defaultValue;
    }
    return Long.parseLong(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the property value.
   * @throws NumberFormatException
   *           if the value could not be converted to a float.
   * @throws IllegalArgumentException
   *           if no property exists for the given key.
   */
  public float getFloatProperty(String key) throws NumberFormatException, IllegalArgumentException {
    String val = lookup(key, true);
    return Float.parseFloat(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @param defaultValue
   *          the default value that must be returned if not property was found
   *          for the given key.
   * @return the property value.
   * @throws NumberFormatException
   *           if the value could not be converted to a float.
   */
  public float getFloatProperty(String key, float defaultValue) throws NumberFormatException {
    String val = lookup(key, false);
    if (val == null) {
      return defaultValue;
    }
    return Float.parseFloat(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return <code>true</code> if the value corresponding to the given key
   *         equals <code>true</code>, <code>on</code>, or <code>yes</code>,
   *         <code>false</code> otherwise, or if no value could be found for the
   *         given key.
   */
  public boolean getBooleanProperty(String key) {
    String val = lookup(key, false);
    if (val == null) {
      return false;
    }
    val = val.toLowerCase();
    return val.equals("true") || val.equals("on") || val.equals("yes");
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return <code>true</code> if the value corresponding to the given key
   *         equals <code>true</code>, <code>on</code>, or <code>yes</code>,
   *         <code>false</code> otherwise, or the passed in default if no value
   *         could be found for the given key.
   */
  public boolean getBooleanProperty(String key, boolean defaultValue) {
    String val = lookup(key, false);
    if (val == null) {
      return defaultValue;
    }
    val = val.toLowerCase();
    return val.equals("true") || val.equals("on") || val.equals("yes");
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the {@link TimeValue} corresponding to the retrieved property
   *         value (which is expected to be a time literal.
   * @throws IllegalArgumentException not value could be found for the
   *         the desired property.
   */
  public TimeValue getTimeProperty(String key) throws IllegalArgumentException {
    String val = lookup(key, false);
    if (val == null) {
      throw new IllegalArgumentException("No time specified for property: " + key);
    }
    val = val.toLowerCase();
    return TimeValue.valueOf(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the {@link TimeRange} corresponding to the property value, which is
   *         expected to be a time range literal.
   */
  public TimeRange getTimeRangeProperty(String key, TimeRange defaultValue) {
    String val = lookup(key, false);
    if (val == null) {
      return defaultValue;
    }
    val = val.toLowerCase();
    return TimeRange.valueOf(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the {@link TimeRange} corresponding to the retrieved property
   *         value (which is expected to be a time range literal.
   * @throws IllegalArgumentException not value could be found for the
   *         the desired property.
   */
  public TimeRange getTimeRangeProperty(String key) throws IllegalArgumentException {
    String val = lookup(key, false);
    if (val == null) {
      throw new IllegalArgumentException("No time specified for property: " + key);
    }
    val = val.toLowerCase();
    return TimeRange.valueOf(val);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the {@link TimeValue} corresponding to the property value, which is
   *         expected to be a time literal.
   */
  public TimeValue getTimeProperty(String key, TimeValue defaultValue) {
    String val = lookup(key, false);
    if (val == null) {
      return defaultValue;
    }
    val = val.toLowerCase();
    return TimeValue.valueOf(val, defaultValue.getUnit());
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the value corresponding to the given key, or <code>null</code> if
   *         no such value exists.
   */
  public String getProperty(String key) {
    return lookup(key, false);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the value corresponding to the given key.
   * @throws IllegalArgumentException
   *           of a value could not be found.
   */
  public String getNotNullProperty(String key) {
    return lookup(key, true);
  }

  /**
   * @param key
   *          the key of the desired property.
   * @return the value corresponding to the given key, or the given default
   *         value of the desired property was not found.
   */
  public String getProperty(String key, String defaultValue) {
    String val = lookup(key, false);
    if (val == null) {
      return defaultValue;
    }
    return val;
  }

  /**
   * @param key
   *          the key of the property whose value is expected to be a class
   *          name.
   * @return the {@link Class} that was loaded from the class name specified by
   *         the given property value.
   * @throws ClassNotFoundException
   *           thrown when the given class could not be found.
   */
  public Class<?> getClass(String key) throws ClassNotFoundException {
    return Class.forName(lookup(key, true));
  }

  /**
   * @param key
   *          the key of the property whose value is expected to be a class
   *          name.
   * @param defaultClass
   *          the {@link Class} to return if not class name was found for the
   *          given property key.
   * @return the {@link Class} that was loaded from the class name specified by
   *         the given property value, or the specified default {@link Class} if
   *         no class name could be found matching the given key.
   * @throws ClassNotFoundException
   *           thrown when the given class could not be found.
   */
  public Class<?> getClass(String key, Class<?> defaultClass) throws ClassNotFoundException {
    String className = lookup(key, false);
    if (className == null) {
      return defaultClass;
    }
    return Class.forName(className);
  }
 
  /**
   * @return the {@link Set} of property names corresponding to the properties held by this instance.
   */
  public Set<String> propertyNames() {
    Set<String> toReturn = new HashSet<String>();
    for (PropertyLookup p : props) {
      toReturn.addAll(p.propertyNames());
    }
    return toReturn;
  }

  /**
   * @return an instance of this class encapsulating system {@link Properties}.
   */
  public static Conf getSystemProperties() {
    return new Conf().addProperties(System.getProperties());
  }

  /**
   * @return a new instance of this class.
   */
  public static Conf newInstance() {
    return new Conf();
  }

  private String lookup(String key, boolean throwExcIfNotFound) {
    PropertyLookup current;
    String val;
    for (int i = 0; i < props.size(); i++) {
      current = props.get(i);
      val = current.getProperty(key);
      if (val != null) {
        return val;
      }
    }
    if (throwExcIfNotFound) {
      throw new IllegalArgumentException("No value found for property: " + key);
    }
    return null;
  }

  // ==========================================================================

  /**
   * This interface property lookup behavior.
   */
  public interface PropertyLookup {

    /**
     * @param name
     *          a property name.
     * @return the value corresponding to the given name, or <code>null</code>
     *         if no such value was found.
     */
    public String getProperty(String name);
    
    /**
     * @return the {@link Set} of property names corresponding to the properties
     * encapsulated by this instance.
     */
    public Set<String> propertyNames();

  }

  // --------------------------------------------------------------------------

  static class MapPropertyLookup implements PropertyLookup {

    private Map<String, String> map;

    public MapPropertyLookup(Map<String, String> map) {
      this.map = map;
    }

    @Override
    public String getProperty(String name) {
      return map.get(name);
    }
    
    @Override
    public Set<String> propertyNames() {
      return Collections.unmodifiableSet(map.keySet());
    }
  }

  // --------------------------------------------------------------------------

  static class PropertiesPropertyLookup implements PropertyLookup {

    private Properties props;

    public PropertiesPropertyLookup(Properties props) {
      this.props = props;
    }

    @Override
    public String getProperty(String name) {
      return props.getProperty(name);
    }
    
    @Override
    public Set<String> propertyNames() {
      return Collections.unmodifiableSet(props.stringPropertyNames());
    }
  }

}
