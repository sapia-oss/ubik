package org.sapia.ubik.util.tokens;

import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.util.Assertions;

import java.util.Optional;
import java.util.function.Supplier;

public class Token {
  private Delimiter  delim;
  private String     token;
  private int        index;
  private boolean    eos;

  Token(Delimiter delim, String token, int index, boolean eos) {
    this.delim = delim;
    this.token = token;
    this.index = index;
    this.eos   = eos;
  }


  /**
   * @return the optional delimiter that was matched - if it is not set, it means that the end of the input has been reached.
   */
  public Optional<Delimiter> getMatchedDelim() {
    return Optional.ofNullable(delim);
  }

  /**
   * @param errorSupplier a {@link Supplier} of a {@link UriSyntaxException} to throw if this instance's matched
   *                      {@link Delimiter} is not set.
   * @return the matched {@link Delimiter}.
   * @throws UriSyntaxException if this instance's matched {@link Delimiter} is not set.
   */
  public <T extends UriSyntaxException> Delimiter getMatchedDelimOrThrow(Supplier<T> errorSupplier) throws UriSyntaxException {
    if (delim == null) {
      throw errorSupplier.get();
    }
    return delim;
  }

  /**
   * @return this instance's value.
   */
  public String getValue() {
    Assertions.illegalState(token == null,
        "Token value is null: check for presence with isNull()/isSet() in order to determine if this method can be called");
    return token;
  }

  /**
   * @return the index corresponding to the location of this token's first character, in the original input.
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return <code>true</code> if no token was extracted.
   */
  public boolean isNull() {
    return token == null;
  }

  /**
   * @return <code>true</code> if a token was extracted.
   */
  public boolean isSet() {
    return token != null;
  }

  /**
   * @return <code>true</code> if the end of the input string has been reached.
   */
  public boolean isEos() {
    return eos ;
  }

}