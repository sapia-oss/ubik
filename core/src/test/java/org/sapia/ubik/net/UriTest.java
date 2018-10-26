package org.sapia.ubik.net;

import org.junit.Test;
import org.sapia.ubik.util.Serialization;

import static org.assertj.core.api.Assertions.assertThat;

public class UriTest {

  private static final String FULL_HTTP_URI_INPUT             = "http://tom:ahawk@www.sapia.org:80/index.html?p1=v1&p2=v2&p3=#intro";

  private static final String HTTP_URI_INPUT_WITHOUT_USER     = "http://www.sapia.org:80/index.html?p1=v1&p2=v2&p3=#intro";

  private static final String HTTP_URI_INPUT_WITHOUT_PORT     = "http://tom:ahawk@www.sapia.org/index.html?p1=v1&p2=v2&p3=#intro";

  private static final String HTTP_URI_INPUT_WITHOUT_PATH     = "http://tom:ahawk@www.sapia.org?p1=v1&p2=v2&p3=#intro";

  private static final String HTTP_URI_INPUT_WITHOUT_FRAGMENT = "http://tom:ahawk@www.sapia.org/index.html?p1=v1&p2=v2&p3=";
  
  private static final String NO_SCHEME_URI                   = "localhost:1099";
  
  private static final String URI_WITH_IP_ADDRESS             = "http://172.31.98.227:1099/service";

  @Test
  public void testParser_parse_scheme() {
    Uri.UriParser parser = new Uri.UriParser(FULL_HTTP_URI_INPUT);
    parser.parseScheme();
    Uri uri = parser.buildUri();

    assertThat(uri.getScheme()).isEqualTo("http");
  }

  @Test
  public void testParser_parse_user() {
    Uri.UriParser parser = new Uri.UriParser(FULL_HTTP_URI_INPUT).advanceTo("http://".length());
    parser.parseUser();
    Uri uri = parser.buildUri();

    assertThat(uri.getUser().get()).isEqualTo("tom");
  }

  @Test
  public void testParser_parse_password() {
    Uri.UriParser parser = new Uri.UriParser(FULL_HTTP_URI_INPUT).advanceTo("http://tom:".length());
    parser.parsePassword();
    Uri uri = parser.buildUri();

    assertThat(uri.getPassword().get()).isEqualTo("ahawk");
  }

  @Test
  public void testParser_parse_host() {
    Uri.UriParser parser = new Uri.UriParser(FULL_HTTP_URI_INPUT).advanceTo("http://tom:ahawk@".length());
    parser.parseHost();
    Uri uri = parser.buildUri();

    assertThat(uri.getHost()).isEqualTo("www.sapia.org");
  }

  @Test
  public void testParser_parse_port() {
    Uri.UriParser parser = new Uri.UriParser(FULL_HTTP_URI_INPUT).advanceTo("http://tom:ahawk@www.sapia.org:".length());
    parser.parsePort();
    Uri uri = parser.buildUri();

    assertThat(uri.getPort()).isEqualTo(80);
  }

  @Test
  public void testParser_parse_path() {
    Uri.UriParser parser = new Uri.UriParser(FULL_HTTP_URI_INPUT).advanceTo("http://tom:ahawk@www.sapia.org:80/".length());
    parser.parsePath();
    Uri uri = parser.buildUri();

    assertThat(uri.getPath()).isEqualTo("/index.html");
  }

  @Test
  public void testParser_parse_fragment() {
    Uri.UriParser parser = new Uri.UriParser(FULL_HTTP_URI_INPUT).advanceTo("http://tom:ahawk@www.sapia.org:80/index.html?p1=v1&p2=v2&p3=#".length());
    parser.parseFragment();
    Uri uri = parser.buildUri();

    assertThat(uri.getFragment().get()).isEqualTo("intro");
  }

  @Test
  public void testParse_full_http_uri() {
    Uri uri = Uri.parse(FULL_HTTP_URI_INPUT);

    assertThat(uri.getScheme()).isEqualTo("http");
    assertThat(uri.getUser().get()).isEqualTo("tom");
    assertThat(uri.getPassword().get()).isEqualTo("ahawk");
    assertThat(uri.getHost()).isEqualTo("www.sapia.org");
    assertThat(uri.getPort()).isEqualTo(80);
    assertThat(uri.getPath()).isEqualTo("/index.html");
    assertThat(uri.getFragment().get()).isEqualTo("intro");
    assertThat(uri.getQueryString().getParameterValue("p1")).isEqualTo("v1");
    assertThat(uri.getQueryString().getParameterValue("p2")).isEqualTo("v2");
    assertThat(uri.getQueryString().getParameterValue("p3")).isEqualTo("");
  }

  @Test
  public void testParse_http_uri_without_user() {
    Uri uri = Uri.parse(HTTP_URI_INPUT_WITHOUT_USER);

    assertThat(uri.getScheme()).isEqualTo("http");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("www.sapia.org");
    assertThat(uri.getPort()).isEqualTo(80);
    assertThat(uri.getPath()).isEqualTo("/index.html");
    assertThat(uri.getFragment().get()).isEqualTo("intro");
    assertThat(uri.getQueryString().getParameterValue("p1")).isEqualTo("v1");
    assertThat(uri.getQueryString().getParameterValue("p2")).isEqualTo("v2");
    assertThat(uri.getQueryString().getParameterValue("p3")).isEqualTo("");
  }

  @Test
  public void testParse_http_uri_without_port() {
    Uri uri = Uri.parse(HTTP_URI_INPUT_WITHOUT_PORT);

    assertThat(uri.getScheme()).isEqualTo("http");
    assertThat(uri.getUser().get()).isEqualTo("tom");
    assertThat(uri.getPassword().get()).isEqualTo("ahawk");
    assertThat(uri.getHost()).isEqualTo("www.sapia.org");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("/index.html");
    assertThat(uri.getFragment().get()).isEqualTo("intro");
    assertThat(uri.getQueryString().getParameterValue("p1")).isEqualTo("v1");
    assertThat(uri.getQueryString().getParameterValue("p2")).isEqualTo("v2");
    assertThat(uri.getQueryString().getParameterValue("p3")).isEqualTo("");
  }

  @Test
  public void testParse_http_uri_without_path() {
    Uri uri = Uri.parse(HTTP_URI_INPUT_WITHOUT_PATH);

    assertThat(uri.getScheme()).isEqualTo("http");
    assertThat(uri.getUser().get()).isEqualTo("tom");
    assertThat(uri.getPassword().get()).isEqualTo("ahawk");
    assertThat(uri.getHost()).isEqualTo("www.sapia.org");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getFragment().get()).isEqualTo("intro");
    assertThat(uri.getQueryString().getParameterValue("p1")).isEqualTo("v1");
    assertThat(uri.getQueryString().getParameterValue("p2")).isEqualTo("v2");
    assertThat(uri.getQueryString().getParameterValue("p3")).isEqualTo("");
  }

  @Test
  public void testParse_http_uri_without_fragment() {
    Uri uri = Uri.parse(HTTP_URI_INPUT_WITHOUT_FRAGMENT);

    assertThat(uri.getScheme()).isEqualTo("http");
    assertThat(uri.getUser().get()).isEqualTo("tom");
    assertThat(uri.getPassword().get()).isEqualTo("ahawk");
    assertThat(uri.getHost()).isEqualTo("www.sapia.org");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("/index.html");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().getParameterValue("p1")).isEqualTo("v1");
    assertThat(uri.getQueryString().getParameterValue("p2")).isEqualTo("v2");
    assertThat(uri.getQueryString().getParameterValue("p3")).isEqualTo("");
  }

  @Test
  public void testParse_file_uri() {
    Uri uri = Uri.parse("file:/etc/file.txt");

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testParse_file_uri_multi_slash() {
    Uri uri = Uri.parse("file:/etc/file.txt");

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testParse_windows_file_uri() {
    Uri uri = Uri.parse("file:/C:/etc/file.txt");

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("C:/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testParse_windows_multi_slash_file_uri() {
    Uri uri = Uri.parse("file:///C:/etc/file.txt");

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("C:/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testToString_full_http_uri() {
    Uri uri = Uri.parse(Uri.parse(FULL_HTTP_URI_INPUT).toString());

    assertThat(uri.getScheme()).isEqualTo("http");
    assertThat(uri.getUser().get()).isEqualTo("tom");
    assertThat(uri.getPassword().get()).isEqualTo("ahawk");
    assertThat(uri.getHost()).isEqualTo("www.sapia.org");
    assertThat(uri.getPort()).isEqualTo(80);
    assertThat(uri.getPath()).isEqualTo("/index.html");
    assertThat(uri.getFragment().get()).isEqualTo("intro");
    assertThat(uri.getQueryString().getParameterValue("p1")).isEqualTo("v1");
    assertThat(uri.getQueryString().getParameterValue("p2")).isEqualTo("v2");
    assertThat(uri.getQueryString().getParameterValue("p3")).isEqualTo("");
  }

  @Test
  public void testToString_file_uri() {
    Uri uri = Uri.parse(Uri.parse("file:/etc/file.txt").toString());

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testToString_multi_slash_file_uri() {
    Uri uri = Uri.parse(Uri.parse("file:///etc/file.txt").toString());

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testToString_windows_file_uri() {
    Uri uri = Uri.parse(Uri.parse("file:/C:/etc/file.txt").toString());

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("C:/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testToString_windows_multi_slash_file_uri() {
    Uri uri = Uri.parse(Uri.parse("file:///C:/etc/file.txt").toString());

    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(uri.getUser().isPresent()).isFalse();
    assertThat(uri.getPassword().isPresent()).isFalse();
    assertThat(uri.getHost()).isEqualTo("");
    assertThat(uri.getPort()).isEqualTo(Uri.UNDEFINED_PORT);
    assertThat(uri.getPath()).isEqualTo("C:/etc/file.txt");
    assertThat(uri.getFragment().isPresent()).isFalse();
    assertThat(uri.getQueryString().isEmpty()).isTrue();
  }

  @Test
  public void testWithScheme() {
    Uri uri = Uri.parse("http://www.sapia.org").withScheme("https");

    assertThat(uri.getScheme()).isEqualTo("https");
  }

  @Test
  public void testWithHost() {
    Uri uri = Uri.parse("http://www.sapia-oss.org").withHost("sapia.org");

    assertThat(uri.getHost()).isEqualTo("sapia.org");
  }

  @Test
  public void testWithPort() {
    Uri uri = Uri.parse("http://www.sapia.org").withPort(8080);

    assertThat(uri.getPort()).isEqualTo(8080);
  }

  @Test
  public void testWithUser() {
    Uri uri = Uri.parse("http://tom:ahawk@www.sapia.org").withUser("test");
    assertThat(uri.getUser().get()).isEqualTo("test");
  }

  @Test
  public void testWithPassword() {
    Uri uri = Uri.parse("http://tom:ahawk@www.sapia.org").withPassword("test");
    assertThat(uri.getPassword().get()).isEqualTo("test");
  }

  @Test
  public void testWithPath() {
    Uri uri = Uri.parse("http://@www.sapia.org/index.html").withPath("/home.html");
    assertThat(uri.getPath()).isEqualTo("/home.html");
  }

  @Test
  public void testWithQueryString() {
    QueryString qs = QueryString.builder().param("p1", "v1").build();
    Uri uri = Uri.parse("http://@www.sapia.org/index.html").withQueryString(qs);
    assertThat(uri.getQueryString().getParameterValue("p1")).isEqualTo("v1");
  }

  @Test
  public void testWithFragment() {
    Uri uri = Uri.parse("http://@www.sapia.org/index.html").withFragment("intro");
    assertThat(uri.getFragment().get()).isEqualTo("intro");
  }

  @Test(expected = UriSyntaxException.class)
  public void testParse_uri_without_scheme() {
    Uri.parse("/etc/file.txt");
  }

  @Test(expected = UriSyntaxException.class)
  public void testParse_uri_with_port_truncated() {
    Uri.parse("http://www.sapia.org:/");
  }

  @Test(expected = UriSyntaxException.class)
  public void testParse_uri_with_port_not_number() {
    Uri.parse("http://www.sapia.org:test");
  }

  @Test(expected = UriSyntaxException.class)
  public void testParse_uri_with_incomplete_query_string() {
    Uri.parse("http://www.sapia.org?p1");
  }

  @Test(expected = UriSyntaxException.class)
  public void testParse_uri_with_wrong_param_delimiter() {
    Uri.parse("http://www.sapia.org?p1&");
  }

  @Test(expected = UriSyntaxException.class)
  public void testParse_uri_with_wrong_param_delimiter_after_value() {
    Uri.parse("http://www.sapia.org?p1=v1?p2=v2");
  }
  
  @Test(expected = UriSyntaxException.class)
  public void testParse_no_scheme_uri() {
	Uri.parse(NO_SCHEME_URI);
  }
  
  @Test
  public void testParse_uri_with_ip_address() {
    Uri.parse(URI_WITH_IP_ADDRESS);
  }
  
  @Test
  public void testSerialization() throws Exception {
    Uri original = Uri.parse("http://www.sapia.org");
    Uri copy     = (Uri) Serialization.deserialize(Serialization.serialize(original));
    
    assertThat(copy).isEqualTo(original);
  }
  
  @Test
  public void testAppend() {
	Uri actual   = Uri.parse("http://www.sapia.org");
	Uri expected = Uri.parse("http://www.sapia.org?n1=v1");
	
	actual = actual.append(QueryString.builder().param("n1", "v1").build());
	
	assertThat(actual).isEqualTo(expected);
  }
  
  @Test
  public void testParse_camel_uri() {
    Uri camelUri = Uri.parse("vm://in?multipleConsumers=true");
    assertThat(camelUri.toString()).isEqualTo("vm://in?multipleConsumers=true");
    assertThat(camelUri.getScheme()).isEqualTo("vm");
    assertThat(camelUri.getHost()).isEqualTo("in");

  }
}
