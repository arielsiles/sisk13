package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.GraphicViewAction;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.cashbox.sql.DebtSql;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.15
 */
@Name("debtViewAction")
@Scope(ScopeType.EVENT)
public class DebtViewAction extends GraphicViewAction<DebtGraph> {

    private Integer executorUnitId;

    @Create
    public void initialize() {
        setGraphic(new DebtGraph());
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    public String getYear() {
        return MessageUtils.getMessage("Dashboard.year", ((DebtSql) getSqlQueryInstance()).getYear().toString()).trim();
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }

    public void disableExecutorUnit() {
        this.executorUnitId = null;
    }

    @Override
    protected DtoConfiguration getDtoConfiguration() {
        if (executorUnitId != null) {//by business unit
            return DtoConfiguration.getInstance(IdField.getInstance("id", 2))
                    .addField(SingleField.getInstance("domainName", 4))//academic unit name
                    .addField(SingleField.getInstance("registeredStudentCreatedDebt", 5))
                    .addField(SingleField.getInstance("registeredStudentPayOver", 6))
                    .addField(SingleField.getInstance("registeredStudentDebt", 7))
                    .addField(SingleField.getInstance("studentshipCreatedDebt", 8))
                    .addField(SingleField.getInstance("studentshipPayOver", 9))
                    .addField(SingleField.getInstance("studentshipDebt", 10))
                    .addField(SingleField.getInstance("defectorStudentCreateDebt", 11))
                    .addField(SingleField.getInstance("defectorStudentPayOver", 12))
                    .addField(SingleField.getInstance("defectorStudentDebt", 13))
                    .addField(SingleField.getInstance("registeredStudentCreatedDebtAmount", 14))
                    .addField(SingleField.getInstance("registeredStudentPayOverAmount", 15))
                    .addField(SingleField.getInstance("registeredStudentDebtAmount", 16))
                    .addField(SingleField.getInstance("studentshipCreatedDebtAmount", 17))
                    .addField(SingleField.getInstance("studentshipPayOverAmount", 18))
                    .addField(SingleField.getInstance("studentshipDebtAmount", 19))
                    .addField(SingleField.getInstance("defectorStudentCreateDebtAmount", 20))
                    .addField(SingleField.getInstance("defectorStudentPayOverAmount", 21))
                    .addField(SingleField.getInstance("defectorStudentDebtAmount", 22));
        } else { //all business units
            return DtoConfiguration.getInstance(IdField.getInstance("id", 1))
                    .addField(SingleField.getInstance("domainName", 2))
                    .addField(SingleField.getInstance("registeredStudentCreatedDebt", 3))
                    .addField(SingleField.getInstance("registeredStudentPayOver", 4))
                    .addField(SingleField.getInstance("registeredStudentDebt", 5))
                    .addField(SingleField.getInstance("studentshipCreatedDebt", 6))
                    .addField(SingleField.getInstance("studentshipPayOver", 7))
                    .addField(SingleField.getInstance("studentshipDebt", 8))
                    .addField(SingleField.getInstance("defectorStudentCreateDebt", 9))
                    .addField(SingleField.getInstance("defectorStudentPayOver", 10))
                    .addField(SingleField.getInstance("defectorStudentDebt", 11))
                    .addField(SingleField.getInstance("registeredStudentCreatedDebtAmount", 12))
                    .addField(SingleField.getInstance("registeredStudentPayOverAmount", 13))
                    .addField(SingleField.getInstance("registeredStudentDebtAmount", 14))
                    .addField(SingleField.getInstance("studentshipCreatedDebtAmount", 15))
                    .addField(SingleField.getInstance("studentshipPayOverAmount", 16))
                    .addField(SingleField.getInstance("studentshipDebtAmount", 17))
                    .addField(SingleField.getInstance("defectorStudentCreateDebtAmount", 18))
                    .addField(SingleField.getInstance("defectorStudentPayOverAmount", 19))
                    .addField(SingleField.getInstance("defectorStudentDebtAmount", 20));
        }
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new DebtSql(executorUnitId);
    }

    @Override
    protected void setGraphicParameters(DebtGraph graphic) {
        if (null != executorUnitId) {
            graphic.setXLabel(MessageUtils.getMessage("Debt.xLabel.facultyAcronim"));
        } else {
            graphic.setXLabel(MessageUtils.getMessage("Debt.xLabel.executorUnitName"));
        }
    }
}
