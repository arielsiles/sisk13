package com.encens.khipus.dashboard.component.factory;

/**
 * @author
 * @version 2.7
 */
public interface InstanceFactory<T extends DashboardObject> {
    T createInstance(T cachedObject, Object[] row);

    Object getIdentifierValue(Object[] row);
}
