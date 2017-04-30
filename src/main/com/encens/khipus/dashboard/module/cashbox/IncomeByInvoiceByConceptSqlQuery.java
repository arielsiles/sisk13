package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.7
 */
public class IncomeByInvoiceByConceptSqlQuery implements SqlQuery {
    private Integer executorUnitCode;
    private Integer startDate;
    private Integer endDate;


    public String getSql() {
        String sql = "SELECT TO_NUMBER(ANIO||MES) CODIGO,\n" +
                "       TO_NUMBER(MES) MES, \n" +
                "       TO_NUMBER(ANIO) ANIO, MES_LITERAL,\n" +
                "       MONTO_B_DEPOSITO_UNICO, MONTO_D_DEPOSITO_UNICO,\n" +
                "       MONTO_B_DERECHO_ADMISION, MONTO_D_DERECHO_ADMISION,\n" +
                "       MONTO_B_COMPUTADORA, MONTO_D_COMPUTADORA,\n" +
                "       MONTO_B_SEMESTRE_CONTADO, MONTO_D_SEMESTRE_CONTADO,\n" +
                "       MONTO_B_MATRICULA, MONTO_D_MATRICULA,\n" +
                "       MONTO_B_CUOTAS, MONTO_D_CUOTAS,\n" +
                "       MONTO_B_GASTOS_ADM, MONTO_D_GASTOS_ADM,\n" +
                "       MONTO_B_MAT_ADICIONAL, MONTO_D_MAT_ADICIONAL,\n" +
                "       MONTO_B_ARRASTRES, MONTO_D_ARRASTRES,\n" +
                "       MONTO_B_CONGRESOS, MONTO_D_CONGRESOS,\n" +
                "       MONTO_B_DERECHOS_CARGOS, MONTO_D_DERECHOS_CARGOS,\n" +
                "       MONTO_B_PRACTICAS_ODONTO, MONTO_D_PRACTICAS_ODONTO,\n" +
                "       MONTO_B_PRACTICAS_HOSP, MONTO_D_PRACTICAS_HOSP,\n" +
                "       MONTO_B_INTERNADO_ROTATORIO, MONTO_D_INTERNADO_ROTATORIO,\n" +
                "       MONTO_B_GASTOS_TITULACION, MONTO_D_GASTOS_TITULACION,\n" +
                "       MONTO_B_CURSOS_EXTRA, MONTO_D_CURSOS_EXTRA,\n" +
                "       MONTO_B_TRAMITES_LEGALIZ,MONTO_D_TRAMITES_LEGALIZ,\n" +
                "       MONTO_B_VERANO_INVIERNO, MONTO_D_VERANO_INVIERNO,\n" +
                "       MONTO_B_DESAFIO_MAT, MONTO_D_DESAFIO_MAT,\n" +
                "       MONTO_B_EXAMEN_EXTEMP, MONTO_D_EXAMEN_EXTEMP,\n" +
                "       MONTO_B_SEGUNDO_TURNO, MONTO_D_SEGUNDO_TURNO,\n" +
                "       MONTO_B_SOUVENIRS, MONTO_D_SOUVENIRS,\n" +
                "       MONTO_B_MATERIAL_DIDACTICO, MONTO_D_MATERIAL_DIDACTICO,\n" +
                "       MONTO_B_ALQUILER_AUDITORIO, MONTO_D_ALQUILER_AUDITORIO,\n" +
                "       MONTO_B_ALQUILER_CAFETERIA, MONTO_D_ALQUILER_CAFETERIA,\n" +
                "       MONTO_B_RESERVA, MONTO_D_RESERVA,\n" +
                "       (MONTO_B_DEPOSITO_UNICO + MONTO_B_DERECHO_ADMISION + MONTO_B_COMPUTADORA + MONTO_B_SEMESTRE_CONTADO + MONTO_B_MATRICULA + MONTO_B_CUOTAS + MONTO_B_GASTOS_ADM + MONTO_B_MAT_ADICIONAL + MONTO_B_ARRASTRES + MONTO_B_CONGRESOS + MONTO_B_DERECHOS_CARGOS + MONTO_B_PRACTICAS_ODONTO + MONTO_B_PRACTICAS_HOSP + MONTO_B_INTERNADO_ROTATORIO + MONTO_B_GASTOS_TITULACION + MONTO_B_CURSOS_EXTRA + MONTO_B_TRAMITES_LEGALIZ + MONTO_B_VERANO_INVIERNO + MONTO_B_DESAFIO_MAT + MONTO_B_EXAMEN_EXTEMP + MONTO_B_SEGUNDO_TURNO + MONTO_B_SOUVENIRS + MONTO_B_MATERIAL_DIDACTICO + MONTO_B_ALQUILER_AUDITORIO + MONTO_B_ALQUILER_CAFETERIA + MONTO_B_RESERVA) TOTAL_CONCEPTOS_BS,\n" +
                "       (MONTO_D_DEPOSITO_UNICO + MONTO_D_DERECHO_ADMISION + MONTO_D_COMPUTADORA + MONTO_D_SEMESTRE_CONTADO + MONTO_D_MATRICULA + MONTO_D_CUOTAS + MONTO_D_GASTOS_ADM + MONTO_D_MAT_ADICIONAL + MONTO_D_ARRASTRES + MONTO_D_CONGRESOS + MONTO_D_DERECHOS_CARGOS + MONTO_D_PRACTICAS_ODONTO + MONTO_D_PRACTICAS_HOSP + MONTO_D_INTERNADO_ROTATORIO + MONTO_D_GASTOS_TITULACION + MONTO_D_CURSOS_EXTRA + MONTO_D_TRAMITES_LEGALIZ + MONTO_D_VERANO_INVIERNO + MONTO_D_DESAFIO_MAT + MONTO_D_EXAMEN_EXTEMP + MONTO_D_SEGUNDO_TURNO + MONTO_D_SOUVENIRS + MONTO_D_MATERIAL_DIDACTICO + MONTO_D_ALQUILER_AUDITORIO + MONTO_D_ALQUILER_CAFETERIA + MONTO_D_RESERVA) TOTAL_CONCEPTOS_DOL,\n" +
                "       (ROUND((\tMONTO_B_DEPOSITO_UNICO + MONTO_B_DERECHO_ADMISION + MONTO_B_COMPUTADORA + MONTO_B_SEMESTRE_CONTADO + MONTO_B_MATRICULA + MONTO_B_CUOTAS + MONTO_B_GASTOS_ADM + MONTO_B_MAT_ADICIONAL + MONTO_B_ARRASTRES + MONTO_B_CONGRESOS + MONTO_B_DERECHOS_CARGOS + MONTO_B_PRACTICAS_ODONTO + MONTO_B_PRACTICAS_HOSP + MONTO_B_INTERNADO_ROTATORIO + MONTO_B_GASTOS_TITULACION + MONTO_B_CURSOS_EXTRA + MONTO_B_TRAMITES_LEGALIZ + MONTO_B_VERANO_INVIERNO + MONTO_B_DESAFIO_MAT + MONTO_B_EXAMEN_EXTEMP + MONTO_B_SEGUNDO_TURNO + MONTO_B_SOUVENIRS + MONTO_B_MATERIAL_DIDACTICO + MONTO_B_ALQUILER_AUDITORIO + MONTO_B_ALQUILER_CAFETERIA + MONTO_B_RESERVA)/" + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO), 2) + (MONTO_D_DEPOSITO_UNICO + MONTO_D_DERECHO_ADMISION + MONTO_D_COMPUTADORA + MONTO_D_SEMESTRE_CONTADO + MONTO_D_MATRICULA + MONTO_D_CUOTAS + MONTO_D_GASTOS_ADM + MONTO_D_MAT_ADICIONAL + MONTO_D_ARRASTRES + MONTO_D_CONGRESOS + MONTO_D_DERECHOS_CARGOS + MONTO_D_PRACTICAS_ODONTO + MONTO_D_PRACTICAS_HOSP + MONTO_D_INTERNADO_ROTATORIO + MONTO_D_GASTOS_TITULACION + MONTO_D_CURSOS_EXTRA + MONTO_D_TRAMITES_LEGALIZ + MONTO_D_VERANO_INVIERNO + MONTO_D_DESAFIO_MAT + MONTO_D_EXAMEN_EXTEMP + MONTO_D_SEGUNDO_TURNO + MONTO_D_SOUVENIRS + MONTO_D_MATERIAL_DIDACTICO + MONTO_D_ALQUILER_AUDITORIO + MONTO_D_ALQUILER_CAFETERIA + MONTO_D_RESERVA)) TOTAL_DOL,\n" +
                "       " + Constants.CASHBOX_SCHEMA + ".F_TIPO_CAMBIO_MES(MES, ANIO) TIPO_CAMBIO\n" +
                "FROM (SELECT TO_CHAR(M.FECHA,'MM') MES, \n" +
                "             TO_CHAR(M.FECHA,'YYYY') ANIO,\n" +
                "             DECODE(TO_CHAR(M.FECHA,'MM'), '01','ENERO','02','FEBRERO','03','MARZO','04','ABRIL','05','MAYO','06','JUNIO','07','JULIO','08','AGOSTO','09','SEPTIEMBRE','10','OCTUBRE','11','NOVIEMBRE','12','DICIEMBRE') MES_LITERAL,\n" +
                "             SUM(M.MONTO_B_DEPOSITO_UNICO) MONTO_B_DEPOSITO_UNICO,\n" +
                "             SUM(M.MONTO_D_DEPOSITO_UNICO) MONTO_D_DEPOSITO_UNICO,\n" +
                "             SUM(M.MONTO_B_DERECHO_ADMISION) MONTO_B_DERECHO_ADMISION,\n" +
                "             SUM(M.MONTO_D_DERECHO_ADMISION) MONTO_D_DERECHO_ADMISION,\n" +
                "             SUM(M.MONTO_B_COMPUTADORA) MONTO_B_COMPUTADORA,\n" +
                "             SUM(M.MONTO_D_COMPUTADORA) MONTO_D_COMPUTADORA,\n" +
                "             SUM(M.MONTO_B_SEMESTRE_CONTADO) MONTO_B_SEMESTRE_CONTADO, \n" +
                "             SUM(M.MONTO_D_SEMESTRE_CONTADO) MONTO_D_SEMESTRE_CONTADO,\n" +
                "             SUM(M.MONTO_B_MATRICULA) MONTO_B_MATRICULA,\n" +
                "             SUM(M.MONTO_D_MATRICULA) MONTO_D_MATRICULA,\n" +
                "             SUM(M.MONTO_B_CUOTAS) MONTO_B_CUOTAS, \n" +
                "             SUM(M.MONTO_D_CUOTAS) MONTO_D_CUOTAS,\n" +
                "             SUM(M.MONTO_B_GASTOS_ADM) MONTO_B_GASTOS_ADM,\n" +
                "             SUM(M.MONTO_D_GASTOS_ADM) MONTO_D_GASTOS_ADM,\n" +
                "             SUM(M.MONTO_B_MAT_ADICIONAL) MONTO_B_MAT_ADICIONAL,\n" +
                "             SUM(M.MONTO_D_MAT_ADICIONAL) MONTO_D_MAT_ADICIONAL,\n" +
                "             SUM(M.MONTO_B_ARRASTRES) MONTO_B_ARRASTRES,\n" +
                "             SUM(M.MONTO_D_ARRASTRES) MONTO_D_ARRASTRES,\n" +
                "             SUM(M.MONTO_B_CONGRESOS) MONTO_B_CONGRESOS,\n" +
                "             SUM(M.MONTO_D_CONGRESOS) MONTO_D_CONGRESOS,\n" +
                "             SUM(M.MONTO_B_DERECHOS_CARGOS) MONTO_B_DERECHOS_CARGOS,\n" +
                "             SUM(M.MONTO_D_DERECHOS_CARGOS) MONTO_D_DERECHOS_CARGOS,\n" +
                "             SUM(M.MONTO_B_PRACTICAS_ODONTO) MONTO_B_PRACTICAS_ODONTO,\n" +
                "             SUM(M.MONTO_D_PRACTICAS_ODONTO) MONTO_D_PRACTICAS_ODONTO,\n" +
                "             SUM(M.MONTO_B_PRACTICAS_HOSP) MONTO_B_PRACTICAS_HOSP,\n" +
                "             SUM(M.MONTO_D_PRACTICAS_HOSP) MONTO_D_PRACTICAS_HOSP,\n" +
                "             SUM(M.MONTO_B_INTERNADO_ROTATORIO) MONTO_B_INTERNADO_ROTATORIO,\n" +
                "             SUM(M.MONTO_D_INTERNADO_ROTATORIO) MONTO_D_INTERNADO_ROTATORIO,\n" +
                "             SUM(M.MONTO_B_GASTOS_TITULACION) MONTO_B_GASTOS_TITULACION,\n" +
                "             SUM(M.MONTO_D_GASTOS_TITULACION) MONTO_D_GASTOS_TITULACION,\n" +
                "             SUM(M.MONTO_B_CURSOS_EXTRA) MONTO_B_CURSOS_EXTRA,\n" +
                "             SUM(M.MONTO_D_CURSOS_EXTRA) MONTO_D_CURSOS_EXTRA,\n" +
                "             SUM(M.MONTO_B_TRAMITES_LEGALIZ) MONTO_B_TRAMITES_LEGALIZ,\n" +
                "             SUM(M.MONTO_D_TRAMITES_LEGALIZ) MONTO_D_TRAMITES_LEGALIZ,\n" +
                "             SUM(M.MONTO_B_VERANO_INVIERNO) MONTO_B_VERANO_INVIERNO,\n" +
                "             SUM(M.MONTO_D_VERANO_INVIERNO) MONTO_D_VERANO_INVIERNO,\n" +
                "             SUM(M.MONTO_B_DESAFIO_MAT) MONTO_B_DESAFIO_MAT,\n" +
                "             SUM(M.MONTO_D_DESAFIO_MAT) MONTO_D_DESAFIO_MAT,\n" +
                "             SUM(M.MONTO_B_EXAMEN_EXTEMP) MONTO_B_EXAMEN_EXTEMP,\n" +
                "             SUM(M.MONTO_D_EXAMEN_EXTEMP) MONTO_D_EXAMEN_EXTEMP,\n" +
                "             SUM(M.MONTO_B_SEGUNDO_TURNO) MONTO_B_SEGUNDO_TURNO,\n" +
                "             SUM(M.MONTO_D_SEGUNDO_TURNO) MONTO_D_SEGUNDO_TURNO,\n" +
                "             SUM(M.MONTO_B_SOUVENIRS) MONTO_B_SOUVENIRS,\n" +
                "             SUM(M.MONTO_D_SOUVENIRS) MONTO_D_SOUVENIRS,\n" +
                "             SUM(M.MONTO_B_MATERIAL_DIDACTICO) MONTO_B_MATERIAL_DIDACTICO,\n" +
                "             SUM(M.MONTO_D_MATERIAL_DIDACTICO) MONTO_D_MATERIAL_DIDACTICO,\n" +
                "             SUM(M.MONTO_B_ALQUILER_AUDITORIO) MONTO_B_ALQUILER_AUDITORIO,\n" +
                "             SUM(M.MONTO_D_ALQUILER_AUDITORIO) MONTO_D_ALQUILER_AUDITORIO,\n" +
                "             SUM(M.MONTO_B_ALQUILER_CAFETERIA) MONTO_B_ALQUILER_CAFETERIA,\n" +
                "             SUM(M.MONTO_D_ALQUILER_CAFETERIA) MONTO_D_ALQUILER_CAFETERIA,\n" +
                "             SUM(M.MONTO_B_RESERVA) MONTO_B_RESERVA,\n" +
                "             SUM(M.MONTO_D_RESERVA) MONTO_D_RESERVA\n" +
                "      FROM(SELECT TRUNC(M.FECHA) FECHA,\n" +
                "                  SUM(DECODE(CON.COD,'01',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DEPOSITO_UNICO,\n" +
                "                  SUM(DECODE(CON.COD,'01',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DEPOSITO_UNICO,\n" +
                "                  SUM(DECODE(CON.COD,'02',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DERECHO_ADMISION,\n" +
                "                  SUM(DECODE(CON.COD,'02',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DERECHO_ADMISION,\n" +
                "                  SUM(DECODE(CON.COD,'03',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_COMPUTADORA,\n" +
                "                  SUM(DECODE(CON.COD,'03',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_COMPUTADORA,\n" +
                "                  SUM(DECODE(CON.COD,'04',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_SEMESTRE_CONTADO,\n" +
                "                  SUM(DECODE(CON.COD,'04',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_SEMESTRE_CONTADO,\n" +
                "                  SUM(DECODE(CON.COD,'05',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_MATRICULA,\n" +
                "                  SUM(DECODE(CON.COD,'05',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_MATRICULA,\n" +
                "                  SUM(DECODE(CON.COD,'06',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_CUOTAS,\n" +
                "                  SUM(DECODE(CON.COD,'06',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_CUOTAS,\n" +
                "                  SUM(DECODE(CON.COD,'07',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_GASTOS_ADM,\n" +
                "                  SUM(DECODE(CON.COD,'07',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_GASTOS_ADM,\n" +
                "                  SUM(DECODE(CON.COD,'08',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_MAT_ADICIONAL,\n" +
                "                  SUM(DECODE(CON.COD,'08',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_MAT_ADICIONAL,\n" +
                "                  SUM(DECODE(CON.COD,'09',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_ARRASTRES,\n" +
                "                  SUM(DECODE(CON.COD,'09',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_ARRASTRES,\n" +
                "                  SUM(DECODE(CON.COD,'10',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_CONGRESOS,\n" +
                "                  SUM(DECODE(CON.COD,'10',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_CONGRESOS,\n" +
                "                  SUM(DECODE(CON.COD,'11',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DERECHOS_CARGOS,\n" +
                "                  SUM(DECODE(CON.COD,'11',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DERECHOS_CARGOS,\n" +
                "                  SUM(DECODE(CON.COD,'12',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_PRACTICAS_ODONTO,\n" +
                "                  SUM(DECODE(CON.COD,'12',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_PRACTICAS_ODONTO,\n" +
                "                  SUM(DECODE(CON.COD,'13',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_PRACTICAS_HOSP,\n" +
                "                  SUM(DECODE(CON.COD,'13',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_PRACTICAS_HOSP,\n" +
                "                  SUM(DECODE(CON.COD,'14',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_INTERNADO_ROTATORIO,\n" +
                "                  SUM(DECODE(CON.COD,'14',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_INTERNADO_ROTATORIO,\n" +
                "                  SUM(DECODE(CON.COD,'15',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_GASTOS_TITULACION,\n" +
                "                  SUM(DECODE(CON.COD,'15',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_GASTOS_TITULACION,\n" +
                "                  SUM(DECODE(CON.COD,'16',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_CURSOS_EXTRA,\n" +
                "                  SUM(DECODE(CON.COD,'16',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_CURSOS_EXTRA,\n" +
                "                  SUM(DECODE(CON.COD,'17',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_TRAMITES_LEGALIZ,\n" +
                "                  SUM(DECODE(CON.COD,'17',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_TRAMITES_LEGALIZ,\n" +
                "                  SUM(DECODE(CON.COD,'18',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_VERANO_INVIERNO,\n" +
                "                  SUM(DECODE(CON.COD,'18',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_VERANO_INVIERNO,\n" +
                "                  SUM(DECODE(CON.COD,'19',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DESAFIO_MAT,\n" +
                "                  SUM(DECODE(CON.COD,'19',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DESAFIO_MAT,\n" +
                "                  SUM(DECODE(CON.COD,'20',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_EXAMEN_EXTEMP,\n" +
                "                  SUM(DECODE(CON.COD,'20',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_EXAMEN_EXTEMP,\n" +
                "                  SUM(DECODE(CON.COD,'21',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_SEGUNDO_TURNO,\n" +
                "                  SUM(DECODE(CON.COD,'21',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_SEGUNDO_TURNO,\n" +
                "                  SUM(DECODE(CON.COD,'22',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_SOUVENIRS,\n" +
                "                  SUM(DECODE(CON.COD,'22',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_SOUVENIRS,\n" +
                "                  SUM(DECODE(CON.COD,'23',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_MATERIAL_DIDACTICO,\n" +
                "                  SUM(DECODE(CON.COD,'23',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_MATERIAL_DIDACTICO,\n" +
                "                  SUM(DECODE(CON.COD,'24',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_ALQUILER_AUDITORIO,\n" +
                "                  SUM(DECODE(CON.COD,'24',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_ALQUILER_AUDITORIO,\n" +
                "                  SUM(DECODE(CON.COD,'25',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_ALQUILER_CAFETERIA,\n" +
                "                  SUM(DECODE(CON.COD,'25',DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_ALQUILER_CAFETERIA,\n" +
                "                  SUM(DECODE(CON.COD,'26',DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_RESERVA,\n" +
                "                  SUM(DECODE(CON.COD,'26', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_RESERVA\n" +
                "           FROM " + Constants.CASHBOX_SCHEMA + ".B_MOVIMIENTOS M,\n" +
                "                " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST,\n" +
                "                " + Constants.CASHBOX_SCHEMA + ".CUENTAS CUEN,\n" +
                "                " + Constants.CASHBOX_SCHEMA + ".CONCEPTOS CON\n" +
                "           WHERE M.ESTADO = 'V'\n" +
                "                 AND M.EST_COD = EST.CODIGO\n" +
                "                 AND M.CUEN_ID = CUEN.ID\n" +
                "                 AND CUEN.CON_COD = CON.COD\n";
        sql += addFilters();

        sql += "          GROUP BY TRUNC(M.FECHA)\n" +
                "          UNION\n" +
                "          SELECT TRUNC(M.FECHA) FECHA,\n" +
                "                 SUM(DECODE(CON.COD, '01', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DEPOSITO_UNICO,\n" +
                "                 SUM(DECODE(CON.COD, '01', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DEPOSITO_UNICO,\n" +
                "                 SUM(DECODE(CON.COD, '02', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DERECHO_ADMISION,\n" +
                "                 SUM(DECODE(CON.COD, '02', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DERECHO_ADMISION,\n" +
                "                 SUM(DECODE(CON.COD, '03', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_COMPUTADORA,\n" +
                "                 SUM(DECODE(CON.COD, '03', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_COMPUTADORA,\n" +
                "                 SUM(DECODE(CON.COD, '04', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_SEMESTRE_CONTADO,\n" +
                "                 SUM(DECODE(CON.COD, '04', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_SEMESTRE_CONTADO,\n" +
                "                 SUM(DECODE(CON.COD, '05', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_MATRICULA,\n" +
                "                 SUM(DECODE(CON.COD, '05', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_MATRICULA,\n" +
                "                 SUM(DECODE(CON.COD, '06', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_CUOTAS,\n" +
                "                 SUM(DECODE(CON.COD, '06', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_CUOTAS,\n" +
                "                 SUM(DECODE(CON.COD, '07', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_GASTOS_ADM,\n" +
                "                 SUM(DECODE(CON.COD, '07', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_GASTOS_ADM,\n" +
                "                 SUM(DECODE(CON.COD, '08', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_MAT_ADICIONAL,\n" +
                "                 SUM(DECODE(CON.COD, '08', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_MAT_ADICIONAL,\n" +
                "                 SUM(DECODE(CON.COD, '09', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_ARRASTRES,\n" +
                "                 SUM(DECODE(CON.COD, '09', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_ARRASTRES,\n" +
                "                 SUM(DECODE(CON.COD, '10', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_CONGRESOS,\n" +
                "                 SUM(DECODE(CON.COD, '10', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_CONGRESOS,\n" +
                "                 SUM(DECODE(CON.COD, '11', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DERECHOS_CARGOS,\n" +
                "                 SUM(DECODE(CON.COD, '11', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DERECHOS_CARGOS,\n" +
                "                 SUM(DECODE(CON.COD, '12', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_PRACTICAS_ODONTO,\n" +
                "                 SUM(DECODE(CON.COD, '12', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_PRACTICAS_ODONTO,\n" +
                "                 SUM(DECODE(CON.COD, '13', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_PRACTICAS_HOSP,\n" +
                "                 SUM(DECODE(CON.COD, '13', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_PRACTICAS_HOSP,\n" +
                "                 SUM(DECODE(CON.COD, '14', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_INTERNADO_ROTATORIO,\n" +
                "                 SUM(DECODE(CON.COD, '14', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_INTERNADO_ROTATORIO,\n" +
                "                 SUM(DECODE(CON.COD, '15', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_GASTOS_TITULACION,\n" +
                "                 SUM(DECODE(CON.COD, '15', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_GASTOS_TITULACION,\n" +
                "                 SUM(DECODE(CON.COD, '16', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_CURSOS_EXTRA,\n" +
                "                 SUM(DECODE(CON.COD, '16', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_CURSOS_EXTRA,\n" +
                "                 SUM(DECODE(CON.COD, '17', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_TRAMITES_LEGALIZ,\n" +
                "                 SUM(DECODE(CON.COD, '17', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_TRAMITES_LEGALIZ,\n" +
                "                 SUM(DECODE(CON.COD, '18', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_VERANO_INVIERNO,\n" +
                "                 SUM(DECODE(CON.COD, '18', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_VERANO_INVIERNO,\n" +
                "                 SUM(DECODE(CON.COD, '19', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_DESAFIO_MAT,\n" +
                "                 SUM(DECODE(CON.COD, '19', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_DESAFIO_MAT,\n" +
                "                 SUM(DECODE(CON.COD, '20', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_EXAMEN_EXTEMP,\n" +
                "                 SUM(DECODE(CON.COD, '20', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_EXAMEN_EXTEMP,\n" +
                "                 SUM(DECODE(CON.COD, '21', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_SEGUNDO_TURNO,\n" +
                "                 SUM(DECODE(CON.COD, '21', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_SEGUNDO_TURNO,\n" +
                "                 SUM(DECODE(CON.COD, '22', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_SOUVENIRS,\n" +
                "                 SUM(DECODE(CON.COD, '22', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_SOUVENIRS,\n" +
                "                 SUM(DECODE(CON.COD, '23', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_MATERIAL_DIDACTICO,\n" +
                "                 SUM(DECODE(CON.COD, '23', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_MATERIAL_DIDACTICO,\n" +
                "                 SUM(DECODE(CON.COD, '24', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_ALQUILER_AUDITORIO,\n" +
                "                 SUM(DECODE(CON.COD, '24', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_ALQUILER_AUDITORIO,\n" +
                "                 SUM(DECODE(CON.COD, '25', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_ALQUILER_CAFETERIA,\n" +
                "                 SUM(DECODE(CON.COD, '25', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_ALQUILER_CAFETERIA,\n" +
                "                 SUM(DECODE(CON.COD, '26', DECODE(M.MONEDA,'B',M.MONTOTESO*M.CANT,0), 0) ) MONTO_B_RESERVA,\n" +
                "                 SUM(DECODE(CON.COD, '26', DECODE(M.MONEDA,'D',M.MONTOTESO*M.CANT,0), 0) ) MONTO_D_RESERVA\n" +
                "          FROM " + Constants.CASHBOX_SCHEMA + ".MOVIMIENTOS M, \n" +
                "               " + Constants.CASHBOX_SCHEMA + ".ESTRUCTURAS EST, \n" +
                "               " + Constants.CASHBOX_SCHEMA + ".CUENTAS CUEN, \n" +
                "               " + Constants.CASHBOX_SCHEMA + ".CONCEPTOS CON\n" +
                "          WHERE M.ESTADO = 'V'\n" +
                "                AND M.EST_COD = EST.CODIGO\n" +
                "                AND M.CUEN_ID = CUEN.ID\n" +
                "                AND CUEN.CON_COD = CON.COD\n";
        sql += addFilters();

        sql += "          GROUP BY TRUNC(M.FECHA)\n" +
                "    ) M\n" +
                "    GROUP BY TO_CHAR(M.FECHA,'MM'), TO_CHAR(M.FECHA,'YYYY')\n" +
                ") ORDER BY CODIGO";
        return sql;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    private String addFilters() {
        String sql = "";
        if (null != executorUnitCode) {
            sql += " AND EST.UNIDAD_ACAD_ADM = " + executorUnitCode + "\n";
        }

        if (null != startDate) {
            sql += " AND TO_NUMBER(TO_CHAR(M.FECHA,'YYYY') || TO_CHAR(M.FECHA,'MM') || TO_CHAR(M.FECHA,'DD')) >= " + startDate + "\n";
        }

        if (null != endDate) {
            sql += " AND TO_NUMBER(TO_CHAR(M.FECHA,'YYYY') || TO_CHAR(M.FECHA,'MM') || TO_CHAR(M.FECHA,'DD')) <= " + endDate + "\n";
        }

        return sql;
    }
}
