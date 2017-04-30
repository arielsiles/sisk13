package com.encens.khipus.dashboard.module.academics.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.26
 */
public class DesertionWidgetSql implements SqlQuery {

    private Integer executorUnitId;
    private String career;
    private Integer gestion;
    private Integer period;

    public String getSql() {
        String sql = "select count(*)," +
                "            sum(estudiantes_inscritos) estudiantes_inscritos," +
                "            sum(estudiantes_desercion) estudiantes_desercion," +
                "            round(sum(estudiantes_desercion) / sum(estudiantes_inscritos) * 100, 2) porcentaje_desercion" +
                "    from (" +
                "        select 1 estudiantes_inscritos, 1 estudiantes_desercion" +
                "        from " + Constants.ACADEMIC_SCHEMA + ".inscripciones ins, " + Constants.ACADEMIC_SCHEMA + ".planes_estudios pl" +
                "        where ins.plan_estudio = pl.plan_estudio" +
                (executorUnitId != null ? " and pl.unidad_acad_adm = " + executorUnitId : "") +
                (gestion != null ? " and ins.gestion = " + gestion : "") +
                (period != null ? " and ins.periodo = " + period : "") +
                (career != null ? " and ins.plan_estudio = " + career : "") +
                "        and (select count(*)" +
                "            from " + Constants.ACADEMIC_SCHEMA + ".registros_academicos ra" +
                "            where ra.estudiante = ins.estudiante" +
                "            and ra.gestion = ins.gestion" +
                "            and ra.periodo = ins.periodo" +
                "            and ra.plan_estudio = ins.plan_estudio" +
                "            and ra.modalidad = 'NORM') > 0" +
                "        and ((select count(*)" +
                "            from " + Constants.ACADEMIC_SCHEMA + ".registros_academicos ra" +
                "            where ra.estudiante = ins.estudiante" +
                "            and ra.gestion = ins.gestion" +
                "            and ra.periodo = ins.periodo" +
                "            and ra.plan_estudio = ins.plan_estudio" +
                "            and ra.modalidad = 'NORM') =" +
                "            (select count(*)" +
                "                from " + Constants.ACADEMIC_SCHEMA + ".registros_academicos ra" +
                "                where ra.estudiante = ins.estudiante" +
                "                and ra.gestion = ins.gestion" +
                "                and ra.periodo = ins.periodo" +
                "                and ra.plan_estudio = ins.plan_estudio" +
                "                and ra.modalidad = 'NORM'" +
                "                and ra.resultado = 'ABA')" +
                "        )" +
                "        union all" +
                "        select 1 estudiantes_inscritos, 0 estudiantes_desercion" +
                "        from " + Constants.ACADEMIC_SCHEMA + ".inscripciones ins, " + Constants.ACADEMIC_SCHEMA + ".planes_estudios pl" +
                "        where 1 = 1" +
                (executorUnitId != null ? " and pl.unidad_acad_adm = " + executorUnitId : "") +
                (gestion != null ? " and ins.gestion = " + gestion : "") +
                (period != null ? " and ins.periodo = " + period : "") +
                (career != null ? " and ins.plan_estudio = " + career : "") +
                "        and ins.plan_estudio = pl.plan_estudio" +
                "        and (select count(*)" +
                "            from " + Constants.ACADEMIC_SCHEMA + ".registros_academicos ra" +
                "            where ra.estudiante = ins.estudiante" +
                "            and ra.gestion = ins.gestion" +
                "            and ra.periodo = ins.periodo" +
                "            and ra.plan_estudio = ins.plan_estudio" +
                "            and ra.modalidad = 'NORM') > 0" +
                "        and ((select count(*)" +
                "            from " + Constants.ACADEMIC_SCHEMA + ".registros_academicos ra" +
                "            where ra.estudiante = ins.estudiante" +
                "            and ra.gestion = ins.gestion" +
                "            and ra.periodo = ins.periodo" +
                "            and ra.plan_estudio = ins.plan_estudio" +
                "            and ra.modalidad = 'NORM') >" +
                "            (select count(*)" +
                "            from " + Constants.ACADEMIC_SCHEMA + ".registros_academicos ra" +
                "            where ra.estudiante = ins.estudiante" +
                "            and ra.gestion = ins.gestion" +
                "            and ra.periodo = ins.periodo" +
                "            and ra.plan_estudio = ins.plan_estudio" +
                "            and ra.modalidad = 'NORM'" +
                "            and ra.resultado = 'ABA')" +
                "        )" +
                ")";

        return sql;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }
}
