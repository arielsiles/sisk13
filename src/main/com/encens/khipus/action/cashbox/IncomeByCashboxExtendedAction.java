package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.cashbox.IncomeByCashboxExtended;
import com.encens.khipus.dashboard.module.cashbox.IncomeByCashboxExtendedInstanceFactory;
import com.encens.khipus.dashboard.module.cashbox.IncomeByCashboxExtendedSqlQuery;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.Map;

/**
 * @author
 * @version 2.7
 */
@Name("incomeByCashboxExtendedAction")
@Scope(ScopeType.PAGE)
public class IncomeByCashboxExtendedAction extends DashboardObjectAction<IncomeByCashboxExtended> {
    private Date startDate = DateUtils.getDate(DateUtils.getCurrentYear(new Date()), 1, 1);

    private Date endDate = new Date();

    @Create
    public void initialize() {
        search();
    }

    public Map<String, Number> getTotals() {
        return ((SumTotalizer<IncomeByCashboxExtended>) factory.getTotalizer()).getTotals();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<IncomeByCashboxExtended, SumTotalizer<IncomeByCashboxExtended>>(
                new IncomeByCashboxExtendedSqlQuery(),
                new IncomeByCashboxExtendedInstanceFactory(),
                new SumTotalizer<IncomeByCashboxExtended>()
        );
    }

    @Override
    protected void setFilters() {
        if (null != startDate) {
            ((IncomeByCashboxExtendedSqlQuery) factory.getSqlQuery()).setStartDate(DateUtils.dateToInteger(startDate));
        }

        if (null != endDate) {
            ((IncomeByCashboxExtendedSqlQuery) factory.getSqlQuery()).setEndDate(DateUtils.dateToInteger(endDate));
        }
    }
}
