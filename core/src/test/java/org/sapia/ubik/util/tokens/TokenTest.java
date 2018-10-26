package org.sapia.ubik.util.tokens;

import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;

public class TokenTest {

  @Test
  public void testGetMatchedDelim() throws Exception {
    Token t = new Token(new Delimiter("/", 0), "test", 0, false);

    assertThat(t.getMatchedDelim().get().getValue()).isEqualTo("/");
  }

  @Test
  public void testGetMatchedDelim_not_set() throws Exception {
    Token t = new Token(null, "test", 0, false);

    assertThat(t.getMatchedDelim().isPresent()).isFalse();
  }

  @Test
  public void testGetValue() throws Exception {
    Token t = new Token(new Delimiter("/", 0), "test", 0, false);

    assertThat(t.getValue()).isEqualTo("test");
  }

  @Test
  public void testIsNull() throws Exception {
    Token t = new Token(new Delimiter("/", 0), null, 0, false);

    assertThat(t.isNull()).isTrue();
  }

  @Test
  public void testIsNull_false() throws Exception {
    Token t = new Token(new Delimiter("/", 0), "test", 0, false);

    assertThat(t.isNull()).isFalse();
  }

  @Test
  public void testIsSet() throws Exception {
    Token t = new Token(new Delimiter("/", 0), "test", 0, false);

    assertThat(t.isSet()).isTrue();
  }

  @Test
  public void testIsSet_false() throws Exception {
    Token t = new Token(new Delimiter("/", 0), null, 0, false);

    assertThat(t.isSet()).isFalse();
  }


  @Test
  public void testIsEos() throws Exception {
    Token t = new Token(null, "test", 0, true);

    assertThat(t.isEos()).isTrue();
  }

  @Test
  public void testIsEos_false() throws Exception {
    Token t = new Token(null, "test", 0, false);

    assertThat(t.isEos()).isFalse();
  }
}