package com.encens.khipus.dashboard.module.cashbox.sql;


import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.18
 */
public class IncomeSql implements SqlQuery {
    public static enum SqlToExecute {
        INVOICE("Income.invoice"),
        CASHBOX("Income.cashbox"),
        BUDGET("Income.budget");

        private String resourceKey;

        SqlToExecute(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        public String getResourceKey() {
            return resourceKey;
        }
    }

    private SqlToExecute sqlToExecute;

    private Integer executorUnitCode = null;
    private Date startDate = DateUtils.getDate(DateUtils.getCurrentYear(new Date()), 1, 1);
    private Date endDate = new Date();

    public String getSql() {
        if (SqlToExecute.INVOICE.equals(sqlToExecute)) {
            return getInvoiceSql();
        }

        if (SqlToExecute.BUDGET.equals(sqlToExecute)) {
            return getBudgetSql();
        }

        if (SqlToExecute.CASHBOX.equals(sqlToExecute)) {
            return getCashboxSql();
        }

        return null;
    }

    private String getBudgetSql() {
        BudgetSql sql = new BudgetSql();
        sql.setTableName("presupuestoingreso");
        sql.setExecutorUnitCode(executorUnitCode);
        sql.setMultiplier("-1");
        sql.setType("ENTRY");

        return sql.getSql();
    }

    private String getInvoiceSql() {
        String sql = "SELECT MES, \n" +
                "       ANIO, \n" +
                "       MES_LITERAL, \n" +
                "       MONTO_B MONTO_BS, \n" +
                "       MONTO_D MONTO_DOL,\n" +
                "       (ROUND(MONTO_B/" + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO), 2) + MONTO_D) TOTAL_DOL,\n" +
                "       " + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO) TIPO_CAMBIO\n" +
                " FROM (SELECT TO_CHAR(M.FECHA,'MM') MES, TO_CHAR(M.FECHA,'YYYY') ANIO,\n" +
                "             DECODE(TO_CHAR(M.FECHA,'MM'), '01','ENERO','02','FEBRERO','03','MARZO','04','ABRIL','05','MAYO','06','JUNIO','07','JULIO','08','AGOSTO','09','SEPTIEMBRE','10','OCTUBRE','11','NOVIEMBRE','12','DICIEMBRE') MES_LITERAL,\n" +
                "             SUM(M.MONTO_B) MONTO_B, SUM(M.MONTO_D) MONTO_D\n" +
                "      FROM (SELECT  TRUNC(M.FECHA) FECHA,\n" +
                "                    SUM(DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0)) MONTO_B,\n" +
                "                    SUM(DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0)) MONTO_D\n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".B_MOVIMIENTOS M, \n" +
                "                 " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST\n" +
                "            WHERE M.ESTADO = 'V' \n" +
                "                  AND M.EST_COD = EST.CODIGO \n" +
                "                  AND TO_NUMBER(TO_CHAR(M.FECHA,'YYYY') || TO_CHAR(M.FECHA,'MM') || TO_CHAR(M.FECHA,'DD')) >= " + DateUtils.dateToInteger(startDate) + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(M.FECHA,'YYYY') || TO_CHAR(M.FECHA,'MM') || TO_CHAR(M.FECHA,'DD')) <= " + DateUtils.dateToInteger(endDate) + "\n";
        sql += addFilters();

        sql += "            GROUP BY TRUNC(M.FECHA)\n" +
                "            UNION\n" +
                "            SELECT TRUNC(M.FECHA) FECHA,\n" +
                "                   SUM(DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0)) MONTO_B,\n" +
                "                   SUM(DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0)) MONTO_D\n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".MOVIMIENTOS M, \n" +
                "                 " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST\n" +
                "            WHERE M.ESTADO = 'V' \n" +
                "                  AND M.EST_COD = EST.CODIGO \n" +
                "                  AND TO_NUMBER(TO_CHAR(M.FECHA,'YYYY') || TO_CHAR(M.FECHA,'MM') || TO_CHAR(M.FECHA,'DD')) >= " + DateUtils.dateToInteger(startDate) + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(M.FECHA,'YYYY') || TO_CHAR(M.FECHA,'MM') || TO_CHAR(M.FECHA,'DD')) <= " + DateUtils.dateToInteger(endDate) + "\n";
        sql += addFilters();

        sql += "            GROUP BY TRUNC(M.FECHA)) M\n" +
                "       GROUP BY TO_CHAR(M.FECHA,'MM'), TO_CHAR(M.FECHA,'YYYY')\n" +
                ")ORDER BY MES";

        return sql;
    }


    private String getCashboxSql() {
        String sql = "SELECT TO_NUMBER(MES) MES, \n"
                + " TO_NUMBER(ANIO) ANIO, MES_LITERAL,\n"
                + " MONTO_B TOTAL_CONCEPTOS_BS,\n"
                + " MONTO_D TOTAL_CONCEPTOS_DOL,\n"
                + " (ROUND(MONTO_B/" + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO), 2)+ MONTO_D) TOTAL_DOL,\n"
                + " " + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO) TIPO_CAMBIO\n"
                + " FROM (SELECT TO_CHAR(M.FECHA,'MM') MES, TO_CHAR(M.FECHA,'YYYY') ANIO,\n"
                + " DECODE(TO_CHAR(M.FECHA,'MM'), '01','ENERO','02','FEBRERO','03','MARZO','04','ABRIL','05','MAYO','06','JUNIO','07','JULIO','08','AGOSTO','09','SEPTIEMBRE','10','OCTUBRE','11','NOVIEMBRE','12','DICIEMBRE') MES_LITERAL,\n"
                + " SUM(M.MONTO_B) MONTO_B,\n"
                + " SUM(M.MONTO_D) MONTO_D\n"
                + " FROM (SELECT TRUNC(TF.FECHA) FECHA,\n"
                + " SUM(TF.EFECTIVO_B-TF.CAMBIO_B) MONTO_B,\n"
                + " SUM(TF.EFECTIVO_D-TF.CAMBIO_D) MONTO_D \n"
                + " FROM " + Constants.CASHBOX_SCHEMA + ".TOTALES_FACTURAS TF,\n"
                + " " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST\n"
                + " WHERE TF.ESTADO = 'V'\n"
                + " AND TF.EST_COD = EST.CODIGO\n"
                + " AND TO_NUMBER(TO_CHAR(TF.FECHA,'YYYY') || TO_CHAR(TF.FECHA,'MM') || TO_CHAR(TF.FECHA,'DD')) >= " + DateUtils.dateToInteger(startDate) + "\n"
                + " AND TO_NUMBER(TO_CHAR(TF.FECHA,'YYYY') || TO_CHAR(TF.FECHA,'MM') || TO_CHAR(TF.FECHA,'DD')) <= " + DateUtils.dateToInteger(endDate) + "\n";

        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + " \n";
        }

        sql += " GROUP BY TF.FECHA \n"
                + " UNION ALL\n"
                + " SELECT TRUNC(CD.FECHA) FECHA,\n"
                + " SUM(DECODE(CD.MONEDA,'B',CD.IMPORTE,0) ) MONTO_B,\n"
                + " SUM(DECODE(CD.MONEDA,'D',CD.IMPORTE,0) ) MONTO_D\n"
                + " FROM " + Constants.CASHBOX_SCHEMA + ".CHEQUES_DEPOSITOS CD,\n"
                + " " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST\n"
                + " WHERE CD.ESTADO = 'V'\n"
                + " AND CD.EST_COD = EST.CODIGO\n"
                + " AND CD.TIPO = 'C'\n"
                + " AND TO_NUMBER(TO_CHAR(CD.FECHA,'YYYY') || TO_CHAR(CD.FECHA,'MM') || TO_CHAR(CD.FECHA,'DD')) >= " + DateUtils.dateToInteger(startDate) + "\n"
                + " AND TO_NUMBER(TO_CHAR(CD.FECHA,'YYYY') || TO_CHAR(CD.FECHA,'MM') || TO_CHAR(CD.FECHA,'DD')) <= " + DateUtils.dateToInteger(endDate) + "\n";

        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + " \n";
        }

        sql += " GROUP BY CD.FECHA \n"
                + " UNION ALL\n"
                + " SELECT TRUNC(CD.FECHA) FECHA,\n"
                + " SUM(DECODE(CD.MONEDA,'B',CD.IMPORTE,0) ) MONTO_B,\n"
                + " SUM(DECODE(CD.MONEDA,'D',CD.IMPORTE,0) ) MONTO_D\n"
                + " FROM " + Constants.CASHBOX_SCHEMA + ".CHEQUES_DEPOSITOS CD," + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST \n"
                + " WHERE CD.ESTADO = 'V'\n"
                + " AND CD.EST_COD = EST.CODIGO \n"
                + " AND CD.TIPO = 'D'\n"
                + " AND TO_NUMBER(TO_CHAR(CD.FECHA,'YYYY') || TO_CHAR(CD.FECHA,'MM') || TO_CHAR(CD.FECHA,'DD')) >= " + DateUtils.dateToInteger(startDate) + "\n"
                + " AND TO_NUMBER(TO_CHAR(CD.FECHA,'YYYY') || TO_CHAR(CD.FECHA,'MM') || TO_CHAR(CD.FECHA,'DD')) <= " + DateUtils.dateToInteger(endDate) + "\n";

        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + "  \n";
        }

        sql += " GROUP BY CD.FECHA\n"
                + " ) M\n"
                + " GROUP BY TO_CHAR(M.FECHA,'MM'), TO_CHAR(M.FECHA,'YYYY')\n"
                + " ) ORDER BY MES";
        return sql;
    }

    public static List<SqlToExecute> getSqlToExecuteConstants() {
        return Arrays.asList(SqlToExecute.values());
    }

    public void setSqlToExecute(SqlToExecute sqlToExecute) {
        this.sqlToExecute = sqlToExecute;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    private String addFilters() {
        String sql = "";
        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + "\n";
        }
        return sql;
    }
}
