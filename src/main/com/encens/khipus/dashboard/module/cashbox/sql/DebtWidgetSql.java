package com.encens.khipus.dashboard.module.cashbox.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * @author
 * @version 2.26
 */
public class DebtWidgetSql implements SqlQuery {
    private Integer currentYear = DateUtils.getCurrentYear(new Date());

    private Integer executorUnitId;

    private String carreer;

    public String getSql() {
        String sql = "SELECT COUNT(*), SUM(INSCRITO) NRO_ESTUDIANTES_INSCRITOS, SUM(DEUDA) NRO_ESTUDIANTES_MORA, ROUND(SUM(DEUDA)/SUM(INSCRITO)*100, 2) PORCENTAJE_ESTUD_MORA\n"
                + "FROM (\n"
                + "   SELECT  CXP.PLAN_ESTUDIO, CXP.UNIDAD_ACAD_ADM, CXP.UNIVERSIDAD, CXP.ESTUDIANTE, 1 INSCRITO, 1 PAGADO, 0 DEUDA\n"
                + "   FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP \n"
                + "   WHERE TO_CHAR(CXP.GESTION) = '" + currentYear + "' AND \n"
                + "         CXP.ACTIVO  = 'SI' AND \n"
                + "         CXP.CUENTA_X_PAGAR IN (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,9939,9942,9941,9944) AND \n";
        sql += applyFilters();

        sql += "         (SELECT COUNT(*) \n"
                + "          FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA \n"
                + "          WHERE RA.ESTUDIANTE = CXP.ESTUDIANTE AND\n"
                + "                RA.GESTION  = CXP.GESTION AND\n"
                + "                RA.PERIODO  = CXP.PERIODO AND \n"
                + "                RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO AND \n"
                + "                RA.MODALIDAD = 'NORM') > 0 AND \n"
                + "                NOT EXISTS (SELECT * \n"
                + "                            FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n"
                + "                            WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE AND \n"
                + "                                  DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM AND \n"
                + "                                  DEU.UNIVERSIDAD = CXP.UNIVERSIDAD AND \n"
                + "                                  DEU.GESTION = CXP.GESTION AND \n"
                + "                                  DEU.PERIODO = CXP.PERIODO AND \n"
                + "                                  DEU.ACTIVO = 'SI' AND DEU.ESTADO = 0) AND \n"
                + "                NOT EXISTS (SELECT * \n"
                + "                            FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n"
                + "                            WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE AND\n"
                + "                                  BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO AND \n"
                + "                                  BE.GESTION = CXP.GESTION AND \n"
                + "                                  BE.PERIODO = CXP.PERIODO AND \n"
                + "                                  BE.ESTADO  = 'ACEPTADO')\n"
                + "   GROUP BY CXP.PLAN_ESTUDIO, CXP.UNIDAD_ACAD_ADM, CXP.UNIVERSIDAD, CXP.ESTUDIANTE\n"
                + "   UNION ALL \n"
                + "   SELECT  CXP.PLAN_ESTUDIO, CXP.UNIDAD_ACAD_ADM, CXP.UNIVERSIDAD, CXP.ESTUDIANTE, 1 INSCRITO, 0 PAGADO, 1 DEUDA\n"
                + "   FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP\n"
                + "   WHERE TO_CHAR(CXP.GESTION) = '" + currentYear + "' AND \n"
                + "         CXP.ACTIVO  = 'SI' AND \n"
                + "         CXP.CUENTA_X_PAGAR IN (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,9939,9942,9941,9944) AND \n";
        sql += applyFilters();

        sql += "         (SELECT COUNT(*) \n"
                + "          FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA \n"
                + "          WHERE RA.ESTUDIANTE = CXP.ESTUDIANTE AND \n"
                + "                RA.GESTION = CXP.GESTION AND \n"
                + "                RA.PERIODO = CXP.PERIODO AND \n"
                + "                RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO AND \n"
                + "                RA.MODALIDAD = 'NORM') > 0 AND \n"
                + "                EXISTS (SELECT * \n"
                + "                        FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU \n"
                + "                        WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE AND \n"
                + "                              DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM AND \n"
                + "                              DEU.UNIVERSIDAD = CXP.UNIVERSIDAD AND \n"
                + "                              DEU.GESTION = CXP.GESTION AND \n"
                + "                              DEU.PERIODO = CXP.PERIODO AND \n"
                + "                              DEU.ACTIVO = 'SI' AND DEU.ESTADO = 0) AND \n"
                + "                NOT EXISTS (SELECT * \n"
                + "                            FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE \n"
                + "                            WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE AND \n"
                + "                                  BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO AND \n"
                + "                                  BE.GESTION = CXP.GESTION AND \n"
                + "                                  BE.PERIODO = CXP.PERIODO AND BE.ESTADO = 'ACEPTADO')  \n"
                + "   GROUP BY CXP.PLAN_ESTUDIO, CXP.UNIDAD_ACAD_ADM, CXP.UNIVERSIDAD, CXP.ESTUDIANTE\n"
                + ") CXP";

        return sql;
    }

    private String applyFilters() {
        String sql = "";
        if (null != executorUnitId) {
            sql += " CXP.UNIDAD_ACAD_ADM = " + executorUnitId + " AND \n";
        }

        if (null != carreer && !"".equals(carreer.trim())) {
            sql += " CXP.PLAN_ESTUDIO LIKE '" + carreer + "' AND \n";
        }

        return sql;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }

    public void setCarreer(String carreer) {
        this.carreer = carreer;
    }
}
