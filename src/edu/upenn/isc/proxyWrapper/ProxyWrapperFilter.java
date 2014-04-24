/**
 * @author mchyzer
 * $Id: ProxyWrapperFilter.java,v 1.3 2014/04/23 06:37:18 mchyzer Exp $
 */
package edu.upenn.isc.proxyWrapper;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 */
public class ProxyWrapperFilter implements Filter {

  /**
   * need this in the reponse to know if we are proxying
   */
  private static InheritableThreadLocal<ProxyServletRequestWrapper> requestThreadLocal = new InheritableThreadLocal<ProxyServletRequestWrapper>();
  
  /** if we should print debug info to stdout */
  private static boolean debug = false;


  
  /**
   * if we should print debug info to stdout
   * @return the debug
   */
  public static boolean isDebug() {
    return debug;
  }

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
  }

  /**
   * get the request
   * @return the request
   */
  public static ProxyServletRequestWrapper retrieveProxyServletRequestWrapper() {
    return requestThreadLocal.get();
  }
  
  
  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    ProxyServletRequestWrapper proxyServletRequestWrapper = new ProxyServletRequestWrapper((HttpServletRequest)servletRequest);
    requestThreadLocal.set(proxyServletRequestWrapper);
    try {
      ProxyServletResponseWrapper proxyServletResponseWrapper = new ProxyServletResponseWrapper((HttpServletResponse)servletResponse);
      
      filterChain.doFilter(proxyServletRequestWrapper, proxyServletResponseWrapper);
    } finally {
      requestThreadLocal.remove();
    }
  }

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // getServletContext().getInitParameter("proxyDebug");
    //
    // <context-param>
    //      <param-name>proxyDebug</param-name>
    //      <param-value>true</param-value>
    //</context-param>

    String proxyDebugString = filterConfig.getServletContext().getInitParameter("proxyDebug");
    if ("true".equals(proxyDebugString)) {
      debug = true;
    }
  }

}
