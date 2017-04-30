package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.Constants;

/**
 * Encens S.R.L.
 * This class implements the credit/Debit comparative report sql query
 *
 * @author
 * @version 2.17
 */
public class CreditDebitComparativeReportSql implements SqlQuery {
    private String year;
    private String businessUnitCode;
    private String costCenterCode;

    public String getSql(){
        StringBuffer sql=new StringBuffer();
                sql.append("select distinct \n" +
                "        businessunit.codunidadejecutora executorUnitCode,\n" +
                "        businessunit.publicidad businessUnitName,\n" +
                "        costcenter.descri costCenterName,\n" +
                "        costcenter.cod_cc costCenterCode,\n" +
                "        TO_CHAR(movmae.fecha,'MM') movementMonth,\n" +
                "        arcgms.clase accountClass,\n" +
                "        ( select SUM(movdet2.monto_mn)\n" +
                "          from "+Constants.FINANCES_SCHEMA+".CG_MOVMAE movmae2\n" +
                "          join "+Constants.FINANCES_SCHEMA+".CG_MOVDET movdet2 on movdet2.no_cia = movmae2.no_cia and movdet2.no_compro = movmae2.no_compro and movdet2.tipo_compro = movmae2.tipo_compro\n" +
                "          join "+Constants.FINANCES_SCHEMA+".ARCGMS arcgms2 on arcgms2.cuenta = movdet2.cuenta\n" +
                "          join "+Constants.FINANCES_SCHEMA+".CG_CENCOS costCenter2 on costcenter2.cod_cc = movdet2.cod_cc\n" +
                "          join unidadnegocio businessUnit2 on businessunit2.codunidadejecutora = movdet2.cod_uni\n" +
                "          where TO_CHAR(movmae2.fecha,'YYYY') = '"+year+"'\n" +
                "                and TO_CHAR(movmae2.fecha,'MM')= TO_CHAR(movmae.fecha,'MM')\n" +
                "                and movdet2.moneda='"+ FinancesCurrencyType.P.name()+"'\n" +
                "                and arcgms2.clase = arcgms.clase\n" +
                "                and businessunit2.codunidadejecutora = businessunit.codunidadejecutora\n" +
                "                and costcenter2.cod_cc = costcenter.cod_cc\n" +
                "        )as bsAmount,\n" +
                "        ( select SUM(movdet3.monto_mn*movdet3.tc)\n" +
                "          from "+Constants.FINANCES_SCHEMA+".CG_MOVMAE movmae3\n" +
                "               join "+Constants.FINANCES_SCHEMA+".CG_MOVDET movdet3 on movdet3.no_cia = movmae3.no_cia and movdet3.no_compro = movmae3.no_compro and movdet3.tipo_compro = movmae3.tipo_compro\n" +
                "               join "+Constants.FINANCES_SCHEMA+".ARCGMS arcgms3 on arcgms3.cuenta = movdet3.cuenta\n" +
                "               join "+Constants.FINANCES_SCHEMA+".CG_CENCOS costCenter3 on costcenter3.cod_cc = movdet3.cod_cc\n" +
                "               join unidadnegocio businessUnit3 on businessunit3.codunidadejecutora = movdet3.cod_uni\n" +
                "          where TO_CHAR(movmae3.fecha,'YYYY') = '"+year+"'\n" +
                "               and TO_CHAR(movmae3.fecha,'MM')= TO_CHAR(movmae.fecha,'MM')\n" +
                "               and movdet3.moneda='"+FinancesCurrencyType.D.name()+"'\n" +
                "               and arcgms3.clase = arcgms.clase\n" +
                "               and businessunit3.codunidadejecutora = businessunit.codunidadejecutora\n" +
                "               and costcenter3.cod_cc = costcenter.cod_cc\n" +
                "          )as susAmount\n" +
                "from "+Constants.FINANCES_SCHEMA+".CG_MOVMAE movmae\n" +
                "     join "+Constants.FINANCES_SCHEMA+".CG_MOVDET movdet on movdet.no_cia = movmae.no_cia and movdet.no_compro = movmae.no_compro and movdet.tipo_compro = movmae.tipo_compro\n" +
                "     join "+Constants.FINANCES_SCHEMA+".ARCGMS arcgms on arcgms.cuenta = movdet.cuenta\n" +
                "     join "+Constants.FINANCES_SCHEMA+".CG_CENCOS costCenter on costcenter.cod_cc = movdet.cod_cc\n" +
                "     join unidadnegocio businessUnit on businessunit.codunidadejecutora = movdet.cod_uni " +
                "where TO_CHAR(movmae.fecha,'YYYY') = '"+year+"' and movmae.fecha is not null and (arcgms.clase='G' or arcgms.clase='I') ");
        if(businessUnitCode!=null && businessUnitCode.length()>0){
                sql.append("and businessUnit.codunidadejecutora = '")
                   .append(businessUnitCode)
                   .append("' ");
        }

        if(costCenterCode!=null && costCenterCode.length()>0){
                sql.append("and costCenter.cod_cc = '")
                   .append(costCenterCode)
                   .append("' ");
        }
        sql.append("order by businessUnitName, executorUnitCode, costCenterName, costCenterCode, accountClass, TO_CHAR(movmae.fecha,'MM')");
        return(sql.toString());
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getBusinessUnitCode() {
        return businessUnitCode;
    }

    public void setBusinessUnitCode(String businessUnitCode) {
        this.businessUnitCode = businessUnitCode;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }
}
