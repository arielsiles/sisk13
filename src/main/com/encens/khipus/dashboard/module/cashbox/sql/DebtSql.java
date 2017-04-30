package com.encens.khipus.dashboard.module.cashbox.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * @author
 * @version 2.17
 */
public class DebtSql implements SqlQuery {
    private Integer year = DateUtils.getCurrentYear(new Date());
    private Integer businessUnitCode;

    public DebtSql(Integer executorUnitId) {
        this.businessUnitCode = executorUnitId;
    }


    public String getSql() {
        if (!isByBusinessUnit()) {
            return "SELECT U.POSICION, CXP.UNIDAD_ACAD_ADM CODIGO_SEDE, UPPER(U.DESCRIPCION) SEDE,\n" +
                    "SUM(INSCRITO) NRO_INSCRITOS_DEUDACREADA, SUM(PAGADO) NRO_INSCRITOS_PAGARON, SUM(DEUDA) NRO_INSCRITOS_DEUDORES,\n" +
                    "SUM(BECADO_INSCRITO) NRO_BECADOS_DEUDACREADA, SUM(BECADO_PAGADO) NRO_BECADOS_PAGARON, SUM(BECADO_DEUDA) NRO_BECADOS_DEUDORES,\n" +
                    "SUM(ABANDONO_INSCRITO) NRO_ABANDONO_DEUDACREADA, SUM(ABANDONO_PAGADO) NRO_ABANDONO_PAGARON, SUM(ABANDONO_DEUDA) NRO_ABANDONO_DEUDORES,\n" +
                    "ROUND(SUM(INSCRITOS_TOTAL_REAL_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(INSCRITOS_TOTAL_REAL_D) TOTAL_INS_DEUDACREADA_D,\n" +
                    "ROUND(SUM(INSCRITOS_TOTAL_PAGADO_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(INSCRITOS_TOTAL_PAGADO_D) TOTAL_INS_PAGADO_D,\n" +
                    "ROUND(SUM(INSCRITOS_TOTAL_DEUDA_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(INSCRITOS_TOTAL_DEUDA_D) TOTAL_INS_DEUDA_D,\n" +
                    "ROUND(SUM(BECADOS_TOTAL_REAL_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(BECADOS_TOTAL_REAL_D) TOTAL_BEC_DEUDACREADA_D,\n" +
                    "ROUND(SUM(BECADOS_TOTAL_PAGADO_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(BECADOS_TOTAL_PAGADO_D) TOTAL_BEC_PAGADO_D,\n" +
                    "ROUND(SUM(BECADOS_TOTAL_DEUDA_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(BECADOS_TOTAL_DEUDA_D) TOTAL_BEC_DEUDA_D,\n" +
                    "ROUND(SUM(ABANDONO_TOTAL_REAL_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(ABANDONO_TOTAL_REAL_D) TOTAL_ABA_DEUDACREADA_D,  \n" +
                    "ROUND(SUM(ABANDONO_TOTAL_PAGADO_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(ABANDONO_TOTAL_PAGADO_D) TOTAL_ABA_PAGADO_D,\n" +
                    "ROUND(SUM(ABANDONO_TOTAL_DEUDA_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(ABANDONO_TOTAL_DEUDA_D) TOTAL_ABA_DEUDA_D\n" +
                    "FROM (\n" +
                    "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,        \n" +
                    "    1 INSCRITO, 1 PAGADO, 0 DEUDA,\n" +
                    "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n" +
                    "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,      \n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_B,        \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_D,\n" +
                    "    0 BECADOS_TOTAL_REAL_B,        \n" +
                    "    0 BECADOS_TOTAL_REAL_D,\n" +
                    "    0 ABANDONO_TOTAL_REAL_B,        \n" +
                    "    0 ABANDONO_TOTAL_REAL_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_B,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_D,    \n" +
                    "    0 BECADOS_TOTAL_PAGADO_B,\n" +
                    "    0 BECADOS_TOTAL_PAGADO_D,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_B,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_B,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_D,\n" +
                    "    0 BECADOS_TOTAL_DEUDA_B,    \n" +
                    "    0 BECADOS_TOTAL_DEUDA_D,\n" +
                    "    0 ABANDONO_TOTAL_DEUDA_B,    \n" +
                    "    0 ABANDONO_TOTAL_DEUDA_D\n" +
                    "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP\n" +
                    "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n" +
                    "    AND     CXP.ACTIVO  = 'SI'       \n" +
                    "    AND     CXP.CUENTA_X_PAGAR IN            \n" +
                    "(4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,\n" +
                    "            9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,\n" +
                    "            4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,\n" +
                    "            9940,9682,9943,9683,9939,9942,9941,9944)\n" +
                    "    AND     (   SELECT  COUNT(*)\n" +
                    "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                AND     RA.GESTION  = CXP.GESTION\n" +
                    "                AND     RA.PERIODO  = CXP.PERIODO\n" +
                    "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                AND     RA.MODALIDAD = 'NORM') > 0    \n" +
                    "    AND EXISTS (    SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n" +
                    "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n" +
                    "                    AND CXP.GESTION = RA.GESTION\n" +
                    "                    AND CXP.PERIODO = RA.PERIODO\n" +
                    "                    AND RA.MODALIDAD = 'NORM'\n" +
                    "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n" +
                    "                )\n" +
                    "    AND NOT EXISTS (SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n" +
                    "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                    AND BE.GESTION = CXP.GESTION\n" +
                    "                    AND BE.PERIODO = CXP.PERIODO\n" +
                    "                    AND BE.ESTADO  = 'ACEPTADO')  \n" +
                    "    AND     NOT EXISTS (SELECT *\n" +
                    "                        FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n" +
                    "                        WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n" +
                    "                        AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n" +
                    "                        AND DEU.UNIVERSIDAD     = CXP.UNIVERSIDAD\n" +
                    "                        AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n" +
                    "                        AND DEU.GESTION = CXP.GESTION\n" +
                    "                        AND DEU.PERIODO = CXP.PERIODO\n" +
                    "                        AND DEU.ACTIVO  = 'SI'\n" +
                    "                        AND DEU.ESTADO  = 0)\n" +
                    "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE                \n" +
                    "    UNION ALL \n" +
                    "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,       \n" +
                    "    1 INSCRITO, 0 PAGADO, 1 DEUDA,\n" +
                    "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n" +
                    "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_B,        \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_D,\n" +
                    "    0 BECADOS_TOTAL_REAL_B,        \n" +
                    "    0 BECADOS_TOTAL_REAL_D,\n" +
                    "    0 ABANDONO_TOTAL_REAL_B,        \n" +
                    "    0 ABANDONO_TOTAL_REAL_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_B,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_D,    \n" +
                    "    0 BECADOS_TOTAL_PAGADO_B,\n" +
                    "    0 BECADOS_TOTAL_PAGADO_D,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_B,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_B,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_D,\n" +
                    "    0 BECADOS_TOTAL_DEUDA_B,    \n" +
                    "    0 BECADOS_TOTAL_DEUDA_D,\n" +
                    "    0 ABANDONO_TOTAL_DEUDA_B,    \n" +
                    "    0 ABANDONO_TOTAL_DEUDA_D\n" +
                    "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP \n" +
                    "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n" +
                    "    AND     CXP.ACTIVO  = 'SI'                \n" +
                    "    AND     CXP.CUENTA_X_PAGAR IN            \n" +
                    "(4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,\n" +
                    "            9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,\n" +
                    "            4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,\n" +
                    "            9940,9682,9943,9683,9939,9942,9941,9944)\n" +
                    "    AND     (   SELECT  COUNT(*)\n" +
                    "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                AND     RA.GESTION  = CXP.GESTION\n" +
                    "                AND     RA.PERIODO  = CXP.PERIODO\n" +
                    "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                AND     RA.MODALIDAD = 'NORM') > 0  \n" +
                    "    AND EXISTS (    SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n" +
                    "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n" +
                    "                    AND CXP.GESTION = RA.GESTION\n" +
                    "                    AND CXP.PERIODO = RA.PERIODO\n" +
                    "                    AND RA.MODALIDAD = 'NORM'\n" +
                    "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n" +
                    "                )\n" +
                    "    AND NOT EXISTS (SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n" +
                    "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                    AND BE.GESTION = CXP.GESTION\n" +
                    "                    AND BE.PERIODO = CXP.PERIODO\n" +
                    "                    AND BE.ESTADO  = 'ACEPTADO')  \n" +
                    "    AND     EXISTS (SELECT *\n" +
                    "                    FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n" +
                    "                    WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n" +
                    "                    AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n" +
                    "                    AND DEU.UNIVERSIDAD = CXP.UNIVERSIDAD\n" +
                    "                    AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n" +
                    "                    AND DEU.GESTION = CXP.GESTION\n" +
                    "                    AND DEU.PERIODO = CXP.PERIODO\n" +
                    "                    AND DEU.ACTIVO = 'SI'\n" +
                    "                    AND DEU.ESTADO = 0)\n" +
                    "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE \n" +
                    "    UNION ALL \n" +
                    "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,        \n" +
                    "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n" +
                    "    1 BECADO_INSCRITO, 1 BECADO_PAGADO, 0 BECADO_DEUDA,    \n" +
                    "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,  \n" +
                    "    0 INSCRITOS_TOTAL_REAL_B,        \n" +
                    "    0 INSCRITOS_TOTAL_REAL_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_B,        \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_D,\n" +
                    "    0 ABANDONO_TOTAL_REAL_B,        \n" +
                    "    0 ABANDONO_TOTAL_REAL_D,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_B,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_D,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_B,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_D,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_B,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_D,\n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_B,    \n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_B,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_D,\n" +
                    "    0 ABANDONO_TOTAL_DEUDA_B,    \n" +
                    "    0 ABANDONO_TOTAL_DEUDA_D\n" +
                    "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP\n" +
                    "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n" +
                    "    AND     CXP.ACTIVO  = 'SI'       \n" +
                    "    AND     CXP.CUENTA_X_PAGAR IN           \n" +
                    "(4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,\n" +
                    "            9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,\n" +
                    "            4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,\n" +
                    "            9940,9682,9943,9683,9939,9942,9941,9944)\n" +
                    "    AND     (   SELECT  COUNT(*)\n" +
                    "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                AND     RA.GESTION  = CXP.GESTION\n" +
                    "                AND     RA.PERIODO  = CXP.PERIODO\n" +
                    "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                AND     RA.MODALIDAD = 'NORM') > 0    \n" +
                    "    AND EXISTS (    SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n" +
                    "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n" +
                    "                    AND CXP.GESTION = RA.GESTION\n" +
                    "                    AND CXP.PERIODO = RA.PERIODO\n" +
                    "                    AND RA.MODALIDAD = 'NORM'\n" +
                    "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n" +
                    "                )\n" +
                    "    AND EXISTS (    SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n" +
                    "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                    AND BE.GESTION = CXP.GESTION\n" +
                    "                    AND BE.PERIODO = CXP.PERIODO\n" +
                    "                    AND BE.ESTADO  = 'ACEPTADO')  \n" +
                    "    AND     NOT EXISTS (SELECT *\n" +
                    "                        FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n" +
                    "                        WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n" +
                    "                        AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n" +
                    "                        AND DEU.UNIVERSIDAD = CXP.UNIVERSIDAD\n" +
                    "                        AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n" +
                    "                        AND DEU.GESTION = CXP.GESTION\n" +
                    "                        AND DEU.PERIODO = CXP.PERIODO\n" +
                    "                        AND DEU.ACTIVO = 'SI'\n" +
                    "                        AND DEU.ESTADO = 0)\n" +
                    "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE                \n" +
                    "    UNION ALL \n" +
                    "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,       \n" +
                    "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n" +
                    "    1 BECADO_INSCRITO, 0 BECADO_PAGADO, 1 BECADO_DEUDA, \n" +
                    "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,\n" +
                    "    0 INSCRITOS_TOTAL_REAL_B,        \n" +
                    "    0 INSCRITOS_TOTAL_REAL_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_B,        \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_D,\n" +
                    "    0 ABANDONO_TOTAL_REAL_B,        \n" +
                    "    0 ABANDONO_TOTAL_REAL_D,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_B,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_D,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_B,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_D,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_B,\n" +
                    "    0 ABANDONO_TOTAL_PAGADO_D,\n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_B,    \n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_B,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_D,\n" +
                    "    0 ABANDONO_TOTAL_DEUDA_B,    \n" +
                    "    0 ABANDONO_TOTAL_DEUDA_D   \n" +
                    "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP \n" +
                    "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n" +
                    "    AND     CXP.ACTIVO  = 'SI'                \n" +
                    "    AND     CXP.CUENTA_X_PAGAR IN            \n" +
                    "(4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,\n" +
                    "            9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,\n" +
                    "            4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,\n" +
                    "            9940,9682,9943,9683,9939,9942,9941,9944)\n" +
                    "    AND     (   SELECT  COUNT(*)\n" +
                    "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                AND     RA.GESTION  = CXP.GESTION\n" +
                    "                AND     RA.PERIODO  = CXP.PERIODO\n" +
                    "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                AND     RA.MODALIDAD = 'NORM') > 0  \n" +
                    "    AND EXISTS (    SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n" +
                    "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n" +
                    "                    AND CXP.GESTION = RA.GESTION\n" +
                    "                    AND CXP.PERIODO = RA.PERIODO\n" +
                    "                    AND RA.MODALIDAD = 'NORM'\n" +
                    "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n" +
                    "                )\n" +
                    "    AND EXISTS (    SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n" +
                    "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                    AND BE.GESTION = CXP.GESTION\n" +
                    "                    AND BE.PERIODO = CXP.PERIODO\n" +
                    "                    AND BE.ESTADO  = 'ACEPTADO')  \n" +
                    "    AND     EXISTS (SELECT *\n" +
                    "                    FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n" +
                    "                    WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n" +
                    "                    AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n" +
                    "                    AND DEU.UNIVERSIDAD = CXP.UNIVERSIDAD\n" +
                    "                    AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n" +
                    "                    AND DEU.GESTION = CXP.GESTION\n" +
                    "                    AND DEU.PERIODO = CXP.PERIODO\n" +
                    "                    AND DEU.ACTIVO = 'SI'\n" +
                    "                    AND DEU.ESTADO = 0)\n" +
                    "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE \n" +
                    "    UNION ALL \n" +
                    "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,        \n" +
                    "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n" +
                    "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n" +
                    "    1 ABANDONO_INSCRITO, 1 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,\n" +
                    "    0 INSCRITOS_TOTAL_REAL_B,        \n" +
                    "    0 INSCRITOS_TOTAL_REAL_D,\n" +
                    "    0 BECADOS_TOTAL_REAL_B,        \n" +
                    "    0 BECADOS_TOTAL_REAL_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_B,        \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_D,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_B,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_D,    \n" +
                    "    0 BECADOS_TOTAL_PAGADO_B,\n" +
                    "    0 BECADOS_TOTAL_PAGADO_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_B,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_D,\n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_B,    \n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_D,\n" +
                    "    0 BECADOS_TOTAL_DEUDA_B,    \n" +
                    "    0 BECADOS_TOTAL_DEUDA_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_B,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_D  \n" +
                    "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP\n" +
                    "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n" +
                    "    AND     CXP.ACTIVO  = 'SI'       \n" +
                    "    AND     CXP.CUENTA_X_PAGAR IN            \n" +
                    "(4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,\n" +
                    "            9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,\n" +
                    "            4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,\n" +
                    "            9940,9682,9943,9683,9939,9942,9941,9944)\n" +
                    "    AND     (   SELECT  COUNT(*)\n" +
                    "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                AND     RA.GESTION  = CXP.GESTION\n" +
                    "                AND     RA.PERIODO  = CXP.PERIODO\n" +
                    "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                AND     RA.MODALIDAD = 'NORM') > 0    \n" +
                    "    AND NOT EXISTS (SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n" +
                    "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n" +
                    "                    AND CXP.GESTION = RA.GESTION\n" +
                    "                    AND CXP.PERIODO = RA.PERIODO\n" +
                    "                    AND RA.MODALIDAD = 'NORM'\n" +
                    "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n" +
                    "                    )    \n" +
                    "    AND     NOT EXISTS (SELECT *\n" +
                    "                        FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n" +
                    "                        WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n" +
                    "                        AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n" +
                    "                        AND DEU.UNIVERSIDAD = CXP.UNIVERSIDAD\n" +
                    "                        AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n" +
                    "                        AND DEU.GESTION = CXP.GESTION\n" +
                    "                        AND DEU.PERIODO = CXP.PERIODO\n" +
                    "                        AND DEU.ACTIVO = 'SI'\n" +
                    "                        AND DEU.ESTADO = 0)\n" +
                    "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE                \n" +
                    "    UNION ALL \n" +
                    "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,       \n" +
                    "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n" +
                    "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n" +
                    "    1 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 1 ABANDONO_DEUDA,\n" +
                    "    0 INSCRITOS_TOTAL_REAL_B,        \n" +
                    "    0 INSCRITOS_TOTAL_REAL_D,\n" +
                    "    0 BECADOS_TOTAL_REAL_B,        \n" +
                    "    0 BECADOS_TOTAL_REAL_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_B,        \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_D,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_B,\n" +
                    "    0 INSCRITOS_TOTAL_PAGADO_D,    \n" +
                    "    0 BECADOS_TOTAL_PAGADO_B,\n" +
                    "    0 BECADOS_TOTAL_PAGADO_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_B,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_D,\n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_B,    \n" +
                    "    0 INSCRITOS_TOTAL_DEUDA_D,\n" +
                    "    0 BECADOS_TOTAL_DEUDA_B,    \n" +
                    "    0 BECADOS_TOTAL_DEUDA_D,\n" +
                    "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_B,    \n" +
                    "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_D    \n" +
                    "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP \n" +
                    "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n" +
                    "    AND     CXP.ACTIVO  = 'SI'                \n" +
                    "    AND     CXP.CUENTA_X_PAGAR IN             \n" +
                    "(4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,9611,9612,9613,\n" +
                    "            9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,4875,4876,4877,4923,4889,4913,\n" +
                    "            4914,4915,4916,4917,4932,4933,4934,4935,4936,4937,9529,4872,9527,9526,9528,4918,\n" +
                    "            9940,9682,9943,9683,9939,9942,9941,9944)\n" +
                    "    AND     (   SELECT  COUNT(*)\n" +
                    "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n" +
                    "                AND     RA.GESTION  = CXP.GESTION\n" +
                    "                AND     RA.PERIODO  = CXP.PERIODO\n" +
                    "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n" +
                    "                AND     RA.MODALIDAD = 'NORM') > 0  \n" +
                    "    AND NOT EXISTS (SELECT *\n" +
                    "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n" +
                    "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n" +
                    "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n" +
                    "                    AND CXP.GESTION = RA.GESTION\n" +
                    "                    AND CXP.PERIODO = RA.PERIODO\n" +
                    "                    AND RA.MODALIDAD = 'NORM'\n" +
                    "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n" +
                    "                    )  \n" +
                    "    AND     EXISTS (SELECT *\n" +
                    "                    FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n" +
                    "                    WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n" +
                    "                    AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n" +
                    "                    AND DEU.UNIVERSIDAD = CXP.UNIVERSIDAD\n" +
                    "                    AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n" +
                    "                    AND DEU.GESTION = CXP.GESTION\n" +
                    "                    AND DEU.PERIODO = CXP.PERIODO\n" +
                    "                    AND DEU.ACTIVO = 'SI'\n" +
                    "                    AND DEU.ESTADO = 0)\n" +
                    "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE     \n" +
                    ") CXP, " + Constants.ACADEMIC_SCHEMA + ".UNIDADES_ACAD_ADM U\n" +
                    "WHERE   CXP.UNIDAD_ACAD_ADM = U.UNIDAD_ACAD_ADM\n" +
                    "GROUP BY CXP.UNIDAD_ACAD_ADM, UPPER(U.DESCRIPCION), U.POSICION\n" +
                    "ORDER BY 1";
        } else {
            return "SELECT UAA.UNIDAD_ACAD_ADM CODIGO_SEDE, UPPER(UAA.DESCRIPCION) SEDE,\n"
                    + "U.UNIDAD CODIGO_FACULTAD, UPPER(U.DESCRIPCION) FACULTAD, U.SIGLA,\n"
                    + "SUM(INSCRITO) NRO_INSCRITOS_DEUDACREADA, SUM(PAGADO) NRO_INSCRITOS_PAGARON, SUM(DEUDA) NRO_INSCRITOS_DEUDORES,\n"
                    + "SUM(BECADO_INSCRITO) NRO_BECADOS_DEUDACREADA, SUM(BECADO_PAGADO) NRO_BECADOS_PAGARON, SUM(BECADO_DEUDA) NRO_BECADOS_DEUDORES,\n"
                    + "SUM(ABANDONO_INSCRITO) NRO_ABANDONO_DEUDACREADA, SUM(ABANDONO_PAGADO) NRO_ABANDONO_PAGARON, SUM(ABANDONO_DEUDA) NRO_ABANDONO_DEUDORES,\n"
                    + "ROUND(SUM(INSCRITOS_TOTAL_REAL_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(INSCRITOS_TOTAL_REAL_D) TOTAL_INS_DEUDACREADA_D,\n"
                    + "ROUND(SUM(INSCRITOS_TOTAL_PAGADO_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(INSCRITOS_TOTAL_PAGADO_D) TOTAL_INS_PAGADO_D,\n"
                    + "ROUND(SUM(INSCRITOS_TOTAL_DEUDA_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(INSCRITOS_TOTAL_DEUDA_D) TOTAL_INS_DEUDA_D,\n"
                    + "ROUND(SUM(BECADOS_TOTAL_REAL_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(BECADOS_TOTAL_REAL_D) TOTAL_BEC_DEUDACREADA_D,\n"
                    + "ROUND(SUM(BECADOS_TOTAL_PAGADO_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(BECADOS_TOTAL_PAGADO_D) TOTAL_BEC_PAGADO_D,\n"
                    + "ROUND(SUM(BECADOS_TOTAL_DEUDA_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(BECADOS_TOTAL_DEUDA_D) TOTAL_BEC_DEUDA_D,\n"
                    + "ROUND(SUM(ABANDONO_TOTAL_REAL_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(ABANDONO_TOTAL_REAL_D) TOTAL_ABA_DEUDACREADA_D,  \n"
                    + "ROUND(SUM(ABANDONO_TOTAL_PAGADO_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(ABANDONO_TOTAL_PAGADO_D) TOTAL_ABA_PAGADO_D,\n"
                    + "ROUND(SUM(ABANDONO_TOTAL_DEUDA_B)/" + Constants.CASHBOX_SCHEMA + ".FNUM_PRECIO_VENTA(SYSDATE),2)+SUM(ABANDONO_TOTAL_DEUDA_D) TOTAL_ABA_DEUDA_D\n"
                    + "FROM (\n"
                    + "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,      \n"
                    + "    1 INSCRITO, 1 PAGADO, 0 DEUDA,\n"
                    + "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n"
                    + "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_B,        \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_D,\n"
                    + "    0 BECADOS_TOTAL_REAL_B,        \n"
                    + "    0 BECADOS_TOTAL_REAL_D,\n"
                    + "    0 ABANDONO_TOTAL_REAL_B,        \n"
                    + "    0 ABANDONO_TOTAL_REAL_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_B,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_D,    \n"
                    + "    0 BECADOS_TOTAL_PAGADO_B,\n"
                    + "    0 BECADOS_TOTAL_PAGADO_D,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_B,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_B,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_D,\n"
                    + "    0 BECADOS_TOTAL_DEUDA_B,    \n"
                    + "    0 BECADOS_TOTAL_DEUDA_D,\n"
                    + "    0 ABANDONO_TOTAL_DEUDA_B,    \n"
                    + "    0 ABANDONO_TOTAL_DEUDA_D\n"
                    + "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP\n"
                    + "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n"
                    + "    AND     CXP.ACTIVO  = 'SI'   \n"
                    + "    AND     CXP.UNIDAD_ACAD_ADM = '" + businessUnitCode + "'\n"
                    + "    AND     CXP.CUENTA_X_PAGAR IN            \n"
                    + "            (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,\n"
                    + "            9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,\n"
                    + "            4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,\n"
                    + "            4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,\n"
                    + "            9939,9942,9941,9944)\n"
                    + "    AND     (   SELECT  COUNT(*)\n"
                    + "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                AND     RA.GESTION  = CXP.GESTION\n"
                    + "                AND     RA.PERIODO  = CXP.PERIODO\n"
                    + "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                AND     RA.MODALIDAD = 'NORM') > 0    \n"
                    + "    AND EXISTS (    SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n"
                    + "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n"
                    + "                    AND CXP.GESTION = RA.GESTION\n"
                    + "                    AND CXP.PERIODO = RA.PERIODO\n"
                    + "                    AND RA.MODALIDAD = 'NORM'\n"
                    + "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n"
                    + "                )\n"
                    + "    AND NOT EXISTS (SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n"
                    + "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                    AND BE.GESTION = CXP.GESTION\n"
                    + "                    AND BE.PERIODO = CXP.PERIODO\n"
                    + "                    AND BE.ESTADO  = 'ACEPTADO')  \n"
                    + "    AND     NOT EXISTS (SELECT *\n"
                    + "                        FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n"
                    + "                        WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n"
                    + "                        AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n"
                    + "                        AND DEU.UNIVERSIDAD     = CXP.UNIVERSIDAD\n"
                    + "                        AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n"
                    + "                        AND DEU.GESTION = CXP.GESTION\n"
                    + "                        AND DEU.PERIODO = CXP.PERIODO\n"
                    + "                        AND DEU.ACTIVO = 'SI'\n"
                    + "                        AND DEU.ESTADO = 0)\n"
                    + "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE                \n"
                    + "    UNION ALL \n"
                    + "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,       \n"
                    + "    1 INSCRITO, 0 PAGADO, 1 DEUDA,\n"
                    + "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n"
                    + "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_B,        \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) INSCRITOS_TOTAL_REAL_D,\n"
                    + "    0 BECADOS_TOTAL_REAL_B,        \n"
                    + "    0 BECADOS_TOTAL_REAL_D,\n"
                    + "    0 ABANDONO_TOTAL_REAL_B,        \n"
                    + "    0 ABANDONO_TOTAL_REAL_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_B,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_PAGADO_D,    \n"
                    + "    0 BECADOS_TOTAL_PAGADO_B,\n"
                    + "    0 BECADOS_TOTAL_PAGADO_D,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_B,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_B,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) INSCRITOS_TOTAL_DEUDA_D,\n"
                    + "    0 BECADOS_TOTAL_DEUDA_B,    \n"
                    + "    0 BECADOS_TOTAL_DEUDA_D,\n"
                    + "    0 ABANDONO_TOTAL_DEUDA_B,    \n"
                    + "    0 ABANDONO_TOTAL_DEUDA_D    \n"
                    + "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP \n"
                    + "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n"
                    + "    AND     CXP.ACTIVO  = 'SI'   \n"
                    + "    AND     CXP.UNIDAD_ACAD_ADM = '" + businessUnitCode + "'\n"
                    + "    AND     CXP.CUENTA_X_PAGAR IN\n"
                    + "            (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,\n"
                    + "            9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,\n"
                    + "            4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,\n"
                    + "            4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,\n"
                    + "            9939,9942,9941,9944)\n"
                    + "    AND     (   SELECT  COUNT(*)\n"
                    + "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                AND     RA.GESTION  = CXP.GESTION\n"
                    + "                AND     RA.PERIODO  = CXP.PERIODO\n"
                    + "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                AND     RA.MODALIDAD = 'NORM') > 0       \n"
                    + "    AND EXISTS (    SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n"
                    + "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n"
                    + "                    AND CXP.GESTION = RA.GESTION\n"
                    + "                    AND CXP.PERIODO = RA.PERIODO\n"
                    + "                    AND RA.MODALIDAD = 'NORM'\n"
                    + "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n"
                    + "                )\n"
                    + "    AND NOT EXISTS (SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n"
                    + "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                    AND BE.GESTION = CXP.GESTION\n"
                    + "                    AND BE.PERIODO = CXP.PERIODO\n"
                    + "                    AND BE.ESTADO  = 'ACEPTADO')  \n"
                    + "    AND     EXISTS (SELECT *\n"
                    + "                    FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n"
                    + "                    WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n"
                    + "                    AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n"
                    + "                    AND DEU.UNIVERSIDAD     = CXP.UNIVERSIDAD\n"
                    + "                    AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n"
                    + "                    AND DEU.GESTION = CXP.GESTION\n"
                    + "                    AND DEU.PERIODO = CXP.PERIODO\n"
                    + "                    AND DEU.ACTIVO = 'SI'\n"
                    + "                    AND DEU.ESTADO = 0)\n"
                    + "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE   \n"
                    + "    UNION ALL\n"
                    + "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,      \n"
                    + "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n"
                    + "    1 BECADO_INSCRITO, 1 BECADO_PAGADO, 0 BECADO_DEUDA,    \n"
                    + "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,\n"
                    + "    0 INSCRITOS_TOTAL_REAL_B,        \n"
                    + "    0 INSCRITOS_TOTAL_REAL_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_B,        \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_D,\n"
                    + "    0 ABANDONO_TOTAL_REAL_B,        \n"
                    + "    0 ABANDONO_TOTAL_REAL_D,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_B,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_D,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_B,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_D,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_B,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_D,\n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_B,    \n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_B,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_D,\n"
                    + "    0 ABANDONO_TOTAL_DEUDA_B,    \n"
                    + "    0 ABANDONO_TOTAL_DEUDA_D    \n"
                    + "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP\n"
                    + "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n"
                    + "    AND     CXP.ACTIVO  = 'SI'   \n"
                    + "    AND     CXP.UNIDAD_ACAD_ADM = '" + businessUnitCode + "'\n"
                    + "    AND     CXP.CUENTA_X_PAGAR IN            \n"
                    + "            (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,\n"
                    + "            9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,\n"
                    + "            4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,\n"
                    + "            4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,\n"
                    + "            9939,9942,9941,9944)\n"
                    + "    AND     (   SELECT  COUNT(*)\n"
                    + "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                AND     RA.GESTION  = CXP.GESTION\n"
                    + "                AND     RA.PERIODO  = CXP.PERIODO\n"
                    + "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                AND     RA.MODALIDAD = 'NORM') > 0 \n"
                    + "    AND EXISTS (    SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n"
                    + "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n"
                    + "                    AND CXP.GESTION = RA.GESTION\n"
                    + "                    AND CXP.PERIODO = RA.PERIODO\n"
                    + "                    AND RA.MODALIDAD = 'NORM'\n"
                    + "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n"
                    + "                )\n"
                    + "    AND EXISTS (    SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n"
                    + "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                    AND BE.GESTION = CXP.GESTION\n"
                    + "                    AND BE.PERIODO = CXP.PERIODO\n"
                    + "                    AND BE.ESTADO  = 'ACEPTADO')  \n"
                    + "    AND     NOT EXISTS (SELECT *\n"
                    + "                        FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n"
                    + "                        WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n"
                    + "                        AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n"
                    + "                        AND DEU.UNIVERSIDAD     = CXP.UNIVERSIDAD\n"
                    + "                        AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n"
                    + "                        AND DEU.GESTION = CXP.GESTION\n"
                    + "                        AND DEU.PERIODO = CXP.PERIODO\n"
                    + "                        AND DEU.ACTIVO = 'SI'\n"
                    + "                        AND DEU.ESTADO = 0)\n"
                    + "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE                \n"
                    + "    UNION ALL \n"
                    + "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,       \n"
                    + "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n"
                    + "    1 BECADO_INSCRITO, 0 BECADO_PAGADO, 1 BECADO_DEUDA, \n"
                    + "    0 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,\n"
                    + "    0 INSCRITOS_TOTAL_REAL_B,        \n"
                    + "    0 INSCRITOS_TOTAL_REAL_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_B,        \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) BECADOS_TOTAL_REAL_D,\n"
                    + "    0 ABANDONO_TOTAL_REAL_B,        \n"
                    + "    0 ABANDONO_TOTAL_REAL_D,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_B,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_D,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_B,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_PAGADO_D,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_B,\n"
                    + "    0 ABANDONO_TOTAL_PAGADO_D,\n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_B,    \n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_B,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) BECADOS_TOTAL_DEUDA_D,\n"
                    + "    0 ABANDONO_TOTAL_DEUDA_B,    \n"
                    + "    0 ABANDONO_TOTAL_DEUDA_D       \n"
                    + "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP \n"
                    + "    WHERE   TO_CHAR(CXP.GESTION) = " + year + "\n"
                    + "    AND     CXP.ACTIVO  = 'SI'   \n"
                    + "    AND     CXP.UNIDAD_ACAD_ADM = '" + businessUnitCode + "'\n"
                    + "    AND     CXP.CUENTA_X_PAGAR IN\n"
                    + "            (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,\n"
                    + "            9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,\n"
                    + "            4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,\n"
                    + "            4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,\n"
                    + "            9939,9942,9941,9944)\n"
                    + "    AND     (   SELECT  COUNT(*)\n"
                    + "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                AND     RA.GESTION  = CXP.GESTION\n"
                    + "                AND     RA.PERIODO  = CXP.PERIODO\n"
                    + "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                AND     RA.MODALIDAD = 'NORM') > 0   \n"
                    + "    AND EXISTS (    SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n"
                    + "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n"
                    + "                    AND CXP.GESTION = RA.GESTION\n"
                    + "                    AND CXP.PERIODO = RA.PERIODO\n"
                    + "                    AND RA.MODALIDAD = 'NORM'\n"
                    + "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n"
                    + "                )\n"
                    + "    AND EXISTS (    SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".BECAS_ESTUDIANTES BE\n"
                    + "                    WHERE BE.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                    AND BE.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                    AND BE.GESTION = CXP.GESTION\n"
                    + "                    AND BE.PERIODO = CXP.PERIODO\n"
                    + "                    AND BE.ESTADO  = 'ACEPTADO')  \n"
                    + "    AND     EXISTS (SELECT *\n"
                    + "                    FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n"
                    + "                    WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n"
                    + "                    AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n"
                    + "                    AND DEU.UNIVERSIDAD     = CXP.UNIVERSIDAD\n"
                    + "                    AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n"
                    + "                    AND DEU.GESTION = CXP.GESTION\n"
                    + "                    AND DEU.PERIODO = CXP.PERIODO\n"
                    + "                    AND DEU.ACTIVO = 'SI'\n"
                    + "                    AND DEU.ESTADO = 0)\n"
                    + "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE   \n"
                    + "    UNION ALL\n"
                    + "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,      \n"
                    + "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n"
                    + "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n"
                    + "    1 ABANDONO_INSCRITO, 1 ABANDONO_PAGADO, 0 ABANDONO_DEUDA,\n"
                    + "    0 INSCRITOS_TOTAL_REAL_B,        \n"
                    + "    0 INSCRITOS_TOTAL_REAL_D,\n"
                    + "    0 BECADOS_TOTAL_REAL_B,        \n"
                    + "    0 BECADOS_TOTAL_REAL_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_B,        \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_D,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_B,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_D,    \n"
                    + "    0 BECADOS_TOTAL_PAGADO_B,\n"
                    + "    0 BECADOS_TOTAL_PAGADO_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_B,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_D,\n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_B,    \n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_D,\n"
                    + "    0 BECADOS_TOTAL_DEUDA_B,    \n"
                    + "    0 BECADOS_TOTAL_DEUDA_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_B,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_D      \n"
                    + "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP\n"
                    + "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n"
                    + "    AND     CXP.ACTIVO  = 'SI'   \n"
                    + "    AND     CXP.UNIDAD_ACAD_ADM = '" + businessUnitCode + "'\n"
                    + "    AND     CXP.CUENTA_X_PAGAR IN            \n"
                    + "            (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,\n"
                    + "            9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,\n"
                    + "            4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,\n"
                    + "            4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,\n"
                    + "            9939,9942,9941,9944)\n"
                    + "    AND     (   SELECT  COUNT(*)\n"
                    + "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                AND     RA.GESTION  = CXP.GESTION\n"
                    + "                AND     RA.PERIODO  = CXP.PERIODO\n"
                    + "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                AND     RA.MODALIDAD = 'NORM') > 0      \n"
                    + "    AND NOT EXISTS (SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n"
                    + "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n"
                    + "                    AND CXP.GESTION = RA.GESTION\n"
                    + "                    AND CXP.PERIODO = RA.PERIODO\n"
                    + "                    AND RA.MODALIDAD = 'NORM'\n"
                    + "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n"
                    + "                    )\n"
                    + "    AND     NOT EXISTS (SELECT *\n"
                    + "                        FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n"
                    + "                        WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n"
                    + "                        AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n"
                    + "                        AND DEU.UNIVERSIDAD     = CXP.UNIVERSIDAD\n"
                    + "                        AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n"
                    + "                        AND DEU.GESTION = CXP.GESTION\n"
                    + "                        AND DEU.PERIODO = CXP.PERIODO\n"
                    + "                        AND DEU.ACTIVO = 'SI'\n"
                    + "                        AND DEU.ESTADO = 0)\n"
                    + "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE                \n"
                    + "    UNION ALL \n"
                    + "    SELECT  CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE,       \n"
                    + "    0 INSCRITO, 0 PAGADO, 0 DEUDA,\n"
                    + "    0 BECADO_INSCRITO, 0 BECADO_PAGADO, 0 BECADO_DEUDA,    \n"
                    + "    1 ABANDONO_INSCRITO, 0 ABANDONO_PAGADO, 1 ABANDONO_DEUDA,\n"
                    + "    0 INSCRITOS_TOTAL_REAL_B,        \n"
                    + "    0 INSCRITOS_TOTAL_REAL_D,\n"
                    + "    0 BECADOS_TOTAL_REAL_B,        \n"
                    + "    0 BECADOS_TOTAL_REAL_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_B,        \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',CXP.IMPORTE, 0)) ABANDONO_TOTAL_REAL_D,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_B,\n"
                    + "    0 INSCRITOS_TOTAL_PAGADO_D,    \n"
                    + "    0 BECADOS_TOTAL_PAGADO_B,\n"
                    + "    0 BECADOS_TOTAL_PAGADO_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_B,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,1, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_PAGADO_D,\n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_B,    \n"
                    + "    0 INSCRITOS_TOTAL_DEUDA_D,\n"
                    + "    0 BECADOS_TOTAL_DEUDA_B,    \n"
                    + "    0 BECADOS_TOTAL_DEUDA_D,\n"
                    + "    SUM(DECODE(CXP.MONEDA,'B',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_B,    \n"
                    + "    SUM(DECODE(CXP.MONEDA,'D',DECODE(CXP.ESTADO,0, CXP.IMPORTE, 0), 0) ) ABANDONO_TOTAL_DEUDA_D  \n"
                    + "    FROM    " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR CXP \n"
                    + "    WHERE   TO_CHAR(CXP.GESTION) = '" + year + "'\n"
                    + "    AND     CXP.ACTIVO  = 'SI'   \n"
                    + "    AND     CXP.UNIDAD_ACAD_ADM = '" + businessUnitCode + "'\n"
                    + "    AND     CXP.CUENTA_X_PAGAR IN\n"
                    + "            (4904,9568,9569,9570,4931,9603,9604,9605,9606,9607,9608,9609,9610,\n"
                    + "            9611,9612,9613,9614,9615,9809,9810,9863,9864,5054,4878,4873,4874,\n"
                    + "            4875,4876,4877,4923,4889,4913,4914,4915,4916,4917,4932,4933,4934,\n"
                    + "            4935,4936,4937,9529,4872,9527,9526,9528,4918,9940,9682,9943,9683,\n"
                    + "            9939,9942,9941,9944)\n"
                    + "    AND     (   SELECT  COUNT(*)\n"
                    + "                FROM    " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                WHERE   RA.ESTUDIANTE = CXP.ESTUDIANTE\n"
                    + "                AND     RA.GESTION  = CXP.GESTION\n"
                    + "                AND     RA.PERIODO  = CXP.PERIODO\n"
                    + "                AND     RA.PLAN_ESTUDIO = CXP.PLAN_ESTUDIO\n"
                    + "                AND     RA.MODALIDAD = 'NORM') > 0      \n"
                    + "    AND NOT EXISTS (SELECT *\n"
                    + "                    FROM " + Constants.ACADEMIC_SCHEMA + ".REGISTROS_ACADEMICOS RA\n"
                    + "                    WHERE CXP.ESTUDIANTE = RA.ESTUDIANTE\n"
                    + "                    AND CXP.PLAN_ESTUDIO = RA.PLAN_ESTUDIO\n"
                    + "                    AND CXP.GESTION = RA.GESTION\n"
                    + "                    AND CXP.PERIODO = RA.PERIODO\n"
                    + "                    AND RA.MODALIDAD = 'NORM'\n"
                    + "                    AND (NE1 IS NOT NULL OR NE2 IS NOT NULL OR NE3 IS NOT NULL)\n"
                    + "                    )\n"
                    + "    AND     EXISTS (SELECT *\n"
                    + "                    FROM " + Constants.CASHBOX_SCHEMA + ".CUENTAS_X_PAGAR DEU\n"
                    + "                    WHERE DEU.ESTUDIANTE = CXP.ESTUDIANTE                    \n"
                    + "                    AND DEU.UNIDAD_ACAD_ADM = CXP.UNIDAD_ACAD_ADM\n"
                    + "                    AND DEU.UNIVERSIDAD     = CXP.UNIVERSIDAD\n"
                    + "                    AND DEU.PLAN_ESTUDIO    = CXP.PLAN_ESTUDIO\n"
                    + "                    AND DEU.GESTION = CXP.GESTION\n"
                    + "                    AND DEU.PERIODO = CXP.PERIODO\n"
                    + "                    AND DEU.ACTIVO = 'SI'\n"
                    + "                    AND DEU.ESTADO = 0)\n"
                    + "    GROUP BY CXP.UNIDAD_ACAD_ADM, CXP.PLAN_ESTUDIO, CXP.ESTUDIANTE  \n"
                    + ") CXP, " + Constants.ACADEMIC_SCHEMA + ".UNIDADES U, " + Constants.ACADEMIC_SCHEMA + ".UNIDADES_ACAD_ADM UAA, " + Constants.ACADEMIC_SCHEMA + ".PLANES_ESTUDIOS PL\n"
                    + "WHERE   CXP.PLAN_ESTUDIO = PL.PLAN_ESTUDIO\n"
                    + "AND     CXP.UNIDAD_ACAD_ADM = PL.UNIDAD_ACAD_ADM\n"
                    + "AND     PL.UNIDAD = U.UNIDAD\n"
                    + "AND     PL.UNIDAD_ACAD_ADM = U.UNIDAD_ACAD_ADM\n"
                    + "AND     CXP.UNIDAD_ACAD_ADM = UAA.UNIDAD_ACAD_ADM\n"
                    + "GROUP BY UAA.UNIDAD_ACAD_ADM, UPPER(UAA.DESCRIPCION), U.UNIDAD, UPPER(U.DESCRIPCION), U.SIGLA\n"
                    + "ORDER BY 4"
                    ;
        }
    }

    public Integer getYear() {
        return year;
    }

    public boolean isByBusinessUnit() {
        return businessUnitCode != null;
    }

}
