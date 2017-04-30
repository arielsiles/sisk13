package com.encens.khipus.dashboard.module.warehouse;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.util.SemaphoreState;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 3.3
 */
public class MaxProductItemControlWidgetSql implements SqlQuery {

    private Integer businessUnitId;
    private SemaphoreState semaphoreState;

    public String getSql() {
        String sql = "SELECT count(*)\n " +
                " FROM " + Constants.FINANCES_SCHEMA + ".INV_INVENTARIO INV\n" +
                " LEFT JOIN " + Constants.FINANCES_SCHEMA + ".INV_ARTICULOS ART on INV.NO_CIA = ART.NO_CIA AND INV.COD_ART = ART.COD_ART \n" +
                " LEFT JOIN " + Constants.FINANCES_SCHEMA + ".INV_ALMACENES ALM on INV.NO_CIA = ALM.NO_CIA AND INV.COD_ALM = ALM.COD_ALM \n" +
                " LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".UNIDADNEGOCIO UN on ALM.IDUNIDADNEGOCIO = UN.IDUNIDADNEGOCIO \n" +
                " WHERE INV.NO_CIA = " + Constants.defaultCompanyNumber + " \n";

        sql += applyFilters();

        return sql;
    }

    private String applyFilters() {
        String filter = "";

        if (null != businessUnitId) {
            filter += " AND UN.IDUNIDADNEGOCIO=" + businessUnitId + " \n";
        }

        if (SemaphoreState.GREEN.equals(semaphoreState)) {
            //filter as valid range of product item stock
            filter += " AND INV.SALDO_UNI <= ART.STOCKMAXIMO AND (ART.STOCKMINIMO IS NULL OR INV.SALDO_UNI > ART.STOCKMINIMO) \n";
        }else if (SemaphoreState.RED.equals(semaphoreState)) {
            //filter as out range of product item stock
            filter += " AND INV.SALDO_UNI > ART.STOCKMAXIMO \n";
        } else {
            //return empty result
            filter += " AND INV.NO_CIA IS NULL \n";
        }

        return filter;
    }

    public void setBusinessUnitId(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    public SemaphoreState getSemaphoreState() {
        return semaphoreState;
    }

    public void setSemaphoreState(SemaphoreState semaphoreState) {
        this.semaphoreState = semaphoreState;
    }
}
