package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.module.cashbox.*;
import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.model.cashbox.Category;
import com.encens.khipus.model.cashbox.Entry;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.7
 */
@Name("debtExtendedAction")
@Scope(ScopeType.PAGE)
public class DebtExtendedAction extends DashboardObjectAction<DebtExtended> {
    private ExecutorUnit executorUnit;
    private Integer year = DateUtils.getCurrentYear(new Date());

    private Integer period;
    private List<Entry> entryList;
    private List<Category> categoryList;

    //@Create

    public void initialize() {
        search();
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ExecutorUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(ExecutorUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public List<Entry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public Map<String, DebtExtendedAttribute> getTotals() {
        return ((DebtExtendedTotalizer) factory.getTotalizer()).getTotals();
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<DebtExtended, DebtExtendedTotalizer>(
                new DebtExtendedSqlQuery(),
                new DebtExtendedInstanceFactory(),
                new DebtExtendedTotalizer()
        );
    }

    @Override
    protected void setFilters() {
        Integer executorUnitCode = null;
        List<String> entryCodList = null;
        List<String> categoryCodList = null;

        if (null != executorUnit) {
            executorUnitCode = executorUnit.getId();
        }

        if (null != entryList) {
            entryCodList = new ArrayList<String>();
            for (Entry entry : entryList) {
                entryCodList.add(entry.getId());
            }
        }
        if (null != categoryList) {
            categoryCodList = new ArrayList<String>();
            for (Category category : categoryList) {
                categoryCodList.add(category.getId());
            }
        }

        ((DebtExtendedSqlQuery) factory.getSqlQuery()).setExecutorUnitCode(executorUnitCode);
        ((DebtExtendedSqlQuery) factory.getSqlQuery()).setYear(year);
        ((DebtExtendedSqlQuery) factory.getSqlQuery()).setPeriod(period);
        ((DebtExtendedSqlQuery) factory.getSqlQuery()).setEntryCodList(entryCodList);
        ((DebtExtendedSqlQuery) factory.getSqlQuery()).setCategoryCodList(categoryCodList);
    }

}
