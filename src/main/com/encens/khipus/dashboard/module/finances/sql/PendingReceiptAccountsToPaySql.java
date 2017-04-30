package com.encens.khipus.dashboard.module.finances.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * Query for the pie widget
 *
 * @author
 * @version 2.26
 */
public class PendingReceiptAccountsToPaySql implements SqlQuery {
    private Integer executorUnitId;

    private Integer upperBound = 0;
    private Integer lowerBound = 0;

    public String getSql() {
        String sql = "SELECT COUNT(*) \n" +
                " FROM " + Constants.FINANCES_SCHEMA + ".CXP_DOCUS DOC \n" +
                "      LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CXP_TIPODOCS PD ON PD.NO_CIA=DOC.NO_CIA AND PD.TIPO_DOC=DOC.TIPO_DOC\n" +
                "      LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CXP_MOVS MS ON MS.NO_CIA=DOC.NO_CIA AND MS.NO_TRANS=DOC.NO_TRANS\n" +
                "      LEFT JOIN " + Constants.FINANCES_SCHEMA + ".USUARIOS USR ON MS.NO_USR=USR.NO_USR\n" +
                " WHERE PD.CLASE_DOC='FAC' AND DOC.PENDIENTE_REGISTRO='SI' AND DOC.ESTADO='APR' AND MS.ESTADO='APR'\n";
        if (null != executorUnitId) {
            sql += " AND USR.COD_UNI=" + executorUnitId + " \n";
        }
        sql += " AND trunc(SYSDATE-DOC.FECHA)>=" + lowerBound + " AND trunc(SYSDATE-DOC.FECHA)<=" + upperBound;
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
}
