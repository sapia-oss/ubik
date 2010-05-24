package org.sapia.ubik.net;


/**
 * This class models a URI.
 * <p>
 * Usage:
 *
 * <pre>
 *  Uri httpURI = Uri.parse("http://www.sapia-oss.org:80/index.html");
 *
 *  // will print: http
 *  System.out.println(httpURI.getScheme());
 *
 *  // will print: www.sapia-oss.org
 *  System.out.println(httpURI.getHost());
 *
 *  // will print: 80
 *  System.out.println(httpURI.getPort());
 *
 *  // will print: /index.html
 *  System.out.println(httpURI.getPath());
 *
 *  Uri fileURI = Uri.parse("file:/some/directory/foo.txt");
 *
 *  // will print: file
 *  System.out.println(fileURI.getScheme());
 *
 *  // these calls don't make sense:
 *  System.out.println(fileURI.getHost());
 *  System.out.println(fileURI.getHost());
 *
 *  // will print: /some/directory/foo.txt
 *  System.out.println(fileURI.getPath());
 *
 * </pre>
 *
 *
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Uri {
  public static final int                UNDEFINED_PORT = -1;
  public static final String             UNDEFINED_HOST = "";
  private static final QueryStringParser PARSER         = new QueryStringParser();
  private static final String            PROTO          = "://";
  private static final char              COLON          = ':';
  private static final char              SLASH          = '/';
  private String                         _scheme;
  private String                         _host;
  private QueryString                    _query;
  private int                            _port          = UNDEFINED_PORT;

  /**
   * Constructor for UrlParser.
   */
  private Uri() {
  }

  public Uri(String scheme, String host, int port, String path) {
    _scheme   = scheme;
    _host     = host;
    _port     = port;
    _query    = new QueryString("");
  }

  /**
   * Returns the path of this URI.
   *
   * @return a path.
   */
  public String getPath() {
    return _query.getPath();
  }

  /**
   * Returns the scheme of this URI.
   *
   * @return a scheme.
   */
  public String getScheme() {
    return _scheme;
  }

  /**
   * Returns the host of this URI.
   *
   * @return a host - if no host was specified, the returned value
   * corresponds to the UNDEFINED_HOST constant of this class.
   */
  public String getHost() {
    return _host;
  }

  /**
   * Returns the port of this URI.
   *
   * @return a port - if no port was specified, the returned value
   * corresponds to the UNDEFINED_PORT constant of this class.
   */
  public int getPort() {
    return _port;
  }

  /**
   * Sets this instance's scheme.
   *
   * @param a scheme.
   */
  public void setScheme(String scheme) {
    _scheme = scheme;
  }

  /**
   * Sets this instance's host.
   *
   * @param a host identifier.
   */
  public void setHost(String host) {
    _host = host;
  }

  /**
   * Sets this instance's port.
   *
   * @param a port.
   */
  public void setPort(int port) {
    _port = port;
  }

  /**
   * Returns this instance's query string.
   *
   * @return a <code>QueryString</code>, or <code>null</code>
   * if this instance has no query string.
   */
  public QueryString getQueryString() {
    return _query;
  }

  /**
   * Returns this instance's string format.
   *
   * @return a <code>String</code>.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer(_scheme);

    if ((_host != null) && !_host.equals(UNDEFINED_HOST)) {
      buf.append(PROTO).append(_host);

      if (_port != UNDEFINED_PORT) {
        buf.append(COLON).append(_port);
      }
    } else {
      buf.append(COLON);
    }

    buf.append(_query.toString());

    return buf.toString();
  }

  /**
   * Parses the given URI string and returns its object
   * representation.
   *
   * @return a <code>Uri</code>.
   */
  public static Uri parse(String uriStr) throws UriSyntaxException {
    Uri uri = new Uri();

    parseUrl(uri, uriStr);

    return uri;
  }

  void setQueryString(QueryString str) {
    _query = str;
  }

  private static void parseUrl(Uri url, String str) throws UriSyntaxException {
    int idx = str.indexOf(PROTO);

    if (idx < 0) {
      url.setHost(UNDEFINED_HOST);
      idx = str.indexOf(COLON);

      if (idx < 0) {
        url.setScheme(str);
        url.setQueryString(new QueryString(""));
      } else {
        url.setScheme(str.substring(0, idx));
        url.setQueryString(PARSER.parseQueryString(str.substring(idx + 1)));
      }
    } else {
      String proto = str.substring(0, idx);

      if (proto.length() == 0) {
        throw new UriSyntaxException("Empty protocol in URI: " + str);
      }

      url.setScheme(proto);

      parseHost(url, str.substring(idx + PROTO.length()));
    }
  }

  /* expects: host:port/name?prop1=value1... */
  private static void parseHost(Uri url, String str) throws UriSyntaxException {
    int idx = str.indexOf(COLON);

    if (idx >= 0) {
      String host = str.substring(0, idx);

      if (host.length() == 0) {
        url.setHost(UNDEFINED_HOST);

        //throw new UriSyntaxException("Host empty in URI: " + str);
      } else {
        url.setHost(host);
        parsePort(url, str.substring(idx + 1));
      }
    } else {
      idx = str.indexOf(SLASH);

      String host;

      if (idx < 0) {
        host = str; //str.substring(0);      

        //        throw new UriSyntaxException("Expected '/' after host in URI " + str);
      } else {
        host = str.substring(0, idx);
      }

      if (host.length() == 0) {
        url.setHost(UNDEFINED_HOST);

        //throw new UriSyntaxException("Host empty in URI: " + str);
      } else {
        url.setHost(host);
      }

      if (idx > 0) {
        String qString = str.substring(idx);
        url.setQueryString(PARSER.parseQueryString(qString));
      } else {
        url.setQueryString(new QueryString(""));
      }
    }
  }

  private static void parsePort(Uri url, String str) throws UriSyntaxException {
    int    idx = str.indexOf(SLASH);

    String port;

    if (idx < 0) {
      port = str.substring(0);
    } else {
      port = str.substring(0, idx);
    }

    if (port.length() == 0) {
      throw new UriSyntaxException("Port expected but not specified in URI: " +
        str);
    }

    try {
      url.setPort(Integer.parseInt(port));
    } catch (NumberFormatException e) {
      throw new UriSyntaxException("Port is not a valid number: " + str);
    }

    if (idx > 0) {
      String qString = str.substring(idx);
      url.setQueryString(PARSER.parseQueryString(qString));
    } else {
      url.setQueryString(new QueryString(""));
    }
  }
}
