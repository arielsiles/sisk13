package com.encens.khipus.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

/**
 * ObservableMap
 *
 * @author
 * @version 2.2
 */
public class ObservableMap<K, V> extends HashMap<K, V> {
    private ObserverMonitor observerMonitor = new ObserverMonitor();
    private Object observerValueId;

    public ObservableMap() {
    }

    public ObservableMap(Object observerValueId) {
        this.observerValueId = observerValueId;
    }

    public ObservableMap(Object observerValueId, Observer... observers) {
        this.observerValueId = observerValueId;
        addObservers(observers);
    }

    @Override
    public V put(K key, V value) {
        V oldValue = super.put(key, value);
        observerMonitor.startNotify(new ObserverEntry<K, V>(getObserverValueId(), key, value));
        return oldValue;
    }

    public void notifyObservers() {
        for (Map.Entry<K, V> entry : entrySet()) {
            observerMonitor.startNotify(new ObserverEntry<K, V>(getObserverValueId(), entry.getKey(), entry.getValue()));
        }
    }

    public void addObservers(Observer... observers) {
        if (observers != null) {
            for (Observer observer : observers) {
                observerMonitor.addObserver(observer);
            }
        }
    }

    public Object getObserverValueId() {
        return observerValueId;
    }

    public void setObserverValueId(Object observerValueId) {
        this.observerValueId = observerValueId;
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static class ObserverEntry<K, V> implements Map.Entry<K, V>, java.io.Serializable {
        private static final long serialVersionUID = -8499721149061103585L;

        private final K key;
        private V value;
        private Object observerValueId;

        public ObserverEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public ObserverEntry(Object observerValueId, K key, V value) {
            this.observerValueId = observerValueId;
            this.key = key;
            this.value = value;
        }

        public ObserverEntry(Map.Entry<? extends K, ? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public ObserverEntry(Object observerValueId, Map.Entry<? extends K, ? extends V> entry) {
            this.observerValueId = observerValueId;
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public Object getObserverValueId() {
            return observerValueId;
        }

        public void setObserverValueId(Object observerValueId) {
            this.observerValueId = observerValueId;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
        }


        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^
                    (value == null ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }
    }

}
