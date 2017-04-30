package com.encens.khipus.dashboard.module.academics.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.26
 */
public class DocumentationRegistrationWidgetSql implements SqlQuery {
    private Integer executorUnitId;
    private String career;
    private Integer period;
    private String documentType;
    private Integer gestion;
    public String getSql() {

        String sql = "select count (*), sum(estudiantes_inscritos) estudiantes_inscritos, sum(estudiantes_mora_docus) estudiantes_mora_docus, round(sum(estudiantes_mora_docus)/decode(sum(estudiantes_inscritos),0,1,sum(estudiantes_inscritos))*100, 2) porcentaje_mora_docus\n" +
                "     from (\n" +
                "        select 0 estudiantes_inscritos, sum(1) estudiantes_mora_docus \n" +
                "        from    "+Constants.ACADEMIC_SCHEMA+".inscripciones ins, "+Constants.ACADEMIC_SCHEMA+".planes_estudios pl, "+Constants.ACADEMIC_SCHEMA+".estudiantes es \n" +
                "        where    ";
                if(null!=gestion){
                    sql+=" ins.gestion = "+gestion+" and \n";
                }
                if(null!=period){
                    sql+="                ins.periodo = "+period+" and \n";
                }
                if(null!=career){
                    sql+="               ins.plan_estudio    = '"+career+"' and \n" ;
                }
                sql+="                ins.plan_estudio    = pl.plan_estudio and \n";
                if(null!=executorUnitId){
                    sql+="               pl.unidad_acad_adm    = "+executorUnitId+" and \n ";
                }
                sql+="                ins.estudiante        = es.estudiante \n" +
                "        and        ( \n" +
                "                    '"+documentType+"' not in ('DI16','DI17','DI18','DI19','DI20','DI21','DI22','DI23','DI24','DI25','DI3','DI5','DI6','DI26') \n" +
                "                    or \n" +
                "                    ( \n" +
                "                        (es.pais_1 <> 'BOL' and '"+documentType+"' in ('DI16','DI17','DI18','DI19','DI20','DI21','DI22','DI23','DI24','DI25')) \n" +
                "                        or \n" +
                "                        (es.pais_1 = 'BOL' and '"+documentType+"' in ('DI3','DI5','DI6','DI26')) \n" +
                "                    ) \n" +
                "                ) \n" +
                "        and (    select count(*) \n" +
                "                    from "+Constants.ACADEMIC_SCHEMA+".registros_academicos ra \n" +
                "                    where ra.estudiante = ins.estudiante \n" +
                "                    and    ra.gestion        = ins.gestion \n" +
                "                    and    ra.periodo      = ins.periodo \n" +
                "                    and    ra.plan_estudio = ins.plan_estudio \n" +
                "                    and    ra.modalidad = 'NORM') > 0 \n" +
                "        and not exists (select * \n" +
                "                                from     "+Constants.ACADEMIC_SCHEMA+".documentos_ingreso di \n" +
                "                                where    ins.estudiante = di.estudiante \n" +
                "                                and        di.valido = 'SI' \n" +
                "                                and        di.tipo_documento_ingreso = '"+documentType+"' ) \n" +
                "        union all \n" +
                "        select  sum(1) estudiantes_inscritos, 0 estudiantes_mora_docus \n" +
                "        from    "+Constants.ACADEMIC_SCHEMA+".inscripciones ins, "+Constants.ACADEMIC_SCHEMA+".planes_estudios pl \n" +
                "        where    ";
                if(null!=gestion){
                    sql+="        ins.gestion = "+gestion+" and \n ";
                }
                if(null!=period){
                    sql+="                ins.periodo = "+period +" and \n";
                }
                if(null!=career){
                    sql+= "                ins.plan_estudio    = '"+career+"' and \n";
                }
                sql+="                ins.plan_estudio    = pl.plan_estudio and \n";
                if(null!=executorUnitId){
                    sql+="                pl.unidad_acad_adm = "+executorUnitId+" and\n ";
                }
                sql+="         (    select count(*) \n" +
                "                    from "+Constants.ACADEMIC_SCHEMA+".registros_academicos ra \n" +
                "                    where ra.estudiante = ins.estudiante \n" +
                "                    and    ra.gestion        = ins.gestion \n" +
                "                    and    ra.periodo      = ins.periodo \n" +
                "                    and    ra.plan_estudio = ins.plan_estudio \n" +
                "                    and    ra.modalidad = 'NORM') > 0 \n" +
                ")\n";
        return sql;
    }


    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public void setDocumentType(String documentType) {

        this.documentType = documentType;
    }
}
