package com.encens.khipus.action.finances;

import com.encens.khipus.action.dashboard.GraphicViewAction;
import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.finances.sql.BankBalanceSql;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.21.3
 */

@Name("bankBalanceViewAction")
@Scope(ScopeType.EVENT)
@Restrict("#{s:hasPermission('BANKBALANCEPANEL','VIEW')}")
public class BankBalanceViewAction extends GraphicViewAction<BankBalanceGraph> {
    private Map<BankBalanceSql.BankBalanceType, List<Dto>> resultMap = new HashMap<BankBalanceSql.BankBalanceType, List<Dto>>();

    @Create
    public void initialize() {
        setGraphic(new BankBalanceGraph());
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    public byte[] createBankChart() {
        return getGraphicInstance().createBankChart();
    }

    public String getYear() {
        return MessageUtils.getMessage("Dashboard.year", ((BankBalanceSql) getSqlQueryInstance()).getYear().toString()).trim();
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new BankBalanceSql();
    }

    @Override
    protected void executeService(SqlQuery sqlQuery) {
        for (BankBalanceSql.BankBalanceType type : BankBalanceSql.BankBalanceType.values()) {
            ((BankBalanceSql) sqlQuery).setBalanceType(type);
            List<Dto> result = dashboardQueryService.getData(getDtoConfiguration(), getInstanceBuilder(), sqlQuery);
            resultMap.put(type, result);
        }
    }

    @Override
    protected DtoConfiguration getDtoConfiguration() {
        return DtoConfiguration.getInstance(IdField.getInstance("code", 0))
                .addField(SingleField.getInstance("bankName", 1))
                .addField(SingleField.getInstance("accountNumber", 2))
                .addField(SingleField.getInstance("monthName", 3))
                .addField(SingleField.getInstance("currencyName", 5))
                .addField(SingleField.getInstance("amount", 6));
    }

    @Override
    protected void setGraphicParameters(BankBalanceGraph graphic) {
        graphic.setResultMap(resultMap);
    }
}
