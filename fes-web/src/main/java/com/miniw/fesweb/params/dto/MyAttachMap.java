package com.miniw.fesweb.params.dto;

import java.util.HashMap;

/**
 * 自定义map
 *
 * @author luoquan
 * @date 2021/09/07
 */
public class MyAttachMap extends HashMap<Integer, Integer> {

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public Integer put(Integer key, Integer value) {
        Integer newV = value;
        if (containsKey(key)) {
            Integer oldV = get(key);
            newV += oldV;
        }
        return super.put(key, newV);
    }

}
