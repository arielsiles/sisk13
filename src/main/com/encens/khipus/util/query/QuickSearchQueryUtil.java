package com.encens.khipus.util.query;

import com.encens.khipus.framework.action.EntityQuery;

import java.util.List;

/**
 * @author
 * @version 2.20
 */
public class QuickSearchQueryUtil {
    public static final QuickSearchQueryUtil i = new QuickSearchQueryUtil();

    private QuickSearchQueryUtil() {
    }

    public EntityQuery createEntityQuery(Class entityClass, String entityAlias, List<String> restrictions) {
        String ejbQl = "SELECT " + entityAlias + " FROM " + entityClass.getName() + " " + entityAlias;

        return EntityQueryFactory.createQuery(ejbQl, restrictions.toArray(new String[0]));
    }

    public EntityQuery createEntityQuery(Class entityClass, String entityAlias, String basicRestriction, List<String> restrictions) {
        String ejbQl = "SELECT " + entityAlias + " FROM " + entityClass.getName() + " " + entityAlias + " WHERE " + basicRestriction;
        return EntityQueryFactory.createQuery(ejbQl, restrictions.toArray(new String[0]));
    }

    public String getEntityAlias(Class entityClass) {
        return processClassName(entityClass.getName());
    }

    private String processClassName(String className) {
        String instanceName = className.substring(className.lastIndexOf('.') + 1);
        String firstLetter = instanceName.substring(0, 1);
        String residual = instanceName.substring(1);

        return firstLetter.toLowerCase() + residual;
    }
}
