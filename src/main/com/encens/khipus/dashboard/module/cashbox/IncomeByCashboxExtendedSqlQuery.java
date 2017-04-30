package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.7
 */
public class IncomeByCashboxExtendedSqlQuery implements SqlQuery {
    private Integer startDate;
    private Integer endDate;

    public String getSql() {
        String sql = "SELECT TO_NUMBER(ANIO||MES) CODIGO,\n" +
                "       TO_NUMBER(MES) MES,\n" +
                "       TO_NUMBER(ANIO) ANIO,\n" +
                "       MES_LITERAL,\n" +
                "       MONTO_B_SEDE_LA_PAZ,\n" +
                "       MONTO_D_SEDE_LA_PAZ,\n" +
                "       MONTO_B_SEDE_STA_CRUZ,\n" +
                "       MONTO_D_SEDE_STA_CRUZ,\n" +
                "       MONTO_B_SEDE_CBBA,\n" +
                "       MONTO_D_SEDE_CBBA,\n" +
                "       MONTO_B_SEDE_ORURO,\n" +
                "       MONTO_D_SEDE_ORURO,\n" +
                "       (MONTO_B_SEDE_LA_PAZ + MONTO_B_SEDE_STA_CRUZ + MONTO_B_SEDE_CBBA + MONTO_B_SEDE_ORURO) TOTAL_CONCEPTOS_BS,\n" +
                "       (MONTO_D_SEDE_LA_PAZ + MONTO_D_SEDE_STA_CRUZ + MONTO_D_SEDE_CBBA + MONTO_D_SEDE_ORURO) TOTAL_CONCEPTOS_DOL,\n" +
                "       (ROUND((MONTO_B_SEDE_LA_PAZ + MONTO_B_SEDE_STA_CRUZ + MONTO_B_SEDE_CBBA + MONTO_B_SEDE_ORURO) / " + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO), 2) + (MONTO_D_SEDE_LA_PAZ + MONTO_D_SEDE_STA_CRUZ + MONTO_D_SEDE_CBBA + MONTO_D_SEDE_ORURO)) TOTAL_DOL,\n" +
                "       " + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO) TIPO_CAMBIO\n" +
                "FROM (SELECT TO_CHAR(M.FECHA,'MM') MES,\n" +
                "             TO_CHAR(M.FECHA,'YYYY') ANIO,\n" +
                "             DECODE(TO_CHAR(M.FECHA,'MM'), '01','ENERO','02','FEBRERO','03','MARZO','04','ABRIL','05','MAYO','06','JUNIO','07','JULIO','08','AGOSTO','09','SEPTIEMBRE','10','OCTUBRE','11','NOVIEMBRE','12','DICIEMBRE') MES_LITERAL,\n" +
                "             SUM(M.MONTO_B_SEDE_LA_PAZ) MONTO_B_SEDE_LA_PAZ,\n" +
                "             SUM(M.MONTO_D_SEDE_LA_PAZ) MONTO_D_SEDE_LA_PAZ,\n" +
                "             SUM(M.MONTO_B_SEDE_STA_CRUZ) MONTO_B_SEDE_STA_CRUZ,\n" +
                "             SUM(M.MONTO_D_SEDE_STA_CRUZ) MONTO_D_SEDE_STA_CRUZ,\n" +
                "             SUM(M.MONTO_B_SEDE_CBBA) MONTO_B_SEDE_CBBA,\n" +
                "             SUM(M.MONTO_D_SEDE_CBBA) MONTO_D_SEDE_CBBA,\n" +
                "             SUM(M.MONTO_B_SEDE_ORURO) MONTO_B_SEDE_ORURO,\n" +
                "             SUM(M.MONTO_D_SEDE_ORURO) MONTO_D_SEDE_ORURO \n" +
                "      FROM (SELECT TRUNC(TF.FECHA) FECHA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'1', (TF.EFECTIVO_B-TF.CAMBIO_B), 0) ) MONTO_B_SEDE_LA_PAZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'1', (TF.EFECTIVO_D-TF.CAMBIO_D), 0) ) MONTO_D_SEDE_LA_PAZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'2', (TF.EFECTIVO_B-TF.CAMBIO_B), 0) ) MONTO_B_SEDE_STA_CRUZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'2', (TF.EFECTIVO_D-TF.CAMBIO_D), 0) ) MONTO_D_SEDE_STA_CRUZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'3', (TF.EFECTIVO_B-TF.CAMBIO_B), 0) ) MONTO_B_SEDE_CBBA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'3', (TF.EFECTIVO_D-TF.CAMBIO_D), 0) ) MONTO_D_SEDE_CBBA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'4', (TF.EFECTIVO_B-TF.CAMBIO_B), 0) ) MONTO_B_SEDE_ORURO,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'4', (TF.EFECTIVO_D-TF.CAMBIO_D), 0) ) MONTO_D_SEDE_ORURO\n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".TOTALES_FACTURAS TF,\n" +
                "                 " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST\n" +
                "            WHERE TF.ESTADO = 'V'\n" +
                "                  AND TF.EST_COD = EST.CODIGO\n";
        sql += setFilters("TF.FECHA");

        sql += "            GROUP BY TF.FECHA\n" +
                "            UNION ALL\n" +
                "            SELECT TRUNC(CD.FECHA) FECHA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'1',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)),0) ) MONTO_B_SEDE_LA_PAZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'1',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_LA_PAZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'2',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)), 0) ) MONTO_B_SEDE_STA_CRUZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'2',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_STA_CRUZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'3',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)), 0) ) MONTO_B_SEDE_CBBA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'3',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_CBBA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'4',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)), 0) ) MONTO_B_SEDE_ORURO,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'4',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_ORURO\n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".CHEQUES_DEPOSITOS CD,\n" +
                "                 " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST\n" +
                "            WHERE CD.ESTADO = 'V'\n" +
                "                  AND CD.EST_COD = EST.CODIGO\n" +
                "                  AND CD.TIPO = 'C'\n";
        sql += setFilters("CD.FECHA");

        sql += "            GROUP BY CD.FECHA\n" +
                "            UNION ALL\n" +
                "            SELECT TRUNC(CD.FECHA) FECHA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'1',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)), 0) ) MONTO_B_SEDE_LA_PAZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'1',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_LA_PAZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'2',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)), 0) ) MONTO_B_SEDE_STA_CRUZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'2',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_STA_CRUZ,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'3',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)), 0) ) MONTO_B_SEDE_CBBA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'3',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_CBBA,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'4',(DECODE(CD.MONEDA,'B',CD.IMPORTE,0)), 0) ) MONTO_B_SEDE_ORURO,\n" +
                "                   SUM(DECODE(EST.UNIDAD_ACAD_ADM,'4',(DECODE(CD.MONEDA,'D',CD.IMPORTE,0)), 0) ) MONTO_D_SEDE_ORURO\n" +
                "            FROM " + Constants.CASHBOX_SCHEMA + ".CHEQUES_DEPOSITOS CD,\n" +
                "                 " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST\n" +
                "            WHERE CD.ESTADO = 'V'\n" +
                "                  AND CD.EST_COD = EST.CODIGO\n" +
                "                  AND CD.TIPO = 'D'\n";
        sql += setFilters("CD.FECHA");

        sql += "            GROUP BY CD.FECHA \n" +
                "        ) M\n" +
                "        GROUP BY TO_CHAR(M.FECHA,'MM'), TO_CHAR(M.FECHA,'YYYY')\n" +
                ") ORDER BY CODIGO";
        return sql;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    private String setFilters(String field) {
        String sql = "";
        if (null != startDate) {
            sql += " AND TO_NUMBER(TO_CHAR(" + field + ",'YYYY') || TO_CHAR(" + field + ",'MM') || TO_CHAR(" + field + ",'DD')) >= " + startDate + "\n";
        }

        if (null != endDate) {
            sql += " AND TO_NUMBER(TO_CHAR(" + field + ",'YYYY') || TO_CHAR(" + field + ",'MM') || TO_CHAR(" + field + ",'DD')) <= " + endDate + "\n";
        }

        return sql;
    }
}
