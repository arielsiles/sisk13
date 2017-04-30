package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 3.2
 */
public class ExpiredReceivablesWidgetSql implements SqlQuery {
    private Integer businessUnitId;

    private Integer upperBound = 0;

    private Integer lowerBound = 0;

    public String getSql() {
        String sql = "SELECT CASE WHEN (COUNT(O.NO_TRANS)>=" + lowerBound
                + " AND COUNT(O.NO_TRANS) <=" + upperBound + " ) THEN COUNT(O.NO_TRANS) ELSE 0 END \n"
                + "FROM " + Constants.FINANCES_SCHEMA + ".CXP_DOCUS O \n"
                + "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CXP_MOVS M on (M.NO_TRANS=O.NO_TRANS and M.NO_CIA= O.NO_CIA) \n"
                + "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".USUARIOS U on U.NO_USR=M.NO_USR \n"
                + "LEFT JOIN UNIDADNEGOCIO UN ON UN.CODUNIDADEJECUTORA=U.COD_UNI \n"
                + "WHERE \n";
        if (null != businessUnitId) {
            sql += "UN.IDUNIDADNEGOCIO=" + businessUnitId + " AND\n";
        }
        sql += "O.ESTADO='APR' \n" +
                "AND O.FECHA_VEN IS NOT NULL \n" +
                "AND O.FECHA_VEN < SYSDATE \n" +
                "AND O.SALDO>0 \n";

        return sql;
    }

    public void setBusinessUnitId(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }


    public void setUpperBound(Integer upperBound) {
        this.upperBound = upperBound;
    }

    public void setLowerBound(Integer lowerBound) {
        this.lowerBound = lowerBound;
    }
}
