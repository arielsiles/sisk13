package com.encens.khipus.dashboard.module.cashbox.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
public class DebtByStudentReportSql implements SqlQuery {
    private Integer year = DateUtils.getCurrentYear(new Date());
    private Integer period;
    private String executorUnitCode = "%";
    private String faculty = "%";
    private String career = "%";
    private List<String> detailCodeList;


    public String getSql() {
        String sql = "SELECT TO_CHAR(UAA.UNIDAD_ACAD_ADM) CODIGO_SEDE,\n" +
                "       REPLACE(UPPER(UAA.DESCRIPCION), 'SEDE ','') SEDE,\n" +
                "       U.UNIDAD CODIGO_FACULTAD, UPPER(U.DESCRIPCION) FACULTAD,\n" +
                "       PL.PLAN_ESTUDIO CODIGO_CARRERA, UPPER(PL.DESC_PLAN) CARRERA,\n" +
                "       CXP.ESTUDIANTE CODIGO_ESTUDIANTE,\n" +
                "       EST.APELLIDO_PATERNO||' '||EST.APELLIDO_MATERNO||' '||EST.NOMBRES ESTUDIANTE,\n" +
                "       UPPER(C.NOMBRE) CONCEPTO,\n" +
                "       IMPORTE_BS,\n" +
                "       IMPORTE_DOL,\n" +
                "       ROUND((IMPORTE_BS/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE)),2)+IMPORTE_DOL TOTAL_IMPORTE_DOL,\n" +
                "       ROUND(" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2) TIPO_CAMBIO\n" +
                "FROM (SELECT CXP.UNIDAD_ACAD_ADM,\n" +
                "             UNI.UNIDAD,\n" +
                "             CXP.PLAN_ESTUDIO,\n" +
                "             CXP.ESTUDIANTE,\n" +
                "             CXP.CUENTA_X_PAGAR,\n" +
                "             SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0,CXP.IMPORTE,0),0)) IMPORTE_BS,\n" +
                "             SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0,CXP.IMPORTE,0),0)) IMPORTE_DOL\n" +
                "      FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP,\n" +
                "           " + Constants.ACADEMIC_SCHEMA + ".PLANES_ESTUDIOS PL,\n" +
                "           " + Constants.ACADEMIC_SCHEMA + ".UNIDADES UNI\n" +
                "      WHERE CXP.GESTION = " + year + "\n" +
                             addPeriodFilter() +
                "            AND CXP.ACTIVO = 'SI'\n" +
                "            AND CXP.ESTADO = 0\n" +
                "            AND CXP.PLAN_ESTUDIO = PL.PLAN_ESTUDIO\n" +
                "            AND PL.UNIDAD = UNI.UNIDAD\n" +
                "            AND CXP.UNIDAD_ACAD_ADM LIKE '" + executorUnitCode + "'\n" +
                "            AND UNI.UNIDAD LIKE '" + faculty + "'\n" +
                "            AND CXP.PLAN_ESTUDIO LIKE '" + career + "' \n" +
                "            AND CXP.CUENTA_X_PAGAR IN (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,9939,9942,9941,9944)\n" +
                "            AND (SELECT COUNT(*)\n" +
                "                 FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                "                 WHERE RA.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                "                       AND RA.GESTION = CXP.GESTION\n" +
                "                       AND RA.PERIODO  = CXP.PERIODO\n" +
                "                       AND RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                "                       AND RA.MODALIDAD = 'NORM') > 0\n" +
                "                 GROUP BY CXP.UNIDAD_ACAD_ADM, UNI.UNIDAD,\n" +
                "                          CXP.PLAN_ESTUDIO,\n" +
                "                          CXP.ESTUDIANTE,\n" +
                "                          CXP.CUENTA_X_PAGAR) CXP,\n" +
                "      " + Constants.ACADEMIC_SCHEMA + ".UNIDADES U,\n" +
                "      " + Constants.ACADEMIC_SCHEMA + ".PLANES_ESTUDIOS PL,\n" +
                "      " + Constants.ACADEMIC_SCHEMA + ".ESTUDIANTES EST,\n" +
                "      " + Constants.CASHBOX_SCHEMA + ".CUENTAS C,\n" +
                "      " + Constants.ACADEMIC_SCHEMA + ".UNIDADES_ACAD_ADM UAA\n" +
                "WHERE CXP.UNIDAD = U.UNIDAD\n" +
                "      AND CXP.UNIDAD_ACAD_ADM = U.UNIDAD_ACAD_ADM\n" +
                "      AND CXP.PLAN_ESTUDIO    = PL.PLAN_ESTUDIO\n" +
                "      AND CXP.UNIDAD_ACAD_ADM = PL.UNIDAD_ACAD_ADM \n" +
                "      AND CXP.ESTUDIANTE   = EST.ESTUDIANTE\n" +
                "      AND CXP.CUENTA_X_PAGAR = C.ID\n";

        sql += addFilters();

        sql += "      AND CXP.UNIDAD_ACAD_ADM = UAA.UNIDAD_ACAD_ADM\n" +
                "ORDER BY 2,4,6,8,9";

        return sql;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public void setDetailCodeList(List<String> detailCodeList) {
        this.detailCodeList = detailCodeList;
    }

    public String addFilters() {
        String sql = "";
        if (null != detailCodeList) {
            sql += composeDetailCodeFilters();
        }

        return sql;
    }

    private String addPeriodFilter() {
        String sql = "";
        if (period != null) {
            sql += "    AND CXP.PERIODO = " + period + " \n";
        }
        return sql;
    }

    private String composeDetailCodeFilters() {
        String sql = "";
        if (detailCodeList != null && !detailCodeList.isEmpty()) {
            sql += "     AND (";
            for (int i = 0; i < detailCodeList.size(); i++) {
                if (i != 0) {
                    sql += " OR ";
                }
                sql += " C.ID = " + detailCodeList.get(i);
            }
            sql += ") \n";
        }
        return sql;
    }
}
