package com.encens.khipus.action.common;

import com.encens.khipus.util.query.EntityQueryExecutorUtil;
import org.jboss.seam.annotations.Name;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.22
 */
@Name("entityQueryExecutor")
public class EntityQueryExecutor {
    /**
     * Execute any <code>EntityQuery</code> object that are defined in the <code>components.xml</code> file
     * under name <code>queryName</code> and enable the <code>businessUnitFilter</code> filter in the query
     * entities.
     *
     * @param queryName The name of the <code>EntityQuery</code> object in the <code>components.xml</code> file.
     * @return <code>List</code> object that contains the result of the <code>EntityQuery</code> execution.
     */
    public List byBusinessUnit(String queryName) {
        return EntityQueryExecutorUtil.i.executeNamedQuery(queryName, Arrays.asList("businessUnitFilter"));
    }
}
