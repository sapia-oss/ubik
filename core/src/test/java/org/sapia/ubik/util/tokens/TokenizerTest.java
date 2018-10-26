package org.sapia.ubik.util.tokens;

import org.junit.Test;

import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

public class TokenizerTest {

  @Test
  public void testGetInput() {
    Tokenizer tkz = new Tokenizer("http://sapia.org", ":");

    assertThat(tkz.getInput()).isEqualTo("http://sapia.org");
  }

  @Test
  public void testWithDelims() {
    Tokenizer tkz = new Tokenizer("http://sapia.org", ":");
    tkz = tkz.withDelims("//");

    assertThat(tkz.getDelims()[0]).isEqualTo("//");
  }

  @Test
  public void testTokenizeNext_start_of_input() {
    Tokenizer tkz = new Tokenizer("http://sapia.org/index.html", ":");
    Token tk = tkz.tokenizeNext();

    assertThat(tk.getValue()).isEqualTo("http");
    assertThat(tk.getMatchedDelim().get().getValue()).isEqualTo(":");
    assertThat(tk.isEos()).isFalse();
    assertThat(tk.getIndex()).isEqualTo(0);
  }

  @Test
  public void testTokenizeNext_after_first_match() {
    Tokenizer tkz = new Tokenizer("http://sapia.org/index.html", ":");
    tkz.tokenizeNext();
    Token tk = tkz.withDelims("//").tokenizeNext();

    assertThat(tk.getValue()).isEqualTo("");
    assertThat(tk.getMatchedDelim().get().getValue()).isEqualTo("//");
    assertThat(tk.isEos()).isFalse();
    assertThat(tk.getIndex()).isEqualTo(5);
  }

  @Test
  public void testTokenizeNext_after_second_match() {
    Tokenizer tkz = new Tokenizer("http://sapia.org/index.html", ":");
    tkz.tokenizeNext();
    tkz = tkz.withDelims("//");
    tkz.tokenizeNext();
    Token tk = tkz.withDelims("/").tokenizeNext();

    assertThat(tk.getValue()).isEqualTo("sapia.org");
    assertThat(tk.getMatchedDelim().get().getValue()).isEqualTo("/");
    assertThat(tk.isEos()).isFalse();
    assertThat(tk.getIndex()).isEqualTo(7);
  }

  @Test
  public void testTokenizeNext_end_of_string_reached() {
    Tokenizer tkz = new Tokenizer("http://sapia.org/index.html", ":");
    tkz.tokenizeNext();
    tkz = tkz.withDelims("//");
    tkz.tokenizeNext();
    tkz = tkz.withDelims("/");
    tkz.tokenizeNext();
    Token tk = tkz.withDelims("?").tokenizeNext();

    assertThat(tk.getValue()).isEqualTo("index.html");
    assertThat(tk.getMatchedDelim().isPresent()).isFalse();
    assertThat(tk.isEos()).isTrue();
    assertThat(tk.getIndex()).isEqualTo(17);
  }

  @Test
  public void testTokenizeNext_after_end_of_string_reached() {
    Tokenizer tkz = new Tokenizer("http://sapia.org/index.html", "?");
    tkz.tokenizeNext();

    Token tk = tkz.tokenizeNext();
    assertThat(tk.isNull()).isTrue();
    assertThat(tk.getMatchedDelim().isPresent()).isFalse();
    assertThat(tk.isEos()).isTrue();
    assertThat(tk.getIndex()).isEqualTo(-1);
  }

  @Test
  public void testPeekNextDelim() {
    Tokenizer tkz = new Tokenizer("http://sapia.org/index.html", ":", "//", "/");

    Optional<Delimiter> nextDelim = tkz.peekNextDelim();

    assertThat(nextDelim.isPresent()).isTrue();
    assertThat(nextDelim.get().getValue()).isEqualTo(":");
  }


  @Test
  public void testPeekNextDelims() {
    Tokenizer tkz = new Tokenizer("http://sapia.org/index.html", ":", "//", "/");

    DelimiterSequence sequence = tkz.peekNextDelims();

    assertThat(sequence.contains(":", "//", "/")).isTrue();
  }
}