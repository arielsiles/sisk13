package com.encens.khipus.framework.action;

import java.util.HashMap;
import java.util.Map;

/**
 * The QueryResult is a class that containt a ejbql and paramters map that
 * need the ejql for its execution
 *
 * @author
 * @version : 1.0.18
 */
public class QueryResult {

    private String ejbql;
    private Map<String, Object> queryParameters = new HashMap<String, Object>();

    public QueryResult() {
    }

    public QueryResult(String ejbql) {
        this.ejbql = ejbql;
    }

    public String getEjbql() {
        return ejbql;
    }

    public void setEjbql(String ejbql) {
        this.ejbql = ejbql;
    }

    public Map<String, Object> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, Object> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void setParameter(String name, Object value) {
        getQueryParameters().put(name, value);
    }

    @Override
    public String toString() {
        return "QueryResult{" + "ejbql='" + ejbql + '\'' + ", queryParameters=" + queryParameters + '}';
    }
}
