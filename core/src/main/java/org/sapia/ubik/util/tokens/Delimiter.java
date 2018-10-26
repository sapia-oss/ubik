package org.sapia.ubik.util.tokens;

/**
 * Wraps a delimiter, and the index at which it was found in a provided input string.
 *
 * @author yduchesne
 */
public class Delimiter {

  private String value;
  private int    index;

  Delimiter(String value, int index) {
    this.value = value;
    this.index = index;
  }

  /**
   * @return the index at which this instance's value was found in the original input string.
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return this instance's value.
   */
  public String getValue() {
    return value;
  }

  /**
   * @param otherDelimValue another delimiter value.
   * @return <code>true</code> if this instance's value is equal to the one provided.
   */
  public boolean isEqualTo(String otherDelimValue) {
    return otherDelimValue == null ? false : value.equals(otherDelimValue);
  }
}
