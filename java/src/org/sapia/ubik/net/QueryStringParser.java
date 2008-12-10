package org.sapia.ubik.net;

import java.util.*;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;


/**
 * Parses query strings to object representations. It can be used to
 * create <code>QueryString</code> or <code>Name</code> instances.
 * <p>
 * From this class' point of view, a query string/name is made of paths
 * delimited by '/'; it can optionally be followed by a list of name-value
 * pairs. The following is a valid query string:
 * <p>
 * <code>some/object?prop1=value1&prop2=value2</code>...
 *
 * @see org.sapia.ubik.net.QueryString
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class QueryStringParser implements NameParser {
  static final char   QMARK = '?';
  static final String AMP   = "&";
  static final char   EQ    = '=';
  static final String SLASH = "/";

  /**
   * Constructor for QueryStringParser.
   */
  public QueryStringParser() {
    super();
  }

  /**
   * Parses the given name string into a <code>QueryString</code> instance.
   *
   * @return a <code>QueryString</code>.
   */
  public QueryString parseQueryString(String string) {
    QueryString qs = new QueryString();

    if ((string == null) || (string.length() == 0)) {
      return qs;
    }

    parseName(qs, string);

    return qs;
  }

  /**
   * Parses the given string into an enumeration of paths.
   *
   * @param queryString a query string.
   *
   * @return an <code>Enumeration</code> containing the separate paths
   * that make the given query string; if the given name has trailing
   * name-value pairs has attributes, the latter are concatenated to the
   * last path. Thus, the following string:
   * <code>some/object?prop1=value1&prop2=value2</code> would have
   * <code>object?prop1=value1&prop2=value2</code> as its last path string.
   */
  public Enumeration parseNameTokens(String queryString) {
    Vector      tokens = new Vector();
    QueryString qs = new QueryString();

    parseName(qs, queryString);

    StringTokenizer st = new StringTokenizer(qs.getPath(), SLASH);

    while (st.hasMoreTokens()) {
      tokens.add(st.nextToken());
    }

    if (qs.getParameters().size() > 0) {
      StringBuffer buf   = new StringBuffer((String) tokens.lastElement());
      Map.Entry    entry;
      buf.append(QueryStringParser.QMARK);

      Iterator itr   = qs.getParameters().entrySet().iterator();
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

      tokens.set(tokens.size() - 1, buf.toString());
    }

    return tokens.elements();
  }

  /**
   * Parses the given query string into a <code>javax.naming.Name</code> instance.
   * The query string's parameters are part of the last element in the returned
   * name object.
   *
   * @return a <code>Name</code>.
   * @see #parseNameTokens(String)
   * @see javax.naming.NameParser#parse(String)
   */
  public Name parse(String queryString) throws NamingException {
    return new CompositeNameEx(parseNameTokens(queryString));
  }

  /**
   * Returns the string form of a <code>Name</code>.
   *
   * @return a name - as a string.
   */
  public String nameToString(Name n) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < n.size(); i++) {
      buf.append(n.get(i));

      if (i < (n.size() - 1)) {
        buf.append(SLASH);
      }
    }

    return buf.toString();
  }

  /* takes name?prop1=val1&prop2=val2&prop3=val3 */
  private void parseName(QueryString qs, String name) {
    int idx = name.indexOf(QMARK);

    if (idx < 0) {
      qs.setPath(name);

      return;
    }

    qs.setPath(name.substring(0, idx));

    if (idx == (name.length() - 1)) {
      return;
    }

    parseProperties(qs, name.substring(idx + 1));
  }

  /* takes prop1=val1&prop2=val2&prop3=val3 */
  private void parseProperties(QueryString tn, String props) {
    StringTokenizer st    = new StringTokenizer(props, AMP);
    String          token;

    while (st.hasMoreTokens()) {
      token = st.nextToken();
      parseProperty(tn, token);
    }
  }

  /* takes prop1=val1 */
  private void parseProperty(QueryString qs, String prop) {
    String name  = null;
    String value = null;

    int    idx = prop.indexOf(EQ);

    if (idx < 0) {
      if (prop.length() > 0) {
        name = prop;
      }
    } else {
      name = prop.substring(0, idx);

      if (idx != (prop.length() - 1)) {
        value = prop.substring(idx + 1);
      }
    }

    if ((name != null) && (value != null)) {
      qs.addParameter(name, value);
    }
  }

  /*//////////////////////////////////////////////////
                     INNER CLASSES
  //////////////////////////////////////////////////*/
  static final class CompositeNameEx extends CompositeName {
    protected CompositeNameEx(Enumeration en) {
      super(en);
    }
  }
}
