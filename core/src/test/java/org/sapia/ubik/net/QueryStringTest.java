package org.sapia.ubik.net;

import org.sapia.ubik.util.Serialization;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryStringTest {

  @Test
  public void testGetParameterValue() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .param("name2", "value2")
        .build();

    assertThat(qs.getParameterValue("name1")).isEqualTo("value1");
    assertThat(qs.getParameterValue("name2")).isEqualTo("value2");
  }

  @Test
  public void testGetParameterValue_not_found() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .build();

    assertThat(qs.getParameterValue("name2")).isNull();
  }

  @Test
  public void testGetParameterValues() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .param("name2", "value2")
        .build();

    assertThat(qs.getParameterValues("name1").get(0)).isEqualTo("value1");
    assertThat(qs.getParameterValues("name2").get(0)).isEqualTo("value2");
  }

  @Test
  public void testGetParameterValues_not_found() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .build();

    assertThat(qs.getParameterValues("name2")).isEmpty();
  }

  @Test
  public void testGetParameters() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .param("name2", "value2")
        .build();

    Map<String, List<String>> parameters = qs.getParameters();

    assertThat(parameters.get("name1").get(0)).isEqualTo("value1");
    assertThat(parameters.get("name2").get(0)).isEqualTo("value2");
  }

  @Test
  public void testGetUniqueParameters() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .param("name2", "value2")
        .build();

    Map<String, String> parameters = qs.getUniqueParameters();

    assertThat(parameters.get("name1")).isEqualTo("value1");
    assertThat(parameters.get("name2")).isEqualTo("value2");
  }

  @Test
  public void testToString() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .param("name2", "value2")
        .build();

    assertThat(qs.toOrderedString()).isEqualTo("name1=value1&name2=value2");
  }

  @Test
  public void testToString_single_param() {
    QueryString qs = QueryString.builder()
        .param("name1", "value1")
        .build();
    assertThat(qs.toOrderedString()).isEqualTo("name1=value1");
  }


  @Test
  public void testAppend() {
    QueryString qs1 = QueryString.builder()
        .param("n1", "v1")
        .build();
    QueryString qs2 = qs1.append(QueryString.builder().param("n2", "v2").build());

    assertThat(qs2.getParameterValue("n1")).isEqualTo("v1");
    assertThat(qs2.getParameterValue("n2")).isEqualTo("v2");
  }

  @Test
  public void testAppend_with_map() {
    QueryString qs1 = QueryString.builder()
        .param("n1", "v1")
        .build();
    Map<String, String> toAppend = new HashMap<>();
    toAppend.put("n2", "v2");
    QueryString qs2 = qs1.append(toAppend);

    assertThat(qs2.getParameterValue("n1")).isEqualTo("v1");
    assertThat(qs2.getParameterValue("n2")).isEqualTo("v2");
  }

  @Test
  public void testIsEmpty() {
    assertThat(new QueryString().isEmpty()).isTrue();
  }

  @Test
  public void testIsEmpty_false() {
    assertThat(QueryString.builder().param("n1", "v1").build().isEmpty()).isFalse();
  }
  
  @Test
  public void testSerialization() throws Exception {
    QueryString qs = QueryString.builder()
        .param("n1", "v1")
        .build();

    byte[] bytes = Serialization.serialize(qs);

    QueryString copy = (QueryString) Serialization.deserialize(bytes);

    assertThat(copy.getParameterValue("n1")).isEqualTo("v1");
  }
  
  @Test
  public void testEquals() {
    QueryString qs1 = QueryString.builder()
	  .param("n1", "v1")
	  .build();  
    QueryString qs2 = QueryString.builder()
	  .param("n1", "v1")
	  .build();
    
    assertThat(qs1).isEqualTo(qs2);
  }

  @Test
  public void testEquals_different_param_order() {
    QueryString qs1 = QueryString.builder()
	  .param("n1", "v1")
	  .param("n2", "v2")
	  .build();  
    QueryString qs2 = QueryString.builder()
      .param("n2", "v2")
	  .param("n1", "v1")
	  .build();
    
    assertThat(qs1).isEqualTo(qs2);
  }

  @Test
  public void testEquals_different_parameter_count() {
    QueryString qs1 = QueryString.builder()
	  .param("n1", "v1")
	  .param("n2", "v2")
	  .build();  
    
    QueryString qs2 = QueryString.builder()
      .param("n1", "v1")
	  .build();
    
    assertThat(qs1).isNotEqualTo(qs2);
  }
  
  @Test
  public void testEquals_empty_parameters() {
    QueryString qs1 = QueryString.builder()
	  .build();  
    
    QueryString qs2 = QueryString.builder()
	  .build();
    
    assertThat(qs1).isEqualTo(qs2);
  }
}
