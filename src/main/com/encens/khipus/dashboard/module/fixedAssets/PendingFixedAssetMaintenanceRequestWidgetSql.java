package com.encens.khipus.dashboard.module.fixedAssets;

import com.encens.khipus.dashboard.component.sql.SqlQuery;

/**
 * @author
 * @version 2.27
 */
public class PendingFixedAssetMaintenanceRequestWidgetSql implements SqlQuery {

    private Integer start;
    private Integer end;
    private Integer businessUnitId;

    public String getSql() {
        String sql = "SELECT COUNT (SM.IDSOLMANT) \n" +
                "FROM SOLICITUDMANTENIMIENTO SM \n" +
                "WHERE \n";
        if (null != businessUnitId) {
            sql += "SM.IDUNIDADEJECUTORA = " + businessUnitId + " AND \n";
        }
        sql += "SM.ESTADO = 'PENDING' \n" +
                "AND DECODE(SIGN(SYSDATE - SM.FECHASOLMANT), 1, SYSDATE - SM.FECHASOLMANT,0 )>= " + start + " \n" +
                "AND DECODE(SIGN(SYSDATE - SM.FECHASOLMANT), 1, SYSDATE - SM.FECHASOLMANT,0 )<=" + end + " \n \n";

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
