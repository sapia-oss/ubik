package org.sapia.ubik.util.tokens;

import org.junit.Before;
import org.junit.Test;


import java.util.Arrays;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

public class DelimiterSequenceTest {

  private DelimiterSequence sequence;

  @Before
  public void setUp() throws Exception {
    sequence = new DelimiterSequence(Arrays.asList(
       new Delimiter("/", 0),
       new Delimiter(":", 1),
       new Delimiter("&", 2)
    ));
  }

  @Test
  public void contains_less_delims() throws Exception {
    assertThat(sequence.contains("/", ":")).isTrue();
  }

  @Test
  public void contains_same_delims() throws Exception {
    assertThat(sequence.contains("/", ":", "&")).isTrue();
  }

  @Test
  public void contains_more_delims() throws Exception {
    assertThat(sequence.contains("/", ":", "&", "?")).isFalse();
  }

  @Test
  public void contains_with_different_order() throws Exception {
    assertThat(sequence.contains("&", ":", "/")).isFalse();
  }

  @Test
  public void asList() throws Exception {
    List<Delimiter> lst = sequence.asList();

    assertThat(lst).hasSize(3);

    assertThat(lst.get(0).getIndex()).isEqualTo(0);
    assertThat(lst.get(0).getValue()).isEqualTo("/");

    assertThat(lst.get(1).getIndex()).isEqualTo(1);
    assertThat(lst.get(1).getValue()).isEqualTo(":");

    assertThat(lst.get(2).getIndex()).isEqualTo(2);
    assertThat(lst.get(2).getValue()).isEqualTo("&");
  }

}