package org.sapia.ubik.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties-related utility class.
 * 
 * @author yduchesne
 *
 */
public final class PropertiesUtil {
	
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
}
