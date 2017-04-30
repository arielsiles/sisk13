package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * @author
 * @version 2.7
 */
public class IncomeByCashboxSqlQuery implements SqlQuery {
    private Integer executorUnitCode = null;
    private Integer year = DateUtils.getCurrentYear(new Date());
    private Integer startMonth = 1;
    private Integer endMonth = DateUtils.getCurrentMonth(new Date());

    public String getSql() {
        String sql = "SELECT TO_NUMBER(MES) MES, \n" +
                "       TO_NUMBER(ANIO) ANIO, MES_LITERAL,\n" +
                "       MONTO_B TOTAL_CONCEPTOS_BS,\n" +
                "       MONTO_D TOTAL_CONCEPTOS_DOL,\n" +
                "       (ROUND(MONTO_B/" + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO), 2)+ MONTO_D) TOTAL_DOL,\n" +
                "       " + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO) TIPO_CAMBIO\n" +
                "FROM (SELECT TO_CHAR(M.FECHA,'MM') MES, TO_CHAR(M.FECHA,'YYYY') ANIO,\n" +
                "             DECODE(TO_CHAR(M.FECHA,'MM'), '01','ENERO','02','FEBRERO','03','MARZO','04','ABRIL','05','MAYO','06','JUNIO','07','JULIO','08','AGOSTO','09','SEPTIEMBRE','10','OCTUBRE','11','NOVIEMBRE','12','DICIEMBRE') MES_LITERAL,\n" +
                "             SUM(M.MONTO_B) MONTO_B,\n" +
                "             SUM(M.MONTO_D) MONTO_D        \n" +
                "      FROM (SELECT TRUNC(TF.FECHA) FECHA,\n" +
                "                   SUM(TF.EFECTIVO_B-TF.CAMBIO_B) MONTO_B,\n" +
                "                   SUM(TF.EFECTIVO_D-TF.CAMBIO_D) MONTO_D \n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".TOTALES_FACTURAS TF, \n" +
                "                 " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST \n" +
                "            WHERE TF.ESTADO = 'V' \n" +
                "                  AND TF.EST_COD = EST.CODIGO \n" +
                "                  AND TO_NUMBER(TO_CHAR(TF.FECHA,'YYYY')) = " + year + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(TF.FECHA,'MM')) >= " + startMonth + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(TF.FECHA,'MM')) <= " + endMonth + "\n";
        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + " \n";
        }

        sql += "            GROUP BY TF.FECHA \n" +
                "            UNION ALL\n" +
                "            SELECT TRUNC(CD.FECHA) FECHA, \n" +
                "                   SUM(DECODE(CD.MONEDA,'B',CD.IMPORTE,0) ) MONTO_B, \n" +
                "                   SUM(DECODE(CD.MONEDA,'D',CD.IMPORTE,0) ) MONTO_D \n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".CHEQUES_DEPOSITOS CD, \n" +
                "                 " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST \n" +
                "            WHERE CD.ESTADO = 'V' \n" +
                "                  AND CD.EST_COD = EST.CODIGO \n" +
                "                  AND CD.TIPO = 'C' \n" +
                "                  AND TO_NUMBER(TO_CHAR(CD.FECHA,'YYYY')) = " + year + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(CD.FECHA,'MM')) >= " + startMonth + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(CD.FECHA,'MM')) <= " + endMonth + "\n";
        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + " \n";
        }

        sql += "            GROUP BY CD.FECHA \n" +
                "            UNION ALL\n" +
                "            SELECT TRUNC(CD.FECHA) FECHA, \n" +
                "                   SUM(DECODE(CD.MONEDA,'B',CD.IMPORTE,0) ) MONTO_B, \n" +
                "                   SUM(DECODE(CD.MONEDA,'D',CD.IMPORTE,0) ) MONTO_D \n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".CHEQUES_DEPOSITOS CD, " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST \n" +
                "            WHERE CD.ESTADO   = 'V' \n" +
                "                  AND CD.EST_COD = EST.CODIGO \n" +
                "                  AND CD.TIPO = 'D' \n" +
                "                  AND TO_NUMBER(TO_CHAR(CD.FECHA,'YYYY')) = " + year + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(CD.FECHA,'MM')) >= " + startMonth + "\n" +
                "                  AND TO_NUMBER(TO_CHAR(CD.FECHA,'MM')) <= " + endMonth + "\n";
        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + "  \n";
        }

        sql += "            GROUP BY CD.FECHA \n" +
                "        ) M\n" +
                "        GROUP BY TO_CHAR(M.FECHA,'MM'), TO_CHAR(M.FECHA,'YYYY')\n" +
                "    ) ORDER BY MES";
        return sql;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public Date getStartDate() {
        return DateUtils.getDate(year, startMonth, 1);
    }

    public Date getEndDate() {
        return DateUtils.getDate(year, endMonth);
    }
}
