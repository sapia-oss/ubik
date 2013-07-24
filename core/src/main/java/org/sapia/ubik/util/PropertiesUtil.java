package org.sapia.ubik.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Properties-related utility class.
 * 
 * @author yduchesne
 *
 */
public final class PropertiesUtil {
  
  /**
   * Holds a property name and value.
   * 
   * @author yduchesne
   *
   */
  public static class Property {
    
    private String name, value;
    
    /**
     * @param name a property name.
     * @param value a property value.
     */
    public Property(String name, String value) {
      this.name = name;
      this.value = value;
    }
    
    /**
     * @return the name of the property to which this instance corresponds.
     */
    public String getName() {
      return name;
    }
    
    /**
     * @return this instance's value.
     */
    public String getValue() {
      return value;
    }
  }
  
  // --------------------------------------------------------------------------
	
	private PropertiesUtil() {
  }
	
  /**
   * @param props the {@link Properties} into which the properties contained in the given file should be loaded.
   * @param file a {@link File} to load properties from. 
   * @throws IOException if an IO problem occurs.
   */
  public static void loadIntoPropertiesFrom(Properties props, File file) throws IOException {
		FileInputStream is    = null;
		try {
			is = new FileInputStream(file);
			props.load(new BufferedInputStream(is));
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// noop
				}
			}
		}  	
  }

	/**
	 * Copies the content of a {@link Properties} instance into another.
	 * 
	 * @param from the {@link Properties} instance to copy from.
	 * @param to the {@link Properties} instance to copy to.
	 */
	public static void copy(Properties from, Properties to) {
		for (String name : from.stringPropertyNames()) {
			String value = from.getProperty(name);
			if (value != null) {
				to.setProperty(name, value);
			}
		}
	}
	
	/**
	 * @param toClear the {@link Properties} to clear.
	 * @param condition the {@link Condition} upon which a given property will be cleared, if that
	 * condition is <code>true</code>.
	 */
	public static void clearProperties(Properties toClear, Condition<Property> condition) {
	  Set<String> toRemove = new HashSet<String>();
    for (String name : toClear.stringPropertyNames()) {
      String value = toClear.getProperty(name);
      Property prop = new Property(name, value);
      if (condition.apply(prop)) {
        toRemove.add(name); 
      }
    }
    for (String n : toRemove) {
      toClear.remove(n);
    }
	}
	
  
  /**
   * @param name a property name.
   * @return <code>true</code> if the value of the system property with the given name
   * is <code>true</code>.
   */
  public static boolean isSystemPropertyTrue(String name) {
    String value = System.getProperty(name);
    if (value != null) {
      return value.equalsIgnoreCase("true");
    }
    return false;
  }
  
  /**
   * @param name a property name.
   * @return <code>false</code> if the value of the system property with the given name
   * is <code>false</code>.
   */
  public static boolean isSystemPropertyFalse(String name) {
    return !isSystemPropertyTrue(name);
  }
	
	/**
	 * Clears Ubik-specific properties from the system properties.
	 */
	public static void clearUbikSystemProperties() {
	  clearProperties(System.getProperties(), new Condition<PropertiesUtil.Property>() {
      @Override
      public boolean apply(Property item) {
        return item.getName().startsWith("ubik");
      }
    });
	}
}
