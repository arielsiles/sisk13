package com.encens.khipus.dashboard.module.purchases;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.26
 */
public class PendingInvoicesWidgetWidgetSql implements SqlQuery {
    private Integer start;
    private Integer end;
    private Integer businessUnitId;

    public String getSql() {
        String sql = "SELECT COUNT(OC.ID_COM_ENCOC) \n" +
                "FROM " + Constants.FINANCES_SCHEMA + ".COM_ENCOC OC \n" +
                "WHERE (OC.NUMERO_FACTURA IS NULL OR OC.NUMERO_FACTURA='PENDIENTE') \n" +
                "AND OC.ESTADO NOT IN ('PEN','ANL') \n";
        if (null != businessUnitId) {
            sql += " AND OC.IDUNIDADNEGOCIO = " + businessUnitId;
        }
        sql += "AND DECODE(     SIGN(TRUNC(SYSDATE)- trunc(OC.FECHA) ),     1,   (TRUNC(SYSDATE)- trunc(OC.FECHA) ),    0   ) >= " + start +
                "AND DECODE(     SIGN(TRUNC(SYSDATE)- trunc(OC.FECHA) ),     1,   (TRUNC(SYSDATE)- trunc(OC.FECHA) ),    0   ) <= " + end;

        return sql;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public void setBusinessUnitId(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }
}
