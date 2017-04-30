package com.encens.khipus.dashboard.module.fixedAssets;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.26
 */
public class DischargeBeforeLifetimeWidgetSql implements SqlQuery {
    private Integer start;
    private Integer end;
    private Integer executorUnitId;

    public String getSql() {
        String sql = "SELECT COUNT(*)" +
                " FROM" +
                " (SELECT (CASE WHEN T.DIASBAJAVU > 0 THEN" +
                " ROUND(100 - (SELECT CASE WHEN T.DIASBAJAACT > T.DIASBAJAVU THEN T.DIASBAJAVU ELSE T.DIASBAJAACT END FROM DUAL) * 100 / T.DIASBAJAVU, 0)" +
                " ELSE 0 END) AS PORCENTAJE" +
                " FROM (" +
                " SELECT TRUNC(AF.FCH_BAJA - AF.FCH_ALTA) AS DIASBAJAACT," +
                " TRUNC(ADD_MONTHS(AF.FCH_ALTA,AF.DURACION) - AF.FCH_ALTA) AS DIASBAJAVU" +
                " FROM " + Constants.FINANCES_SCHEMA + ".AF_ACTIVOS AF" +
                " WHERE AF.DURACION IS NOT NULL AND AF.FCH_ALTA IS NOT NULL AND AF.FCH_BAJA IS NOT NULL";
        if (null != executorUnitId) {
            sql += " AND AF.IDUNIDADNEGOCIO = " + executorUnitId;
        }
        sql += " ) T" +
                " ) ACTIVO" +
                " WHERE ACTIVO.PORCENTAJE >= " + start +
                " AND ACTIVO.PORCENTAJE <= " + end;

        return sql;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}
