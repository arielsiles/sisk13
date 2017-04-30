package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.ViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.cashbox.sql.DebtReportSql;
import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.model.cashbox.Category;
import com.encens.khipus.model.cashbox.Entry;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;

/**
 * @author
 * @version 2.7
 */
@Name("debtFiltersAction")
@Scope(ScopeType.PAGE)
public class DebtFiltersAction extends ViewAction {
    private ExecutorUnit executorUnit;
    private Entry entry;
    private Category category;
    private Integer year = DateUtils.getCurrentYear(new Date());


    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new DebtReportSql();
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        Integer executorUnitId = null;
        if (null != executorUnit) {
            executorUnitId = executorUnit.getId();
        }

        String entryId = "%";
        if (null != entry) {
            entryId = entry.getId();
        }
        String categoryId = "%";
        if (null != category) {
            categoryId = category.getId();
        }

        ((DebtReportSql) sqlQuery).setExecutorUnitCode(executorUnitId);
        ((DebtReportSql) sqlQuery).setEntryId(entryId);
        ((DebtReportSql) sqlQuery).setCategoryId(categoryId);
        ((DebtReportSql) sqlQuery).setYear(year);
    }

    public ExecutorUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(ExecutorUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
