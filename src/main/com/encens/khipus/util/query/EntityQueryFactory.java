package com.encens.khipus.util.query;

import com.encens.khipus.framework.action.EntityQuery;

import java.util.Arrays;

/**
 * EntityQueryFactory
 *
 * @author
 * @version 2.7
 */
public class EntityQueryFactory {
    private EntityQueryFactory() {
    }

    public static EntityQuery createQuery(String ejbql, String[] restrictions) {
        EntityQuery entityQuery = new EntityQuery();
        entityQuery.setEjbql(ejbql);
        entityQuery.setRestrictionExpressionStrings(Arrays.asList(restrictions));
        return entityQuery;
    }

    public static EntityQuery createQuery(String ejbql, String[] restrictions, String order) {
        EntityQuery entityQuery = createQuery(ejbql, restrictions);
        entityQuery.setOrder(order);
        return entityQuery;
    }

    public static EntityQuery createQuery(String ejbql, String[] restrictions, String order, String groupBy) {
        EntityQuery entityQuery = createQuery(ejbql, restrictions, order);
        entityQuery.setGroupBy(groupBy);
        return entityQuery;
    }
}
