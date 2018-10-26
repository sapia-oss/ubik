package org.sapia.ubik.util.tokens;

import java.util.Collections;
import java.util.List;


/**
 * Encapsulates a {@link List} of {@link Delimiter}s, in the order in which they appear
 * in a provided input string.
 *
 * @author yduchesne
 */
public class DelimiterSequence {

  private List<Delimiter> delims;

  /**
   * @param delims a {@link List} of {@link Delimiter}s.
   */
  DelimiterSequence(List<Delimiter> delims) {
    this.delims = delims;
  }

  /**
   * Tests if this instance contains the provided delimiter values, in that order.
   *
   * @param values one or more delimiter values to test for.
   * @return <code>true</code>.
   */
  public boolean contains(String...values) {
    if (delims.size() < values.length) {
      return false;
    }

    for (int i = 0; i < values.length; i++) {
      if (!delims.get(i).getValue().equals(values[i])) {
        return false;
      }
    }
    return  true;
  }

  /**
   * @return the unmodifiable {@link List} of {@link Delimiter}s held by this instance.
   */
  public List<Delimiter> asList() {
    return Collections.unmodifiableList(delims);
  }
}
