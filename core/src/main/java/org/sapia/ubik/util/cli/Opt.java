package org.sapia.ubik.util.cli;

import org.sapia.ubik.util.Strings;

/**
 * Models a command-line argument or option.
 * 
 * @author yduchesne
 *
 */
public class Opt {
	
	private static final String BLANK = "";
	
	public enum Type {
		
		ARG, SWITCH;
		
	}

	private Type 	 type;
	private String name, value;
	
	Opt(Type type, String name, String value) {
		this.type  = type;
		this.name  = name;
		this.value = value;
  }
	
	/**
	 * @return this option's name.
	 */
	public String getName() {
	  return name;
  }
	
	/**
	 * @return this option's value, or <code>null</code> if none was specified (or if this instance's type if {@link Type#ARG}).
	 */
	public String getValue() {
	  return value;
  }
	
	/**
	 * @return an empty {@link String} if this instance's {@link #value} is <code>null</code>, or this instance's value with its
	 * heading and tailing whitespaces trimmed if it isn't <code>null</code>.
	 */
	public String getTrimmedValueOrBlank() {
		if (value == null) {
			return "";
		} else {
			return value.trim();
		}
	}
	
	/**
	 * @return this instance's value, converted to an <code>int</code>.
	 */
	public int getIntValue() {
		if (value == null) {
			throw new IllegalArgumentException("Option value is null for: " + name);
		}
		return Integer.parseInt(value);
	}
	
	/**
	 * @return <code>true</code> if this instance is blank or <code>null</code>.
	 */
	public boolean isBlankOrNull() {
		return value == null || value.trim().equals(BLANK);
	}
	
	/**
	 * @return this option's type.
	 */
	public Type getType() {
	  return type;
  }
	
	@Override
	public String toString() {
	  return Strings.toString("name", name, "value", value, "type", type);
	}
	 
	
}
