package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.sql.SqlQuery;

/**
 * @author
 * @version 2.26
 */
public class FoundsToPayWidgetSql implements SqlQuery {
    private Integer start;
    private Integer end;
    private Integer executorUnitId;

    public String getSql() {
        String sql = "SELECT COUNT(*) " +
                " FROM (SELECT " +
                "       (SELECT MIN(CT.FECHAVENCIMIENTO) FROM CUOTA CT " +
                "       WHERE CT.FECHAVENCIMIENTO <= SYSDATE AND CT.ESTADO IN ('APR','PLI') AND CT.IDFONDOROTATORIO = FR.IDFONDOROTATORIO) AS FECHAVENCIMIENTO " +
                "       FROM FONDOROTATORIO FR LEFT JOIN TIPODOCFONDOROTA TDFR ON FR.IDTIPODOCFONDOROTA=TDFR.IDTIPODOCFONDOROTA" +
                "       WHERE TDFR.TIPOFONDOROTATORIO='RECEIVABLE_FUND' AND FR.ESTADO='APR' ";
        if (null != executorUnitId) {
            sql += " AND FR.IDUNIDADNEGOCIO = " + executorUnitId;
        }
        sql += " ) T " +
                " WHERE T.FECHAVENCIMIENTO IS NOT NULL " +
                " AND TRUNC(SYSDATE-T.FECHAVENCIMIENTO) >= " + start + " AND TRUNC(SYSDATE-T.FECHAVENCIMIENTO) <= " + end;
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
