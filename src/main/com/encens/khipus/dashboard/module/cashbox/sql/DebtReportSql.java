package com.encens.khipus.dashboard.module.cashbox.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * @author
 * @version 2.17
 */
public class DebtReportSql implements SqlQuery {
    private Integer executorUnitCode = null;
    private Integer year = DateUtils.getCurrentYear(new Date());

    private Integer periodId = null;
    private String entryId = "%";
    private String categoryId = "%";


    public String getSql() {
        String sql = "";
        sql += setFilters();
        return sql;
    }

    private String setFilters() {
        String sql = " AND CU.RBR_COD LIKE '" + entryId + "'\n" +
                " AND CU.CAT_COD  LIKE '" + categoryId + "'\n";

        if (null != periodId) {
            sql += " AND TO_CHAR(CXP.PERIODO) = '" + periodId + "'\n";
        }

        if (null != year) {
            sql += " AND TO_CHAR(CXP.GESTION) = '" + year + "'  \n";
        }

        if (null != executorUnitCode) {
            sql += " AND CXP.UNIDAD_ACAD_ADM = " + executorUnitCode + "\n";
        }

        return sql;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
