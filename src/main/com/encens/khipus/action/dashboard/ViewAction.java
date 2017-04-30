package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.factory.DefaultInstanceBuilder;
import com.encens.khipus.dashboard.component.dto.factory.InstanceBuilder;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.service.dashboard.DashboardQueryService;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Name("viewAction")
public class ViewAction implements Serializable {
    @Logger
    protected Log log;

    @In
    protected DashboardQueryService dashboardQueryService;

    private List<Dto> resultList = new ArrayList<Dto>();

    public void search() {
        SqlQuery sqlQuery = getSqlQueryInstance();
        setFilters(sqlQuery);

        executeService(sqlQuery);
    }

    protected void executeService(SqlQuery sqlQuery) {
        resultList = dashboardQueryService.getData(getDtoConfiguration(), getInstanceBuilder(), sqlQuery);
    }

    public List<Dto> getResultList() {
        return resultList;
    }

    public String getSql() {
        SqlQuery sqlQuery = getSqlQueryInstance();
        setFilters(sqlQuery);

        return sqlQuery.getSql();
    }

    protected DtoConfiguration getDtoConfiguration() {
        throw new UnsupportedOperationException("This method should be overwrite in the children classes");
    }

    protected SqlQuery getSqlQueryInstance() {
        throw new UnsupportedOperationException("This method should be overwrite in the children classes");
    }

    protected InstanceBuilder getInstanceBuilder() {
        return new DefaultInstanceBuilder();
    }

    protected void setFilters(SqlQuery sqlQuery) {
    }
}
