package com.encens.khipus.dashboard.component.factory;

import com.encens.khipus.dashboard.component.totalizer.Totalizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author
 * @version 2.6
 */
public class ComponentFactory<T extends DashboardObject, U extends Totalizer<T>> {
    private SqlQuery sqlQuery;

    private InstanceFactory<T> instanceFactory;

    private LinkedHashMap<Object, T> cache = new LinkedHashMap<Object, T>();

    private U totalizer;

    public ComponentFactory(SqlQuery sqlQuery, InstanceFactory<T> instanceFactory, U totalizer) {
        this.sqlQuery = sqlQuery;
        this.instanceFactory = instanceFactory;
        this.totalizer = totalizer;
    }

    public List<T> getResultList(List data) {
        List<T> result = new ArrayList<T>();

        if (null != data && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                Object[] row = (Object[]) data.get(i);
                buildInstance(row);
            }

            result.addAll(cache.values());

            clearCache();
        }

        return result;
    }

    public SqlQuery getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(SqlQuery sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public InstanceFactory<T> getInstanceFactory() {
        return instanceFactory;
    }

    public U getTotalizer() {
        return totalizer;
    }

    protected void processInstance(T instance) {

    }

    private T buildInstance(Object[] row) {
        T cachedObject = getCachedObject(row);

        T instance = instanceFactory.createInstance(cachedObject, row);
        processInstance(instance);

        if (null != totalizer) {
            totalizer.calculate(instance);
        }

        updateCache(instance);
        return instance;
    }

    private T getCachedObject(Object[] row) {
        Object key = instanceFactory.getIdentifierValue(row);
        if (null == key) {
            throw new IllegalArgumentException("The identifier cannot be a null object.");
        }

        return cache.get(key);
    }

    private void updateCache(T t) {
        cache.put(t.getIdentifier(), t);
    }

    private void clearCache() {
        cache.clear();
    }
}
