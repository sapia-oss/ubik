package org.sapia.ubik.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This class models a query string. A query string has a format similar
 * to the following:
 *
 * <pre>
 *   /some/path?name1=value1&name2=value2
 * </pre>
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class QueryString {
  private String _path       = "/";
  private Map    _properties = new HashMap();

  /**
   * Constructor for QueryString.
   */
  QueryString() {
  }

  /**
   * Constructor for QueryString.
   *
   * This constructor takes the path of the query string.
   *
   * @param a path
   */
  public QueryString(String path) {
    _path = path;
  }

  /**
   * Returns this instance's path.
   *
   * @return a path.
   */
  public String getPath() {
    return _path;
  }

  /**
   * Returns this instance's parameters.
   *
   * @return a <code>Map</code> containing name/value pairs.
   */
  public Map getParameters() {
    return _properties;
  }

  /**
   * Adds the passed in name/value pair as parameter.
   *
   * @param name an object attribute name
   * @param value an object attribute value.
   */
  public void addParameter(String name, String value) {
    _properties.put(name, value);
  }

  /**
   * Returns the value for the parameter with the passed
   * in name.
   *
   * @param the name of the parameter whose value should be returned.
   * @return the value of the given parameter, or <code>null</code>
   * if no such value exists.
   */
  public String getParameter(String name) {
    return (String) _properties.get(name);
  }

  /**
   * Sets this instance's path.
   *
   * @param path a path.
   */
  void setPath(String path) {
    _path = path;
  }

  /**
   * Parses a query string and returns its object representation.
   *
   * @return a <code>QueryString</code>
   */
  public static QueryString parse(String queryStr) {
    QueryStringParser p = new QueryStringParser();

    return p.parseQueryString(queryStr);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(_path);

    if (_properties.size() > 0) {
      Map.Entry entry;
      buf.append(QueryStringParser.QMARK);

      Iterator itr   = _properties.entrySet().iterator();
      int      count = 0;

      while (itr.hasNext()) {
        entry = (Map.Entry) itr.next();

        if (count > 0) {
          buf.append(QueryStringParser.AMP);
        }

        buf.append(entry.getKey().toString()).append(QueryStringParser.EQ)
           .append(entry.getValue());
        count++;
      }
    }

    return buf.toString();
  }
}
