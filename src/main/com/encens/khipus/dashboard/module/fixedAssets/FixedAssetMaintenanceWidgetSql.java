package com.encens.khipus.dashboard.module.fixedAssets;

import com.encens.khipus.dashboard.component.sql.SqlQuery;

/**
 * @author
 * @version 2.26
 */
public class FixedAssetMaintenanceWidgetSql implements SqlQuery {

    private Integer start;
    private Integer end;
    private Integer executorUnitId;

    public String getSql() {
        String sql = "SELECT COUNT (*) FROM ( " +
                " SELECT TRUNC(SYSDATE-M.FECHAESTIMADARECEPCION) AS DAYSELAPSED " +
                " FROM SOLICITUDMANTENIMIENTO SM INNER JOIN MANTENIMIENTO M ON SM.IDMANTENIMIENTO = M.IDMANT " +
                " WHERE SM.ESTADO = 'APPROVED' AND M.FECHAESTIMADARECEPCION<=SYSDATE ";
        if (null != executorUnitId) {
            sql += " AND SM.IDUNIDADEJECUTORA = " + executorUnitId;
        }
        sql += " ) WHERE DAYSELAPSED >= " + start + " AND DAYSELAPSED <= " + end;

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
