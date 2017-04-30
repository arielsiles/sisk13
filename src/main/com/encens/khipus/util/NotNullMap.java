package com.encens.khipus.util;

import java.util.HashMap;

/**
 * NotNullMap
 *
 * @author
 * @version 2.26
 */
public class NotNullMap<K, V> extends HashMap<K, V> {
    @Override
    public V get(Object key) {
        return MapUtil.getNotNullValue(this, (K) key, super.get(key));
    }
}
