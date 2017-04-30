package com.encens.khipus.dashboard.component.totalizer;

import com.encens.khipus.dashboard.component.factory.DashboardObject;

/**
 * @author
 * @version 2.7
 */
public interface Totalizer<T extends DashboardObject> {
    void calculate(T instance);

    void initialize();
}
