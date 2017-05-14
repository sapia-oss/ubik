package org.sapia.ubik.util;

import java.util.Properties;

/**
 * Extends the {@link Properties} class by adding convenience methods that are
 * type-specific and allow for chained invocations.
 * 
 * @author yduchesne
 *
 */
public class ExtendedProperties extends Properties {
  
  public ExtendedProperties setString(String name, String value) {
    setProperty(name, value);
    return this;
  }
  
  public ExtendedProperties setInt(String name, int value) {
    setProperty(name, Integer.toString(value));
    return this;
  }
  
  public ExtendedProperties setLong(String name, long value) {
    setProperty(name, Long.toString(value));
    return this;
  }

  public ExtendedProperties setBoolean(String name, boolean value) {
    setProperty(name, Boolean.toString(value));
    return this;
  }
  
  /**
   * @return a new {@link Conf} object, with this instance added to it.
   */
  public Conf toConf() {
    return Conf.newInstance()
        .addSystemProperties()
        .addProperties(this);
  }
    
}
