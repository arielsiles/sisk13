package com.encens.khipus.util.query;

import com.encens.khipus.util.ValidatorUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The QueryBuilder class manage a query result set.
 * This class could be used directly as a JPQL generator as well as used
 * to get single result or result list from JPQL. that has been configured.
 *
 * @author
 * @version 3.5
 */
public class QueryBuilder {
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\s(where)\\s", Pattern.CASE_INSENSITIVE);
    private static final String WHERE_OPERATOR = "where";
    private String jpql;
    private String orderBy;
    private String groupBy;
    private List<ParameterRestriction> restrictions = new ArrayList<ParameterRestriction>(0);

    public static QueryBuilder createQuery(String jpql) {
        return new QueryBuilder(jpql);
    }

    public static Param param(String name, Object value) {
        return new Param(name, value);
    }

    public QueryBuilder(String jpql) {
        this.jpql = jpql;
    }

    public QueryBuilder addOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public QueryBuilder addGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public QueryBuilder addRestriction(String condition, Param... value) {
        restrictions.add(new QueryBuilder.ParameterRestriction(condition, value));
        return this;
    }

    public QueryBuilder addRestrictionOperator(String condition, QueryBuilder.LogicOperator logicOperator, Param... value) {
        restrictions.add(new QueryBuilder.ParameterRestriction(condition, logicOperator, value));
        return this;
    }

    public List<ParameterRestriction> getRestrictions() {
        return restrictions;
    }

    private Query buildQuery(EntityManager entityManager, QueryBuilder... subQueries) {
        Query queryResult = entityManager.createQuery(getRenderedJPQL());

        for (QueryBuilder.ParameterRestriction restriction : restrictions) {
            restriction.addParameters(queryResult);
        }

        if (!ValidatorUtil.isEmptyOrNull(subQueries)) {
            for (QueryBuilder queryBuilder : subQueries) {
                for (QueryBuilder.ParameterRestriction restriction : queryBuilder.getRestrictions()) {
                    restriction.addParameters(queryResult);
                }
            }
        }

        return queryResult;
    }

    public List getResultList(EntityManager entityManager, QueryBuilder... subQueries) {
        return buildQuery(entityManager, subQueries).getResultList();
    }

    public <T> List<T> getResultList(EntityManager entityManager, Class<T> clazz, QueryBuilder... subQueries) {
        return buildQuery(entityManager, subQueries).getResultList();
    }

    public Object getSingleResult(EntityManager entityManager, QueryBuilder... subQueries) {
        return buildQuery(entityManager, subQueries).getSingleResult();
    }

    public <T> T getSingleResult(EntityManager entityManager, Class<T> clazz, QueryBuilder... subQueries) {
        return (T) buildQuery(entityManager, subQueries).getSingleResult();
    }

    public String getRenderedJPQL() {
        StringBuilder builder = new StringBuilder().append(jpql);

        for (QueryBuilder.ParameterRestriction restriction : restrictions) {
            if (restriction.getActive()) {
                if (WHERE_PATTERN.matcher(builder).find()) {
                    builder.append(" ").append(restriction.getLogicOperator()).append(" ");
                } else {
                    builder.append(" ").append(WHERE_OPERATOR).append(" ");
                }
                builder.append(restriction.getCondition());
            }
        }

        if (groupBy != null) {
            builder.append(" group by ").append(groupBy);
        }

        if (orderBy != null) {
            builder.append(" order by ").append(orderBy);
        }

        return builder.toString();
    }

    enum LogicOperator {
        AND, OR
    }

    private class ParameterRestriction {

        private String condition;
        private QueryBuilder.LogicOperator logicOperator = QueryBuilder.LogicOperator.AND;
        private Param[] params;
        private Boolean active;

        ParameterRestriction(String condition, Param... params) {
            this.condition = condition;
            this.params = params;
            setCurrentActiveState();
        }

        ParameterRestriction(String condition, QueryBuilder.LogicOperator logicOperator, Param... params) {
            this.condition = condition;
            this.logicOperator = logicOperator;
            this.params = params;
            setCurrentActiveState();
        }

        public String getCondition() {
            return condition;
        }

        public QueryBuilder.LogicOperator getLogicOperator() {
            return logicOperator;
        }

        public Boolean getActive() {
            return active;
        }

        public void addParameters(Query query) {
            if (active) {
                for (int i = 0; i < params.length; i++) {
                    query.setParameter(params[i].getName(), params[i].getValue());
                }
            }
        }

        public void setCurrentActiveState() {
            active = true;
            if (params == null) {
                active = false;
            } else {
                for (int i = 0; i < params.length && active; i++) {
                    active = !(params[i] == null || params[i].getValue() == null);
                }
            }
        }

    }
}