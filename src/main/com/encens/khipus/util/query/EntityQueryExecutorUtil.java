package com.encens.khipus.util.query;

import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.persistence.Filter;

import java.util.*;

/**
 * @author
 * @version 2.22
 */
public class EntityQueryExecutorUtil {
    private static final LogProvider log = Logging.getLogProvider(EntityQueryExecutorUtil.class);

    public static EntityQueryExecutorUtil i = new EntityQueryExecutorUtil();

    private EntityQueryExecutorUtil() {

    }

    /**
     * Execute the <code>EntityQuery</code> that is defined in the <code>components.xml</code> file under name
     * <code>queryName</code>.
     *
     * @param queryName              The query name.
     * @param applicationFilterNames The filter names that will be enabled to execute the query, all names
     *                               should be registered in the main <code>components.xml</code> configuration file.
     * @return A <code>List</code> object that is the result of the execution the <code>EntityQuery</code> object.
     */
    public List executeNamedQuery(String queryName, List<String> applicationFilterNames) {
        EntityQuery entityQuery = (EntityQuery) Component.getInstance(queryName);

        return execute(entityQuery, applicationFilterNames, getApplicationFilterValues(applicationFilterNames));
    }

    /**
     * Execute the <code>EntityQuery</code> that is defined in the <code>components.xml</code> file.
     *
     * @param entityQuery            <code>EntityQuery</code> to be executed.
     * @param applicationFilterNames The filter names that will be enabled to execute the query, all names
     *                               should be registered in the main <code>components.xml</code> configuration file.
     * @return A <code>List</code> object that is the result of the execution the <code>EntityQuery</code> object.
     */
    public List executeNamedQuery(EntityQuery entityQuery, List<String> applicationFilterNames) {
        return execute(entityQuery, applicationFilterNames, getApplicationFilterValues(applicationFilterNames));
    }

    private List execute(EntityQuery entityQuery,
                         List<String> applicationFilterNames,
                         Map<String, Map<String, Expressions.ValueExpression>> filterConfiguration) {
        org.hibernate.Session session = (Session) entityQuery.getEntityManager().getDelegate();
        List<org.hibernate.Filter> hibernateFilters = new ArrayList<org.hibernate.Filter>();

        for (String filterName : applicationFilterNames) {
            log.debug("Enabling filter: " + filterName);
            org.hibernate.Filter hibernateFilter = session.enableFilter(filterName);
            hibernateFilters.add(hibernateFilter);
        }

        for (org.hibernate.Filter hibernateFilter : hibernateFilters) {
            Map<String, Expressions.ValueExpression> parameters = filterConfiguration.get(hibernateFilter.getName());
            addParameters(hibernateFilter, parameters);
        }

        entityQuery.refresh();

        List result = entityQuery.getResultList();

        hibernateFilters.clear();
        for (String filterName : applicationFilterNames) {
            log.debug("Disabling filter: " + filterName);
            session.disableFilter(filterName);
        }

        entityQuery.refresh();
        return result;
    }

    private void addParameters(org.hibernate.Filter filter, Map<String, Expressions.ValueExpression> parameters) {
        Set<String> parameterNames = parameters.keySet();

        for (String parameterName : parameterNames) {
            Expressions.ValueExpression valueExpression = parameters.get(parameterName);
            Object value = valueExpression.getValue();
            if (value instanceof Collection) {
                filter.setParameterList(parameterName, (Collection) value);
            } else if (value instanceof Object[]) {
                filter.setParameterList(parameterName, (Object[]) value);
            } else {
                filter.setParameter(parameterName, value);
            }
        }
    }

    private Map<String, Map<String, Expressions.ValueExpression>> getApplicationFilterValues(List<String> applicationFilterNames) {
        Map<String, Map<String, Expressions.ValueExpression>> result =
                new HashMap<String, Map<String, Expressions.ValueExpression>>();

        for (String filterName : applicationFilterNames) {
            org.jboss.seam.persistence.Filter filter = (Filter) Component.getInstance(filterName);
            if (null != filter) {
                result.put(filterName, filter.getParameters());
            }
        }

        return result;
    }
}
