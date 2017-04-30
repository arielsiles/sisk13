package com.encens.khipus.util;

import javax.lang.model.type.TypeVariable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * MapUtil
 *
 * @author
 * @version 2.2
 */
public class MapUtil {
    private MapUtil() {
    }

    public static <K, V> V getNotNullValue(Map<K, V> map, K key, V currentValue) {
        currentValue = currentValue == null ? newValueInstance(map) : currentValue;
        map.put(key, currentValue);
        return currentValue;
    }

    public static <K, V> V newValueInstance(Map<K, V> map) {
        try {
            return getValueClass(map).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <K, V> Class<V> getValueClass(Map<K, V> map) {
        Class<V> valueClass = null;
        Type type = map.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            if (paramType.getActualTypeArguments()[1] instanceof Class) {
                valueClass = (Class<V>) paramType.getActualTypeArguments()[1];
            } else if (paramType.getActualTypeArguments()[1] instanceof TypeVariable) {
                throw new IllegalArgumentException("Could not guess entity class by reflection");
            }

        }
        return valueClass;
    }

    public static Map createByDefaultValue(Collection collection, Object defaultValue) {
        Map resultMap = new HashMap();
        for (Object key : collection) {
            resultMap.put(key, defaultValue);
        }
        return resultMap;

    }
}
