package com.encens.khipus.dashboard.module.fixedAssets;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.26
 */
public class FixedAssetWidgetSql implements SqlQuery {
    private Integer businessUnitId;

    private Integer upperBound = 0;

    private Integer lowerBound = 0;

    public String getSql() {
        String sql = "SELECT COUNT(O.ID_COM_ENCOC) \n"
                + "FROM " + Constants.FINANCES_SCHEMA + ".COM_ENCOC O \n" +
                "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".AFVALE V ON V.IDORDENCOMPRA=O.ID_COM_ENCOC \n"
                + "WHERE \n";
        if (null != businessUnitId) {
            sql += "O.IDUNIDADNEGOCIO=" + businessUnitId + " AND\n";
        }
        sql += "O.ESTADO!='PEN' \n" +
                "AND O.ESTADO!='ANL' \n" +
                "AND O.TIPO='FIXEDASSET' \n" +
                "AND (V.ESTADO IS NULL OR V.ESTADO!='APR') \n" +
                "AND trunc(SYSDATE-(    DECODE (SIGN(SYSDATE-O.FECHA_RECEPCION), 1, O.FECHA_RECEPCION ,SYSDATE ) )) >=" + lowerBound + "\n" +
                "AND trunc(SYSDATE-(    DECODE (SIGN(SYSDATE-O.FECHA_RECEPCION), 1, O.FECHA_RECEPCION ,SYSDATE ) )) <=" + upperBound + "\n";

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
