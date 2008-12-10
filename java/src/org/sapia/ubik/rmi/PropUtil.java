package org.sapia.ubik.rmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sapia.ubik.rmi.server.Log;

/**
 * This class provides utility methods over a list of <code>Properties</code> object that are sequentially
 * looked up for given values.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class PropUtil {
  
  private List _props = new ArrayList();
  
  /**
   * @param props some <code>Properties</code> to look up.
   * @return this instance.
   */
  public PropUtil addProperties(Properties props){
    _props.add(props);
    return this;
  }
  
  /**
   * @param key the key of the desired property.
   * @return the property value.
   * @throws NumberFormatException if the value could not be converted to an integer.
   * @throws IllegalArgumentException if no property exists for the given key.
   */
  public int getIntProperty(String key) throws NumberFormatException, IllegalArgumentException{
    String val = lookup(key, true);
    return Integer.parseInt(val);
  }
  
  /**
   * @param key the key of the desired property.
   * @param defaultValue the default value that must be returned if not property was found
   * for the given key.
   * @return the property value.
   * @throws NumberFormatException if the value could not be converted to an integer.
     */  
  public int getIntProperty(String key, int defaultValue) throws NumberFormatException{
    String val = lookup(key, false);
    if(val == null){
      return defaultValue;
    }
    return Integer.parseInt(val);
  }
  
  /**
   * @param key the key of the desired property.
   * @return the property value.
   * @throws NumberFormatException if the value could not be converted to a long.
   * @throws IllegalArgumentException if no property exists for the given key.
   */
  public long getLongProperty(String key) throws NumberFormatException, IllegalArgumentException{
    String val = lookup(key, true);
    return Long.parseLong(val);
  }
  
  /**
   * @param key the key of the desired property.
   * @param defaultValue the default value that must be returned if not property was found
   * for the given key.
   * @return the property value.
   * @throws NumberFormatException if the value could not be converted to a long.
     */  
  public long getLongProperty(String key, long defaultValue) throws NumberFormatException{
    String val = lookup(key, false);
    if(val == null){
      return defaultValue;
    }
    return Long.parseLong(val);
  }  
  
  
  /**
   * @param key the key of the desired property.
   * @return the property value.
   * @throws NumberFormatException if the value could not be converted to a float.
   * @throws IllegalArgumentException if no property exists for the given key.
   */
  public float getFloatProperty(String key) throws NumberFormatException, IllegalArgumentException{
    String val = lookup(key, true);
    return Long.parseLong(val);
  }
  
  /**
   * @param key the key of the desired property.
   * @param defaultValue the default value that must be returned if not property was found
   * for the given key.
   * @return the property value.
   * @throws NumberFormatException if the value could not be converted to a float.
     */  
  public float getFloat(String key, float defaultValue) throws NumberFormatException{
    String val = lookup(key, false);
    if(val == null){
      return defaultValue;
    }
    return Long.parseLong(val);
  }  
    
  
  /**
   * @param key the key of the desired property
   * @return <code>true</code> if the value corresponding to the given key equals <code>true</code>, <code>on</code>, or
   * <code>yes</code>, <code>false</code> otherwise, or if no value could be found for the given key.
   */
  public boolean getBooleanProperty(String key){
    String val = lookup(key, false);
    if(val == null){
      return false;
    }
    val = val.toLowerCase();
    return val.equals("true") || val.equals("yes") || val.equals("yes");
  }
  
  /**
   * @param key the key of the desired property
   * @return <code>true</code> if the value corresponding to the given key equals <code>true</code>, <code>on</code>, or
   * <code>yes</code>, <code>false</code> otherwise, or the passed in default if no value could be found for the given key.
   */
  public boolean getBooleanProperty(String key, boolean defaultValue){
    String val = lookup(key, false);
    if(val == null){
      return defaultValue;
    }
    val = val.toLowerCase();
    return val.equals("true") || val.equals("yes") || val.equals("yes");
  }
  
  /**
   * @param key the key of the desired property.
   * @return the value corresponding to the given key, or <code>null</code> if no
   * such value exists.
   */
  public String getProperty(String key){
    return lookup(key, false);
  }
  
  /**
   * @param key the key of the desired property.
   * @return the value corresponding to the given key, or the given
   * default value of the desired property was not found.
   */
  public String getProperty(String key, String defaultValue){
    String val = lookup(key, false);
    if(val == null){
      return defaultValue;
    }
    return val;
  }  
  
  private String lookup(String key, boolean throwExcIfNotFound){
    Properties current;
    String val;
    for(int i = 0; i < _props.size(); i++){
      current = (Properties)_props.get(i);
      val = current.getProperty(key);
      if(val != null){
        Log.info("**** PROPERTY ****", key + " = " + val);
        return val;
      }
    }
    if(throwExcIfNotFound){
      throw new IllegalArgumentException("No value found for property: " + key);
    }
    return null;
  }

}
