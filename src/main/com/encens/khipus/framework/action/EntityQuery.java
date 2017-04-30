package com.encens.khipus.framework.action;

import org.jboss.seam.persistence.QueryParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encens Team
 *
 * @author
 * @version 1.2.3
 */
public class EntityQuery<E> extends org.jboss.seam.framework.EntityQuery<E> {

    private static Pattern DISTINCT_PATTERN = Pattern.compile("(^|\\s)(distinct)\\s", Pattern.CASE_INSENSITIVE);
    private static Pattern SUBJECT_PATTERN = Pattern.compile("^select (\\w+((\\s+|\\.)\\w+)*)\\s+from", Pattern.CASE_INSENSITIVE);
    private static Pattern FROM_PATTERN = Pattern.compile("(^|\\s)(from)\\s", Pattern.CASE_INSENSITIVE);
    private static Pattern WHERE_PATTERN = Pattern.compile("\\s(where)\\s", Pattern.CASE_INSENSITIVE);
    private static Pattern ORDER_PATTERN = Pattern.compile("\\s(order)(\\s)+by\\s", Pattern.CASE_INSENSITIVE);
    private static Pattern GROUP_PATTERN = Pattern.compile("\\s(group)(\\s)+by\\s", Pattern.CASE_INSENSITIVE);
    private static Pattern ORDER_COLUMN_PATTERN = Pattern.compile("^\\w+(\\.\\w+)*$");
    private static String COUNT_EJBQL = "select count(<countProperty>) ";
    private static String FETCH_EJBQL = " fetch ";
    private String queryId = "";

    public EntityQuery() {
    }

    public EntityQuery(String queryId) {
        setQueryId(queryId);
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId.trim();
    }

    public QueryResult createQueryResult() {
        parseEjbql();
        evaluateAllParameters();
        joinTransaction();

        QueryResult queryResult = new QueryResult(getQueryResultEjbql());
        setParameters(queryResult, getQueryParameterValues(), 0);
        setParameters(queryResult, getRestrictionParameterValues(), getQueryParameterValues().size());
        return queryResult;
    }

    private String getQueryResultEjbql() {
        return getRenderedEjbql().replaceAll("\\:", ":" + getParameterNameSuffix());
    }

    private void setParameters(QueryResult queryResult, List<Object> parameters, int start) {
        for (int i = 0; i < parameters.size(); i++) {
            Object parameterValue = parameters.get(i);
            if (isRestrictionParameterSet(parameterValue)) {
                queryResult.setParameter(getParameterNameSuffix() + QueryParser.getParameterName(start + i), parameterValue);
            }
        }
    }

    public String getParameterNameSuffix() {
        return getQueryId() + "Param";
    }

    /**
     * Return the ejbql to used in a count query (for calculating number of
     * results)
     *
     * @return String The ejbql query
     */
    @Override
    protected String getCountEjbql() {
        String ejbql = getRenderedEjbql().replace(FETCH_EJBQL, " ");

        Matcher fromMatcher = FROM_PATTERN.matcher(ejbql);
        if (!fromMatcher.find()) {
            throw new IllegalArgumentException(
                    "no from clause found in query");
        }
        int fromLoc = fromMatcher.start(2);

        Matcher orderMatcher = ORDER_PATTERN.matcher(ejbql);
        int orderLoc = orderMatcher.find() ? orderMatcher.start(1)
                : ejbql.length();

        return getCountPropertyEjbql(ejbql.substring(0, fromLoc)) + ejbql.substring(fromLoc, orderLoc);
    }

    /**
     * Return the count way to used (could be either DEFAULT_COUNT_EJBQL or DISTINCT_COUNT_EJBQL)
     * in a count query.
     *
     * @param selectBody select body
     * @return String The count way
     */

    protected String getCountPropertyEjbql(String selectBody) {
        Matcher distinctMatcher = DISTINCT_PATTERN.matcher(selectBody);
        return COUNT_EJBQL.replaceAll("<countProperty>", distinctMatcher.find() ? selectBody.substring(distinctMatcher.start(), selectBody.length()).trim() : "*");
    }
}
