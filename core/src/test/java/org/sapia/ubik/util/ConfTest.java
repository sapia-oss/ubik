package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ConfTest {

  private Conf props;

  @Before
  public void setUp() {
    props = new Conf();
  }

  @Test
  public void testAddProperties() {
    Properties properties = new Properties();
    properties.setProperty("key", "value");
    props.addProperties(properties);
    assertEquals("value", props.getProperty("key"));
  }

  @Test
  public void testAddMap() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "value");
    props.addMap(properties);
    assertEquals("value", props.getProperty("key"));
  }

  @Test
  public void testAddSystemProperties() {
    props.addSystemProperties();
    assertEquals(System.getProperty("user.name"), props.getProperty("user.name"));
  }

  @Test
  public void testAddPropertyLookup() {
    props.addPropertyLookup(new Conf.PropertyLookup() {
      @Override
      public String getProperty(String name) {
        return System.getProperty(name);
      }
      
      @Override
      public Set<String> propertyNames() {
        return System.getProperties().stringPropertyNames();
      }
    });
    assertEquals(System.getProperty("user.name"), props.getProperty("user.name"));
  }

  @Test
  public void testGetIntProperty() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "1");
    props.addMap(properties);
    assertEquals(1, props.getIntProperty("key"));
  }

  @Test
  public void testGetIntPropertyWithDefault() {
    assertEquals(1, props.getIntProperty("key", 1));
  }

  @Test
  public void testGetLongProperty() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "1");
    props.addMap(properties);
    assertEquals(1, props.getLongProperty("key"));
  }

  @Test
  public void testGetLongPropertyWithDefault() {
    assertEquals(1, props.getLongProperty("key", 1));
  }

  @Test
  public void testGetFloatProperty() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "1.5");
    props.addMap(properties);
    assertTrue(1.5f == props.getFloatProperty("key"));
  }

  @Test
  public void testGetFloatPropertyWithDefault() {
    assertTrue(1.5f == props.getFloatProperty("key", 1.5f));
  }

  @Test
  public void testGetBooleanPropertyWithTrue() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "true");
    props.addMap(properties);
    assertTrue(props.getBooleanProperty("key"));
  }

  @Test
  public void testGetBooleanPropertyWithYes() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "yes");
    props.addMap(properties);
    assertTrue(props.getBooleanProperty("key"));
  }

  @Test
  public void testGetBooleanPropertyWithOn() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "on");
    props.addMap(properties);
    assertTrue(props.getBooleanProperty("key"));
  }

  @Test
  public void testGetBooleanPropertyWithDefault() {
    assertTrue(props.getBooleanProperty("key", true));
  }

  @Test
  public void testGetTimeProperty() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "30s");
    props.addMap(properties);
    assertEquals(30, props.getTimeProperty("key").getValueInSeconds());
  }

  @Test
  public void testGetTimePropertyWithDefault() {
    assertEquals(30, props.getTimeProperty("key", TimeValue.createSeconds(30)).getValueInSeconds());
  }


  @Test
  public void testGetTimeRangeProperty() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key", "30s:30s");
    props.addMap(properties);
    assertEquals(30, props.getTimeRangeProperty("key").getRandomTime().getValueInSeconds());
  }

  @Test
  public void testGetTimeRangePropertyWithDefault() {
    assertEquals(30, props.getTimeRangeProperty("key", TimeRange.valueOf("30s:30s")).getRandomTime().getValueInSeconds());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNotNullProperty() {
    props.getNotNullProperty("someValue");
  }

  @Test
  public void testGetPropertyWithDefault() {
    assertEquals("value", props.getProperty("key", "value"));
  }

  @Test
  public void testGetClassProperty() throws Exception {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("class.name", "java.lang.String");
    props.addMap(properties);
    assertEquals(String.class, props.getClass("class.name"));
  }

  @Test
  public void testGetSystemProperties() {
    assertEquals(System.getProperty("user.name"), Conf.getSystemProperties().getProperty("user.name"));
  }

}
