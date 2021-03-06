package edu.upenn.isc.proxyWrapper.util;

import java.io.Serializable;


/**
 * This holds the actual value of the map, and the time it was inserted, and
 * the time that it should last in the cache
 * @version $Id: ExpirableValue.java,v 1.1 2011/05/06 15:24:00 mchyzer Exp $
 * @author mchyzer
 * @param <T> is the type of the underlying content
 */
public class ExpirableValue<T> implements Serializable {

  /** this is the time it was placed in the cache */
  private long timePlacedInCache = System.currentTimeMillis();
  
  /** the time to live is by default 1 day */
  private long timeToLiveInCacheMillis = ExpirableCache.MAX_TIME_TO_LIVE_MILLIS;
  
  /** underlying content */
  private T content = null;
  
  /**
   * Makes an expirable value with max 1 day time to live
   * @param theContent content to store
   * @param theTimeToLiveInCacheMillis number of millis the items should stay in cache.
   * this cannot be longer than 1 day
   */
  ExpirableValue(T theContent, long theTimeToLiveInCacheMillis) {
    super();
    //cant be longer then the max
    if (theTimeToLiveInCacheMillis > 0 && 
        theTimeToLiveInCacheMillis <= ExpirableCache.MAX_TIME_TO_LIVE_MILLIS) {
      this.timeToLiveInCacheMillis = theTimeToLiveInCacheMillis;
    }
    this.content = theContent;
  }

  /**
   * dont call this on expired content!  check first.  get the content
   * @return Returns the content.
   */
  T getContent() {
    if(this.expiredLongTime()) {
      throw new RuntimeException("This content is expired!");
    }
    return this.content;
  }

  
  /**
   * see if the content is expired
   * @return true if expired
   */
  boolean expired() {
    return System.currentTimeMillis() - this.timePlacedInCache > this.timeToLiveInCacheMillis;
  }
  
  /**
   * see if the content is expired 3 seconds ago, to eliminate race conditions
   * @return true if expired
   */
  boolean expiredLongTime() {
    return (System.currentTimeMillis() - 3000) - this.timePlacedInCache > this.timeToLiveInCacheMillis;
  }
}
