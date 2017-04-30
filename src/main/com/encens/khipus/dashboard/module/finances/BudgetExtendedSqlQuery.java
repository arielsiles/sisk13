package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.8
 */
public class BudgetExtendedSqlQuery implements SqlQuery {
    private Integer executorUnitCode;
    private Integer year;
    private Month month;

    private String multiplier;
    private String tableName;
    private String type;
    private String classType;

    public BudgetExtendedSqlQuery(String multiplier, String tableName, String type, String classType) {
        this.multiplier = multiplier;
        this.tableName = tableName;
        this.type = type;
        this.classType = classType;
    }

    public String getSql() {
        String sql = "SELECT \n" +
                "CODIGOCLASIFICADOR,\n" +
                "NOMBRECLASIFICADOR,\n" +
                "PRESUPUESTOANUAL,\n" +
                "EJECUCIONACUMULADA,\n" +
                "PRESUPUESTOMENSUAL,\n" +
                "EJECUACIONMENSUAL,\n" +
                "(DECODE(PRESUPUESTOMENSUAL,NULL,0,PRESUPUESTOMENSUAL,PRESUPUESTOMENSUAL) - DECODE(EJECUACIONMENSUAL,NULL,0,EJECUACIONMENSUAL,EJECUACIONMENSUAL)) DIFERENCIAEJECUCIONMENSUAL,\n" +
                "(DECODE(EJECUCIONACUMULADA,NULL,0,EJECUCIONACUMULADA,EJECUCIONACUMULADA) + DECODE(EJECUACIONMENSUAL,NULL,0,EJECUACIONMENSUAL,EJECUACIONMENSUAL)) EJECUCIONACTUAL,\n" +
                "(DECODE(PRESUPUESTOANUAL,NULL,0,PRESUPUESTOANUAL,PRESUPUESTOANUAL) - ((DECODE(EJECUCIONACUMULADA,NULL,0,EJECUCIONACUMULADA,EJECUCIONACUMULADA) + DECODE(EJECUACIONMENSUAL,NULL,0,EJECUACIONMENSUAL,EJECUACIONMENSUAL)))) DIFERENCIADEJECUCIONANUAL,\n" +
                "ROUND((((DECODE(PRESUPUESTOANUAL,NULL,0,PRESUPUESTOANUAL,PRESUPUESTOANUAL) - ((DECODE(EJECUCIONACUMULADA,NULL,0,EJECUCIONACUMULADA,EJECUCIONACUMULADA) + DECODE(EJECUACIONMENSUAL,NULL,0,EJECUACIONMENSUAL,EJECUACIONMENSUAL))))*100)/PRESUPUESTOANUAL),2) PORCENTAJEEJECUCIONANUAL\n" +
                "FROM \n" +
                "(\n" +
                "SELECT \n" +
                "cr.codigo CODIGOCLASIFICADOR,\n" +
                "cr.nombre NOMBRECLASIFICADOR,\n" +
                "(select sum(" + tableName + "1.importe) \n" +
                "        from " + tableName + "  " + tableName + "1 \n" +
                "        LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".unidadnegocio unidadNegocio on " + tableName + "1.idunidadnegocio = unidadNegocio.idunidadnegocio\n" +
                "        LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion  on " + tableName + "1.idgestion = gestion.idgestion \n" +
                "        where \n" +
                "        gestion.anio = " + year + " \n";

        sql += setFilters("unidadNegocio.codunidadejecutora");

        sql += "        and " + tableName + "1.idclasificador=cr.idclasificador\n" +
                "        ) as PRESUPUESTOANUAL,\n" +
                "        \n" +
                "       (select SUM(detalleMovimientoCuenta1.MONTO_MN)*" + multiplier + " \n" +
                "        from " + Constants.FINANCES_SCHEMA + ".CG_MOVDET detalleMovimientoCuenta1 LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE movimientoCuenta1 \n" +
                "             on detalleMovimientoCuenta1.NO_CIA = movimientoCuenta1.NO_CIA \n" +
                "             and detalleMovimientoCuenta1.TIPO_COMPRO = movimientoCuenta1.TIPO_COMPRO \n" +
                "             and detalleMovimientoCuenta1.NO_COMPRO=movimientoCuenta1.NO_COMPRO LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS cuentaCaja1 \n" +
                "             on detalleMovimientoCuenta1.NO_CIA = cuentaCaja1.NO_CIA \n" +
                "             and detalleMovimientoCuenta1.CUENTA = cuentaCaja1.CUENTA \n" +
                "        where TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'MM')) < " + getMonthValue() + " \n" +
                "             and TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'YYYY'))= " + year + " \n" +
                "             and  cuentaCaja1.CUENTA IN (select distinct cuentaClasificador2.codigocuenta \n" +
                "                                          from  " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + "2 \n" +
                "                                              LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasificador clasificador2 on " + tableName + "2.idclasificador = clasificador2.idclasificador \n" +
                "                                              LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasifcuenta cuentaClasificador2 on clasificador2.idclasificador = cuentaClasificador2.idclasificador\n" +
                "                                              LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion2 on " + tableName + "2.idgestion=gestion2.idgestion \n" +
                "                                          where gestion2.anio = " + year + "\n" +
                "                                              and clasificador2.idclasificador=cr.idclasificador)\n";

        sql += setFilters("detalleMovimientoCuenta1.COD_UNI");

        sql += "       ) as EJECUCIONACUMULADA,\n" +
                "       (select SUM(detalleMovimientoCuenta1.MONTO_MN)*" + multiplier + " \n" +
                "        from " + Constants.FINANCES_SCHEMA + ".CG_MOVDET detalleMovimientoCuenta1 LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE movimientoCuenta1 \n" +
                "             on detalleMovimientoCuenta1.NO_CIA = movimientoCuenta1.NO_CIA \n" +
                "             and detalleMovimientoCuenta1.TIPO_COMPRO = movimientoCuenta1.TIPO_COMPRO \n" +
                "             and detalleMovimientoCuenta1.NO_COMPRO = movimientoCuenta1.NO_COMPRO\n" +
                "        where TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'MM')) = " + getMonthValue() + " \n" +
                "              and TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'YYYY')) = " + year + " \n" +
                "              and  detalleMovimientoCuenta1.CUENTA IN (select distinct cuentaClasificador2.codigocuenta \n" +
                "                                          from " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + "2 \n" +
                "                                              LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasificador clasificador2 on " + tableName + "2.idclasificador = clasificador2.idclasificador \n" +
                "                                              LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasifcuenta cuentaClasificador2 on clasificador2.idclasificador = cuentaClasificador2.idclasificador\n" +
                "                                              LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion2 on " + tableName + "2.idgestion=gestion2.idgestion \n" +
                "                                          where gestion2.anio = " + year + "\n" +
                "                                              and clasificador2.idclasificador=cr.idclasificador) \n";

        sql += setFilters("detalleMovimientoCuenta1.COD_UNI");

        sql += "       ) as EJECUACIONMENSUAL,\n" +
                "       ROUND((select sum(" + tableName + "1.importe*((select avg(detalleDistribPresupuesto7.porcentajeDist)\n" +
                "                                              from " + Constants.KHIPUS_SCHEMA + ".distpresupuesto distribucionPresupuesto7 LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".distpresdet detalleDistribPresupuesto7 \n" +
                "                                                on distribucionPresupuesto7.iddistpresupuesto = detalleDistribPresupuesto7.iddistpresupuesto LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion7 \n" +
                "                                                on distribucionPresupuesto7.idgestion = gestion7.idgestion \n" +
                "                                                \n";

        sql += addJoin();

        sql += "                                                \n" +
                "                                              where gestion7.anio = " + year + " \n" +
                "                                                and detalleDistribPresupuesto7.mes = '" + month.name() + "' \n" +
                "                                                and distribucionPresupuesto7.tipo='" + type + "'\n";

        sql += setFilters("unidadnegocio2.codunidadejecutora");

        sql += "                                                ) /100)) \n" +
                "        from " + tableName + "  " + tableName + "1 \n" +
                "        LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".unidadnegocio unidadNegocio on " + tableName + "1.idunidadnegocio = unidadNegocio.idunidadnegocio\n" +
                "        LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion  on " + tableName + "1.idgestion = gestion.idgestion \n" +
                "        where \n" +
                "        gestion.anio = " + year + " \n";

        sql += setFilters("unidadNegocio.codunidadejecutora");

        sql += "        and " + tableName + "1.idclasificador=cr.idclasificador\n" +
                "        ),2) as PRESUPUESTOMENSUAL\n" +
                "\n" +
                "FROM " + Constants.KHIPUS_SCHEMA + ".CLASIFICADOR cr \n" +
                "WHERE cr.tipo='" + classType + "'\n" +
                "ORDER BY cr.codigo) T";
        return sql;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    private String setFilters(String property) {
        String sql = "";
        if (null != executorUnitCode) {
            sql = " and TO_NUMBER(" + property + ") = " + executorUnitCode + "\n";
        }

        return sql;
    }

    private String addJoin() {
        if (null != executorUnitCode) {
            return "  left join unidadnegocio unidadnegocio2  on distribucionPresupuesto7.idunidadnegocio =  unidadnegocio2.idunidadnegocio\n";
        }

        return "";
    }

    private Integer getMonthValue() {
        return month.getValue() + 1;
    }
}
