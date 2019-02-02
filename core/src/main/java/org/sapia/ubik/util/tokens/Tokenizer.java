package org.sapia.ubik.util.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.sapia.ubik.util.Assertions;

/**
 * Utility class used to tokenize strings.
 *
 * @author yduchesne
 */
public class Tokenizer {

  private String[]    delims;
  private String      input;
  private int         index;

  /**
   * @param input the input string to tokenize.
   * @param delims one or more delimiters to match.
   */
  public Tokenizer(String input, String...delims) {
    Assertions.isFalse(delims.length == 0, "At least one delimiter must be specified");
    this.delims   = delims;
    this.input    = input;
  }

  /**
   * @return the input to tokenize.
   */
  public String getInput() {
    return input;
  }

  /**
   * @return this instance's current index, corresponding to the current position in the input string.
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return <code>true</code> if this instance's current index is equal/greater than the input's length.
   */
  public boolean isEos() {
    return index >= input.length();
  }

  /**
   * @param delims one or more new delimiters to use.
   * @return a copy of this instance, with this instance's state but with the
   * given delimiters.
   */
  public Tokenizer withDelims(String...delims) {
    Tokenizer tk = new Tokenizer(input, delims);
    tk.index = index;
    return tk;
  }

  /**
   * @param index the index at which to start tokenizing.
   * @return a copy of this instance, with this instance's state but with the
   * given index.
   */
  public Tokenizer withIndex(int index) {
    Assertions.isFalse(index >= input.length(), "Invalid index: %s. Must be < than %s", index, input.length());
    Tokenizer tk = new Tokenizer(input, delims);
    tk.index = index;
    return tk;
  }

  /**
   * @return the remaining characters in the input string - that is, the substring
   * starting at this instance's current index.
   */
  public String remaining() {
    if (index >= input.length()) {
      return "";
    } else {
      return input.substring(index);
    }
  }

  /**
   * @return an extracted {@link Token}.
   */
  public Token tokenizeNext() {
    if (index >= input.length()) {
      return new Token(null, null, -1, true);
    }

    for (int d = 0; d < delims.length; d++) {
      Delimiter delim = nextDelim(index);
      if (delim != null) {
        Token tok = new Token(delim, input.substring(index, delim.getIndex()), index, false);
        index = index + tok.getValue().length() + delim.getValue().length();
        return tok;
      }
    }

    Token tok = new Token(null, input.substring(index), index, true);
    index = input.length();
    return tok;
  }

  /**
   * Finds the next delimiter in the provided input, starting at this instance's current
   * index - this instance's index remains untouched after this method exits.
   *
   * @return the next delimiter that is occurring (the returned {@link Optional} will be
   * empty if no delimiter is encountered.
   */
  public Optional<Delimiter> peekNextDelim() {
    if (index >= input.length()) {
      return Optional.empty();
    }

    return Optional.ofNullable(nextDelim(index));
  }

  /**
   * @return the {@link DelimiterSequence} corresponding to the next delimiters
   * found in this instance's input, starting that this instance's current index.
   */
  public DelimiterSequence peekNextDelims() {
    if (index >= input.length()) {
      return new DelimiterSequence(Collections.emptyList());
    }

    List<Delimiter> found = new ArrayList<>();

    int tmpIndex = index;
    while (tmpIndex < input.length()) {
      Delimiter delim = nextDelim(tmpIndex);
      if (delim != null) {
        found.add(delim);
        tmpIndex = delim.getIndex() + delim.getValue().length();
      } else {
        break;
      }
    }

    return new DelimiterSequence(found);
  }

  // --------------------------------------------------------------------------
  // Visible for testing

  /**
   * @return this instance's delimiters.
   */
  String[] getDelims() {
    return delims;
  }

  // --------------------------------------------------------------------------
  // Restricted

  private Delimiter nextDelim(int tmpIndex) {
    Delimiter nextDelim = null;
    for (int d = 0; d < delims.length; d++) {
      String delim = delims[d];
      int i = input.indexOf(delim, tmpIndex);
      if (i >= 0) {
        if (nextDelim == null) {
          nextDelim = new Delimiter(delim, i);
        } else if (nextDelim != null && i < nextDelim.getIndex()) {
          nextDelim = new Delimiter(delim, i);
        }
      }
    }
    return nextDelim;
  }

}
