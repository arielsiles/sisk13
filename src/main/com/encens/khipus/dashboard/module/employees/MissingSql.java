package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.employees.HoraryBandStateType;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 3.2
 */
public class MissingSql implements SqlQuery {
    private Integer businessUnitId;
    private CostCenter costCenter;
    private OrganizationalUnit organizationalUnit;

    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("\nSELECT COUNT(EBH.IDESTADOBANDAHORARIA), UO.NOMBRE, CC.DESCRI \n" +
                "FROM " + Constants.KHIPUS_SCHEMA + ".ESTADOBANDAHORARIA EBH \n" +
                "LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".UNIDADORGANIZACIONAL UO ON UO.IDUNIDADORGANIZACIONAL=EBH.IDUNIDADORGANIZACIONAL \n" +
                "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_CENCOS CC ON CC.COD_CC= UO.CODIGOCENCOS AND CC.NO_CIA= UO.NUMEROCOMPANIA \n" +
                "WHERE EBH.ESTADO='");
        sql.append(HoraryBandStateType.MISSING.name());
        sql.append("' \n" +
                "AND EBH.FECHA= SYSDATE \n");
        if (null != businessUnitId) {
            sql.append("AND EBH.IDUNIDADNEGOCIO = ");
            sql.append(businessUnitId);
            sql.append(" \n");
        }
        if (null != costCenter) {
            sql.append("AND EBH.COD_CC = ");
            sql.append(costCenter.getCode());
            sql.append(" AND EBH.NO_CIA = ");
            sql.append(costCenter.getCompanyNumber());
            sql.append(" \n");
        }
        if (null != organizationalUnit) {
            sql.append("AND EBH.IDUNIDADORGANIZACIONAL = ");
            sql.append(organizationalUnit.getId());
            sql.append(" \n");
        }
        sql.append("GROUP BY UO.NOMBRE, CC.DESCRI \n");
        return sql.toString();
    }

    public void setBusinessUnitId(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }
}