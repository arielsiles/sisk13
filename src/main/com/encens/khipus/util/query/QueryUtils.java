package com.encens.khipus.util.query;

import com.encens.khipus.util.ValidatorUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * QueryUtils
 *
 * @author
 * @version 2.2
 */
public class QueryUtils {
    private QueryUtils() {
    }

    private static String SELECT_ALL = "select entity from <entityName> entity";
    private static String IN_CONDITION = " where entity.id in (<conditionParams>)";

    public static Query selectAll(EntityManager entityManager, Class entityClass) {
        return selectAllIn(entityManager, entityClass, null);
    }

    public static Query selectAllIn(EntityManager entityManager, Class entityClass, List inParameters) {
        String ejbql = SELECT_ALL.replaceAll("<entityName>", entityClass.getSimpleName());
        if (!ValidatorUtil.isEmptyOrNull(inParameters)) {
            ejbql += IN_CONDITION.replaceAll("<conditionParams>", toQueryParameter(inParameters));
        }
        return entityManager.createQuery(ejbql);
    }

    public static String removeInvalidCharacters(String parameterValue) {
        if (!ValidatorUtil.isBlankOrNull(parameterValue)) {
            return parameterValue.replaceAll("[\\[-\\]]", "").replaceAll("[\\(-\\)]", "").trim();
        }
        return parameterValue;
    }

    public static String toQueryParameter(Collection collection) {
        if (!ValidatorUtil.isEmptyOrNull(collection)) {
            return removeInvalidCharacters(collection.toString());
        }
        return "0";
    }

}
