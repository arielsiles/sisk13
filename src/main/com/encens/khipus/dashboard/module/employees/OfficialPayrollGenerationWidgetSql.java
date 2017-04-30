package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.sql.SqlQuery;

/**
 * @author
 * @version 2.26
 */
public class OfficialPayrollGenerationWidgetSql implements SqlQuery {
    private Integer year;
    private String month;
    private Integer executorUnitId;

    public String getSql() {
        String sql = " SELECT TP.TOTALPLAN, PO.PLANOFICIAL," +
                " CASE WHEN TP.TOTALPLAN > 0 THEN ROUND(PO.PLANOFICIAL * 100 / TP.TOTALPLAN, 0) ELSE 0 END PLANOFICIALPORCENT" +
                " FROM" +
                " (SELECT COUNT(*) TOTALPLAN" +
                " FROM GESTIONPLANILLA GP LEFT JOIN GESTION GE ON GP.IDGESTION = GE.IDGESTION" +
                " WHERE" +
                " GE.ANIO = " + year +
                " AND GP.MES = '" + month + "'";
        if (null != executorUnitId) {
            sql += " AND GP.IDUNIDADNEGOCIO = " + executorUnitId;
        }
        sql += " ) TP," +
                " (SELECT COUNT(*) PLANOFICIAL" +
                " FROM GESTIONPLANILLA GP LEFT JOIN GESTION GE ON GP.IDGESTION = GE.IDGESTION" +
                " WHERE" +
                " GE.ANIO = " + year +
                " AND GP.MES = '" + month + "'";
        if (null != executorUnitId) {
            sql += " AND GP.IDUNIDADNEGOCIO = " + executorUnitId;
        }
        sql += " AND EXISTS(SELECT POG.IDPLANILLAGENERADA FROM PLANILLAGENERADA POG WHERE POG.TIPOPLANILLAGEN = 'OFFICIAL' AND POG.IDGESTIONPLANILLA = GP.IDGESTIONPLANILLA)" +
                " ) PO";

        return sql;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}
