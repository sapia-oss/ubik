package org.sapia.ubik.util;

import java.util.Properties;

/**
 * Extends the {@link Properties} class by adding convenience methods.
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
  
  public Conf toConf() {
    return Conf.newInstance().addProperties(this);
  }
    
}
