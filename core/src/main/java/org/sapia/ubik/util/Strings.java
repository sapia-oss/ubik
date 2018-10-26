package org.sapia.ubik.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides string utility methods.
 * 
 * @author yduchesne
 * 
 */
public final class Strings {

  private static final char[] EMPTY_IGNORE_CHARS = new char[]{};
  
  private Strings() {
  }

  /**
   * Returns a String for the given array of fields. A "field" consists of a
   * name/value pair. Thus, the given array is expected to be structured as
   * such:
   * 
   * <pre>
   * [name-0,value-0,name-1,value-1...]
   * </pre>
   * 
   * For <code>null</code> values, a "null" string is inserted in the resulting
   * {@link String} instance.
   * 
   * @param fields
   *          an array of fields (name/value pairs).
   * @return a {@link String}.
   */
  public static String toString(Object... fields) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < fields.length; i += 2) {
      sb.append(fields[i].toString()).append("=").append(i + 1 < fields.length && fields[i + 1] != null ? fields[i + 1].toString() : "null");

      if (i + 1 < fields.length - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Returns a String for the given array of fields. A "field" consists of a
   * name/value pair. Thus, the given array is expected to be structured as
   * such:
   * 
   * <pre>
   * [name-0,value-0,name-1,value-1...]
   * </pre>
   * 
   * For <code>null</code> values, a "null" string is inserted in the resulting
   * {@link String} instance.
   * 
   * @param fields
   *          an array of fields (name/value pairs).
   * @return a {@link String}.
   */
  public static String toStringFor(Object owner, Object... fields) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(owner.getClass().getSimpleName()).append("@").append(Integer.toHexString(owner.hashCode())).append(":");
    for (int i = 0; i < fields.length; i += 2) {
      sb.append(fields[i].toString()).append("=").append(i + 1 < fields.length && fields[i + 1] != null ? fields[i + 1].toString() : "null");

      if (i + 1 < fields.length - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * @param s
   *          a {@link String}
   * @return <code>true</code> if the given string is <code>null</code>, empty, or containing
   *          only whitespaces.
   */
  public static boolean isBlank(String s) {
    if (s == null || s.length() == 0) {
      return true;
    }
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isWhitespace(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param s
   *          a {@link String}
   * @return <code>true</code> if the given string is NOT <code>null</code>, empty, or containing
   *          only whitespace.
   */
  public static boolean isNotBlank(String s) {
    return !isBlank(s);
  }

  /**
   * @param toSplit a {@link String} to split.
   * @param delim the delimiter character to use.
   * @param ignoreWhiteSpace if <code>true</code>, whitespaces will be trimmed.
   * @return the array of {@link String}s corresponding to the result of the split operation.
   */
  public static String[] split(String toSplit, char delim, boolean ignoreWhiteSpace) {
    return split(toSplit, delim, ignoreWhiteSpace, EMPTY_IGNORE_CHARS);
  }
  
  /**
   * @param toSplit a {@link String} to split.
   * @param delim the delimiter character to use.
   * @param ignoreWhiteSpace if <code>true</code>, whitespaces will be trimmed.
   * @param ignoreChars an array holding characters that should be ignored.
   * @return the array of {@link String}s corresponding to the result of the split operation.
   */
  public static String[] split(String toSplit, char delim, boolean ignoreWhiteSpace, char[] ignoreChars) {
    Set<Character> ignoreCharSet = new HashSet<>();
    for (int i = 0; i < ignoreChars.length; i++) {
      ignoreCharSet.add(Character.valueOf(ignoreChars[i]));
    }
    List<String> parts = new ArrayList<String>();
    StringBuilder part = new StringBuilder();
    for (int i = 0; i < toSplit.length(); i++) {
      char c = toSplit.charAt(i);
      if (ignoreCharSet.contains(Character.valueOf(c))) {
        continue;
      } if (Character.isWhitespace(c) && ignoreWhiteSpace) {
        continue;
      } else if (c == delim) {
        if (part.length() > 0 || (part.length() == 0 && !ignoreWhiteSpace)) {
          parts.add(part.toString());
          part.delete(0, part.length());
        }
      } else {
        part.append(c);
      }
    }
    if (part.length() > 0 || (part.length() == 0 && !ignoreWhiteSpace)) {
      parts.add(part.toString());
    }
    return parts.toArray(new String[parts.size()]);
  }

  /**
   * Joins the objects in the given array into a single string. The following are equivalent:
   * <pre>
   *   Strings.join('/', toConcat);
   *   Strings.join('/', 0, toConcat.length, toConcat);
   * </pre>
   *
   * @param delim
   * @param toConcat
   * @return the result of the join operation on the given strings.
   *
   * @see #joinAt(char, int, int, Object...)
   */
  public static String join(char delim, Object...toConcat) {
    return joinAt(delim, 0, toConcat.length, toConcat);
  }

  /**
   * Joins the objects in the given array into a single string, starting at the given <code>startIndex</code>,
   * up to the given <code>endIndex</code> (that is, it will stop at <code>endIndex - 1</code>).
   * The following are equivalent:
   * <pre>
   *   Strings.join('/', toConcat);
   *   Strings.join('/', 0, toConcat.length, toConcat);
   * </pre>
   *
   * @param delim the character corresponding to the delimiter to use.
   * @param startIndex the index (inclusive) of the first string to start at, in the given array.
   * @param endIndex the index (exclusive) of the last string to join.
   * @param toConcat one or more strings to concatenate.
   * @return the string resulting from the concatenation.
   */
  public static String joinAt(char delim, int startIndex, int endIndex, Object...toConcat) {
    Assertions.isFalse(startIndex < 0, "Invalid start index: must be positive");
    Assertions.isFalse(startIndex >= toConcat.length, "Invalid start index: cannot be greater than or equal to input length");
    Assertions.isFalse(endIndex < startIndex, "Invalid end index: cannot be smaller than start index");
    Assertions.isFalse(endIndex > toConcat.length, "Invalid end index: cannot be greater than or equal to input length");
    StringBuilder result = new StringBuilder();
    for (int i = startIndex; i < endIndex; i++) {
      if (i > startIndex) {
        result.append(delim);
      }
      result.append(toConcat[i]);
    }
    return result.toString();
  }
}
