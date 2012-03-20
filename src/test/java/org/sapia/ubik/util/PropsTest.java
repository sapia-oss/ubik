package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class PropsTest {
	
	private Props props;
	
	@Before
	public void setUp() {
		props = new Props();
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
		props.addPropertyLookup(new Props.PropertyLookup() {
			@Override
			public String getProperty(String name) {
				return System.getProperty(name);
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

	@Test(expected=IllegalArgumentException.class)
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
		assertEquals(System.getProperty("user.name"), Props.getSystemProperties().getProperty("user.name"));
	}

}
