package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.7
 */
public class HumanResourcesExpenseExtendedSqlQuery implements SqlQuery {
    private Integer executorUnitCode;
    private Integer year;

    public String getSql() {
        String sql = "SELECT \n" +
                "CUENTA,\n" +
                "DESCRI,\n" +
                "\n" +
                "ENE_MONTOBS,\n" +
                "ENE_MONTOSUS,\n" +
                "ENE_TCBSSUS,\n" +
                "ROUND((DECODE(ENE_MONTOBS,NULL,0,ENE_MONTOBS,ENE_MONTOBS)/ENE_TCBSSUS + DECODE(ENE_MONTOSUS,NULL,0,ENE_MONTOSUS,ENE_MONTOSUS)),2) ENE_TOTALSUS,\n" +
                "\n" +
                "FEB_MONTOBS,\n" +
                "FEB_MONTOSUS,\n" +
                "FEB_TCBSSUS,\n" +
                "ROUND((DECODE(FEB_MONTOBS,NULL,0,FEB_MONTOBS,FEB_MONTOBS)/FEB_TCBSSUS + DECODE(FEB_MONTOSUS,NULL,0,FEB_MONTOSUS,FEB_MONTOSUS)),2) FEB_TOTALSUS,\n" +
                "\n" +
                "MAR_MONTOBS,\n" +
                "MAR_MONTOSUS,\n" +
                "MAR_TCBSSUS,\n" +
                "ROUND((DECODE(MAR_MONTOBS,NULL,0,MAR_MONTOBS,MAR_MONTOBS)/MAR_TCBSSUS + DECODE(MAR_MONTOSUS,NULL,0,MAR_MONTOSUS,MAR_MONTOSUS)),2) MAR_TOTALSUS,\n" +
                "\n" +
                "ABR_MONTOBS,\n" +
                "ABR_MONTOSUS,\n" +
                "ABR_TCBSSUS,\n" +
                "ROUND((DECODE(ABR_MONTOBS,NULL,0,ABR_MONTOBS,ABR_MONTOBS)/ABR_TCBSSUS + DECODE(ABR_MONTOSUS,NULL,0,ABR_MONTOSUS,ABR_MONTOSUS)),2) ABR_TOTALSUS,\n" +
                "\n" +
                "MAY_MONTOBS,\n" +
                "MAY_MONTOSUS,\n" +
                "MAY_TCBSSUS,\n" +
                "ROUND((DECODE(MAY_MONTOBS,NULL,0,MAY_MONTOBS,MAY_MONTOBS)/MAY_TCBSSUS + DECODE(MAY_MONTOSUS,NULL,0,MAY_MONTOSUS,MAY_MONTOSUS)),2) MAY_TOTALSUS,\n" +
                "\n" +
                "JUN_MONTOBS,\n" +
                "JUN_MONTOSUS,\n" +
                "JUN_TCBSSUS,\n" +
                "ROUND((DECODE(JUN_MONTOBS,NULL,0,JUN_MONTOBS,JUN_MONTOBS)/JUN_TCBSSUS + DECODE(JUN_MONTOSUS,NULL,0,JUN_MONTOSUS,JUN_MONTOSUS)),2) JUN_TOTALSUS,\n" +
                "\n" +
                "JUL_MONTOBS,\n" +
                "JUL_MONTOSUS,\n" +
                "JUL_TCBSSUS,\n" +
                "ROUND((DECODE(JUL_MONTOBS,NULL,0,JUL_MONTOBS,JUL_MONTOBS)/JUL_TCBSSUS + DECODE(JUL_MONTOSUS,NULL,0,JUL_MONTOSUS,JUL_MONTOSUS)),2) JUL_TOTALSUS,\n" +
                "\n" +
                "AGO_MONTOBS,\n" +
                "AGO_MONTOSUS,\n" +
                "AGO_TCBSSUS,\n" +
                "ROUND((DECODE(AGO_MONTOBS,NULL,0,AGO_MONTOBS,AGO_MONTOBS)/AGO_TCBSSUS + DECODE(AGO_MONTOSUS,NULL,0,AGO_MONTOSUS,AGO_MONTOSUS)),2) AGO_TOTALSUS,\n" +
                "\n" +
                "SEP_MONTOBS,\n" +
                "SEP_MONTOSUS,\n" +
                "SEP_TCBSSUS,\n" +
                "ROUND((DECODE(SEP_MONTOBS,NULL,0,SEP_MONTOBS,SEP_MONTOBS)/SEP_TCBSSUS + DECODE(SEP_MONTOSUS,NULL,0,SEP_MONTOSUS,SEP_MONTOSUS)),2) SEP_TOTALSUS,\n" +
                "\n" +
                "OCT_MONTOBS,\n" +
                "OCT_MONTOSUS,\n" +
                "OCT_TCBSSUS,\n" +
                "ROUND((DECODE(OCT_MONTOBS,NULL,0,OCT_MONTOBS,OCT_MONTOBS)/OCT_TCBSSUS + DECODE(OCT_MONTOSUS,NULL,0,OCT_MONTOSUS,OCT_MONTOSUS)),2) OCT_TOTALSUS,\n" +
                "\n" +
                "NOV_MONTOBS,\n" +
                "NOV_MONTOSUS,\n" +
                "NOV_TCBSSUS,\n" +
                "ROUND((DECODE(NOV_MONTOBS,NULL,0,NOV_MONTOBS,NOV_MONTOBS)/NOV_TCBSSUS + DECODE(NOV_MONTOSUS,NULL,0,NOV_MONTOSUS,NOV_MONTOSUS)),2) NOV_TOTALSUS,\n" +
                "\n" +
                "DIC_MONTOBS,\n" +
                "DIC_MONTOSUS,\n" +
                "DIC_TCBSSUS,\n" +
                "ROUND((DECODE(DIC_MONTOBS,NULL,0,DIC_MONTOBS,DIC_MONTOBS)/DIC_TCBSSUS + DECODE(DIC_MONTOSUS,NULL,0,DIC_MONTOSUS,DIC_MONTOSUS)),2) DIC_TOTALSUS,\n" +
                "\n" +
                "GLOBAL_MONTOBS,\n" +
                "GLOBAL_MONTOSUS,\n" +
                "GLOBAL_TCBSSUS,\n" +
                "ROUND((DECODE(GLOBAL_MONTOBS,NULL,0,GLOBAL_MONTOBS,GLOBAL_MONTOBS)/GLOBAL_TCBSSUS + DECODE(GLOBAL_MONTOSUS,NULL,0,GLOBAL_MONTOSUS,GLOBAL_MONTOSUS)),2) GLOBAL_TOTALSUS\n" +
                "FROM\n" +
                "(select \n" +
                "  qarg.cuenta CUENTA,\n" +
                "  qarg.descri DESCRI,\n" +
                "  \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 1) ENE_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 1) ENE_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 1) ENE_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 2) FEB_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 2) FEB_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 2) FEB_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 3) MAR_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 3) MAR_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 3) MAR_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 4) ABR_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 4) ABR_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 4) ABR_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 5) MAY_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 5) MAY_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 5) MAY_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 6) JUN_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 6) JUN_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 6) JUN_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 7) JUL_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 7) JUL_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 7) JUL_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 8) AGO_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 8) AGO_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 8) AGO_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 9) SEP_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 9) SEP_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 9) SEP_TCBSSUS,\n" +
                "      \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 10) OCT_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 10) OCT_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 10) OCT_TCBSSUS,\n" +
                "    \n" +
                "  \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 11) NOV_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 11) NOV_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 11) NOV_TCBSSUS,\n" +
                "    \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 12) DIC_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 12) DIC_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();
        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = 12) DIC_TCBSSUS,\n" +
                "    \n" +
                "    \n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'P' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) >= 1\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) <= 12) GLOBAL_MONTOBS,\n" +
                "(SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' and md.cuenta = qarg.cuenta\n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) >= 1\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) <= 12) GLOBAL_MONTOSUS,\n" +
                "(SELECT avg(md.md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE ag.moneda = 'D' \n";

        sql += addFilters();

        sql += "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = " + year + "\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) >= 1\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) <= 12) GLOBAL_TCBSSUS    \n" +
                "from " + Constants.FINANCES_SCHEMA + ".ARCGMS qarg\n" +
                "where qarg.cuenta IN (SELECT DISTINCT qcp.codctactbhaberme FROM " + Constants.KHIPUS_SCHEMA + ".categoriapuesto qcp)\n" +
                " or qarg.cuenta IN (SELECT DISTINCT qcp.codctactbhaber FROM " + Constants.KHIPUS_SCHEMA + ".categoriapuesto qcp) \n" +
                "order by qarg.cuenta) T";
        return sql;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    private String addFilters() {
        String sql = "";
        if (null != executorUnitCode) {
            sql += " AND TO_NUMBER(md.cod_uni)=" + executorUnitCode + "\n";
        }

        return sql;
    }
}
