package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Totalizer;
import com.encens.khipus.service.dashboard.DashboardQueryService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This implementation should be removed when the others changing his parent class by ViewAction
 *
 * @author
 * @version 2.7
 */
@Name("dashboardObjectAction")
public class DashboardObjectAction<T extends DashboardObject> implements Serializable {
    private String containerWidth = "480";
    protected static final Integer GRAPHIC_HEIGHT = 300;

    private List<T> resultList = new ArrayList<T>();

    protected ComponentFactory<T, ? extends Totalizer<T>> factory;

    @Logger
    protected Log log;

    @In
    protected DashboardQueryService dashboardQueryService;

    public DashboardObjectAction() {
        initializeFactory();
    }

    public List<T> getResultList() {
        return resultList;
    }

    public void search() {
        factory.getTotalizer().initialize();
        setFilters();

        resultList = dashboardQueryService.executeQuery(factory);
    }

    public String getContainerWidth() {
        return containerWidth;
    }

    public void setContainerWidth(String containerWidth) {
        this.containerWidth = containerWidth;
    }

    public boolean isResultListEmpty() {
        return resultList.isEmpty();
    }

    public String getSql() {
        setFilters();
        return factory.getSqlQuery().getSql();
    }

    protected void initializeFactory() {

    }

    protected void setFilters() {

    }

    protected Integer getGraphicWidth() {
        return BigDecimalUtil.divide(
                BigDecimalUtil.multiply(new BigDecimal(containerWidth), new BigDecimal("90")),
                new BigDecimal("100")).intValue();
    }
}
