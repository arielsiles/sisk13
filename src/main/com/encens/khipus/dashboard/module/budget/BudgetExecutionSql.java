package com.encens.khipus.dashboard.module.budget;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.budget.ClassifierType;
import com.encens.khipus.util.Constants;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Query for the pie widget
 *
 * @author
 * @version 2.26
 */
public class BudgetExecutionSql implements SqlQuery {
    private Integer executorUnitId;

    private Integer upperBound = 0;

    private Integer lowerBound = 0;

    private ClassifierType classifierType;

    private Boolean exceeded = false;

    public String getSql() {
        Calendar today = new GregorianCalendar();
        String sql =
                " select COUNT(*) \n" +
                        " from \n" +
                        "    (select caccount.codigocuenta accountCode, SUM(budget.importe) budgetImport, budget.idunidadnegocio businessUnitId \n" +
                        "     from clasifcuenta caccount \n" +
                        "          left join clasificador classifier on caccount.idclasificador=classifier.idclasificador\n" +
                        "          left join " + getBudgetTableName() + " budget on classifier.idclasificador=budget.idclasificador\n" +
                        "          left join gestion gestion on budget.idgestion=gestion.idgestion\n" +
                        "     where classifier.tipo='" + classifierType.name() + "' \n" +
                        "           and gestion.anio=" + today.get(Calendar.YEAR) + "\n ";
        if (null != executorUnitId) {
            sql += "           and budget.idunidadnegocio=" + executorUnitId + "\n";
        }
        sql += "     group by caccount.codigocuenta, budget.idunidadnegocio)  burdenTable left join\n" +

                "    (select movdet.cuenta accountCode, SUM(movdet.monto_mn/movdet.tc) executionImport, movdet.cod_uni businessUnitId \n" +
                "     from " + Constants.FINANCES_SCHEMA + ".cg_movdet movdet " +
                "          left join " + Constants.FINANCES_SCHEMA + ".cg_movmae mov on movdet.no_cia=mov.no_cia and movdet.no_compro=mov.no_compro and movdet.tipo_compro=mov.tipo_compro " +
                "     where TO_NUMBER(TO_CHAR(mov.fecha,'YYYY'))=" + today.get(Calendar.YEAR) + " \n";
        if (null != executorUnitId) {
            sql += "          and movdet.cod_uni=" + executorUnitId + "\n";
        }
        sql += "     group by movdet.cuenta,  movdet.cod_uni) executionTable on burdenTable.accountCode=executionTable.accountCode\n" +
                "                                  and TO_NUMBER(burdenTable.businessUnitId)=executionTable.businessUnitId \n ";
        if (!isExceeded()) {
            sql += " where (100-(executionTable.executionImport*100/burdenTable.budgetImport))>=" + lowerBound +
                    "       and (100-(executionTable.executionImport*100/burdenTable.budgetImport))<=" + upperBound;
        } else {
            sql += " where (executionTable.executionImport*100/burdenTable.budgetImport)> 100" +
                    "       and ((executionTable.executionImport*100/burdenTable.budgetImport)-100)>=" + lowerBound +
                    "       and ((executionTable.executionImport*100/burdenTable.budgetImport)-100)<=" + upperBound;
        }

        return sql;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }

    public void setUpperBound(Integer upperBound) {
        this.upperBound = upperBound;
    }

    public void setLowerBound(Integer lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setClassifierType(ClassifierType classifierType) {
        this.classifierType = classifierType;
    }

    public Boolean isExceeded() {
        return exceeded;
    }

    public void setExceeded(Boolean exceeded) {
        this.exceeded = exceeded;
    }

    public String getBudgetTableName() {
        return ClassifierType.BURDEN.equals(classifierType) ? "presupuestoingreso" : "presupuestogasto";
    }
}
