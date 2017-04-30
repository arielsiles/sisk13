package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * @author
 * @version 2.7
 */
public class HumanResourcesSqlQuery implements SqlQuery {
    private Integer executorUnitId = null;
    private Integer year = DateUtils.getCurrentYear(new Date());
    private Integer startMonth = 1;
    private Integer endMonth = DateUtils.getCurrentMonth(new Date());

    public String getSql() {
        String sql = "SELECT" +
                " P.MES,\n" +
                " P.ANIO,\n" +
                "(SELECT sum(md.monto_mn)\n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN  " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE md.cuenta IN (SELECT DISTINCT cp.codctactbhaber FROM " + Constants.KHIPUS_SCHEMA + ".categoriapuesto cp) \n" +
                (executorUnitId != null ? "   AND TO_NUMBER(md.cod_uni)=" + executorUnitId + "\n" : "") +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = P.ANIO\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = P.MES\n" +
                "  ) TOTALMN,       \n" +
                "  (SELECT sum(md.monto_mn/md.tc) \n" +
                "  FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE mm \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET md on mm.no_compro=md.no_compro \n" +
                "  LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS ag on ag.cuenta=md.cuenta\n" +
                "  WHERE md.cuenta IN (SELECT DISTINCT cp.codctactbhaberme FROM " + Constants.KHIPUS_SCHEMA + ".categoriapuesto cp)\n" +
                (executorUnitId != null ? "   AND TO_NUMBER(md.cod_uni)=" + executorUnitId + "\n" : "") +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'YYYY')) = P.ANIO\n" +
                "    AND TO_NUMBER(TO_CHAR(mm.FECHA,'MM')) = P.MES\n" +
                "  ) TOTALME,       \n" +
                "P.MES_LITERAL\n" +
                "FROM (\n" +
                "SELECT DISTINCT \n" +
                "  TO_NUMBER(TO_CHAR(qmm.FECHA,'MM')) MES,  \n" +
                "  TO_NUMBER(TO_CHAR(qmm.FECHA,'YYYY')) ANIO,\n" +
                "  DECODE(TO_CHAR(qmm.FECHA,'MM'), '01','ENERO','02','FEBRERO','03','MARZO','04','ABRIL','05','MAYO','06','JUNIO','07','JULIO','08','AGOSTO','09','SEPTIEMBRE','10','OCTUBRE','11','NOVIEMBRE','12','DICIEMBRE') MES_LITERAL  \n" +
                " FROM " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE qmm\n" +
                " LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVDET qmd on qmm.no_compro=qmd.no_compro\n" +
                " LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS qag on qag.cuenta=qmd.cuenta\n" +
                " WHERE (qmd.cuenta IN (SELECT DISTINCT qcp.codctactbhaber FROM " + Constants.KHIPUS_SCHEMA + ".categoriapuesto qcp) OR\n" +
                "        qmd.cuenta IN (SELECT DISTINCT qcp.codctactbhaberme FROM " + Constants.KHIPUS_SCHEMA + ".categoriapuesto qcp))\n" +
                (executorUnitId != null ? "   AND TO_NUMBER(qmd.cod_uni)=" + executorUnitId + "\n" : "") +
                "   AND TO_NUMBER(TO_CHAR(qmm.FECHA,'YYYY')) = " + year + "\n" +
                "   AND TO_NUMBER(TO_CHAR(qmm.FECHA,'MM')) >= " + startMonth + "\n" +
                "   AND TO_NUMBER(TO_CHAR(qmm.FECHA,'MM')) <= " + endMonth + "\n" +
                " ORDER BY TO_NUMBER(TO_CHAR(qmm.FECHA,'YYYY')), TO_CHAR(qmm.FECHA,'MM')) P";
        return sql;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }

    public Integer getYear() {
        return year;
    }
}
