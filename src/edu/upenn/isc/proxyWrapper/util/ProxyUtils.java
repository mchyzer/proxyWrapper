/**
 * @author mchyzer
 * $Id: ProxyUtils.java,v 1.2 2014/01/29 18:26:45 mchyzer Exp $
 */
package edu.upenn.isc.proxyWrapper.util;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class ProxyUtils {

  /**
   * string equality null safe
   * @param a
   * @param b
   * @return true if equal
   */
  public static boolean equals (String a, String b) {
    if (a==b) {
      return true;
    }
    if (a==null || b==null) {
      return false;
    }
    return a.equals(b);
  }
  
  /**
   * string equality null safe case insensitive
   * @param a
   * @param b
   * @return true if equal ignore case
   */
  public static boolean equalsIgnoreCase (String a, String b) {
    if (a==b) {
      return true;
    }
    if (a==null || b==null) {
      return false;
    }
    return a.equalsIgnoreCase(b);
  }
  
  /**
   * split trim a string by delimiter
   * @param string
   * @param regex
   * @return list
   */
  public static List<String> splitTrim(String string, String regex) {
    List<String> result = new ArrayList<String>();
    if (string != null) {
      String[] stringArray = string.split(regex);
      if (stringArray != null) {
        for (String entry : stringArray) {
          result.add(entry.trim());
        }
      }
    }
    return result;
  }
  
  /**
   * see if string is blank
   * @param string
   * @return true if blank
   */
  public static boolean isBlank(String string) {
    return string == null || "".equals(string.trim());
  }
  
}
