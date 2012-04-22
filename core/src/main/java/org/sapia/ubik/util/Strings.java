package org.sapia.ubik.util;

/**
 * Provides string utility methods.
 * 
 * @author yduchesne
 *
 */
public final class Strings {

  private Strings() {
  }
  
  /**
   * Returns a String for the given array of fields. A "field" consists of a name/value pair. Thus,
   * the given array is expected to be structured as such:
   * <pre>
   * [name-0,value-0,name-1,value-1...]
   * </pre>
   * For <code>null</code> values, a "null" string is inserted in the resulting {@link String} instance.
   * 
   * @param fields an array of fields (name/value pairs).
   * @return a {@link String}. 
   */
  public static String toString(Object... fields){
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for(int i = 0; i < fields.length; i += 2) {
      sb.append(fields[i].toString())
        .append("=")
        .append(i + 1 < fields.length && fields[i + 1] != null ? fields[i+1].toString() : "null");
      
      if(i+1 < fields.length - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Returns a String for the given array of fields. A "field" consists of a name/value pair. Thus,
   * the given array is expected to be structured as such:
   * <pre>
   * [name-0,value-0,name-1,value-1...]
   * </pre>
   * For <code>null</code> values, a "null" string is inserted in the resulting {@link String} instance.
   * 
   * @param fields an array of fields (name/value pairs).
   * @return a {@link String}. 
   */
  public static String toStringFor(Object owner, Object... fields){
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(owner.getClass().getSimpleName()).append("@").append(Integer.toHexString(owner.hashCode())).append(":");
    for(int i = 0; i < fields.length; i += 2) {
      sb.append(fields[i].toString())
        .append("=")
        .append(i + 1 < fields.length && fields[i + 1] != null ? fields[i + 1].toString() : "null");
      
      if(i+1 < fields.length - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }
  /**
   * @param s a {@link String}
   * @return <code>true</code> if the given string is <code>null</code>, or if it 
   * consists of only whitespaces.
   */
  public static boolean isBlank(String s) {
    return s == null || s.trim().length() == 0;
  }

}
