package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * @author
 * @version 2.7
 * @deprecated
 */
public class BudgetSqlQuery implements SqlQuery {
    private Integer executorUnitCode = null;
    private Integer year = DateUtils.getCurrentYear(new Date());
    private Integer monthNumber;
    private String monthConstant;

    private String multiplier;
    private String tableName;
    private String type;


    public BudgetSqlQuery(String tableName, String type, String multiplier) {
        this.tableName = tableName;
        this.type = type;
        this.multiplier = multiplier;
    }

    public String getSql() {
        String sql = "SELECT" +
                "       (select SUM(" + tableName + ".importe) \n" +
                "        from " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + " \n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".unidadnegocio unidadNegocio on " + tableName + ".idunidadnegocio = unidadNegocio.idunidadnegocio\n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion on " + tableName + ".idgestion = gestion.idgestion \n" +
                "        where gestion.anio = " + year + "\n";
        if (null != executorUnitCode) {
            sql += "          AND TO_NUMBER(unidadNegocio.codunidadejecutora) = " + executorUnitCode + "\n";
        }
        sql += "       ) as presupuestoAnual , " +
                "       (select SUM(detalleMovimientoCuenta1.MONTO_MN)*" + multiplier + " \n" +
                "        from " + Constants.FINANCES_SCHEMA + ".CG_MOVDET detalleMovimientoCuenta1 LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE movimientoCuenta1 \n" +
                "             on detalleMovimientoCuenta1.NO_CIA = movimientoCuenta1.NO_CIA \n" +
                "             and detalleMovimientoCuenta1.TIPO_COMPRO = movimientoCuenta1.TIPO_COMPRO \n" +
                "             and detalleMovimientoCuenta1.NO_COMPRO=movimientoCuenta1.NO_COMPRO LEFT JOIN " + Constants.FINANCES_SCHEMA + ".ARCGMS cuentaCaja1 \n" +
                "             on detalleMovimientoCuenta1.NO_CIA = cuentaCaja1.NO_CIA \n" +
                "             and detalleMovimientoCuenta1.CUENTA = cuentaCaja1.CUENTA \n" +
                "        where TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'MM')) < " + monthNumber + " \n" +
                "              and TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'YYYY'))= " + year + " \n" +
                "              and  cuentaCaja1.CUENTA IN (select distinct cuentaClasificador2.codigocuenta \n" +
                "                                          from " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + "2 LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasificador clasificador2 \n" +
                "                                               on " + tableName + "2.idclasificador = clasificador2.idclasificador LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasifcuenta cuentaClasificador2 \n" +
                "                                               on clasificador2.idclasificador = cuentaClasificador2.idclasificador LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion2 \n" +
                "                                               on " + tableName + "2.idgestion=gestion2.idgestion where gestion2.anio = " + year + ")\n";
        if (null != executorUnitCode) {
            sql += "             and TO_NUMBER(detalleMovimientoCuenta1.COD_UNI) = " + executorUnitCode + "\n";
        }

        sql += "       ) as ejecucionAcumulada,\n" +
                "       (select SUM(detalleMovimientoCuenta1.MONTO_MN)*" + multiplier + " \n" +
                "        from " + Constants.FINANCES_SCHEMA + ".CG_MOVDET detalleMovimientoCuenta1 LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CG_MOVMAE movimientoCuenta1 \n" +
                "             on detalleMovimientoCuenta1.NO_CIA = movimientoCuenta1.NO_CIA \n" +
                "             and detalleMovimientoCuenta1.TIPO_COMPRO = movimientoCuenta1.TIPO_COMPRO \n" +
                "             and detalleMovimientoCuenta1.NO_COMPRO = movimientoCuenta1.NO_COMPRO\n" +
                "        where TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'MM')) = " + monthNumber + " \n" +
                "              and TO_NUMBER(TO_CHAR(movimientoCuenta1.fecha,'YYYY'))= " + year + " \n" +
                "              and  detalleMovimientoCuenta1.CUENTA IN (select distinct cuentaClasificador2.codigocuenta \n" +
                "                                          from " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + "2 LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasificador clasificador2 \n" +
                "                                               on " + tableName + "2.idclasificador = clasificador2.idclasificador LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".clasifcuenta cuentaClasificador2 \n" +
                "                                               on clasificador2.idclasificador = cuentaClasificador2.idclasificador LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion2 \n" +
                "                                               on " + tableName + "2.idgestion=gestion2.idgestion \n" +
                "                                          where gestion2.anio = " + year + ") \n";
        if (null != executorUnitCode) {
            sql += "              and TO_NUMBER(detalleMovimientoCuenta1.COD_UNI) = " + executorUnitCode + "\n";
        }

        sql += "       ) as ejecucionMensual,\n" +
                "       (select SUM(" + tableName + ".importe * (( " +
                "                                                   case when presupuestogasto.IDDISTPRESUPUESTO is null then " +
                "                                                       (select avg(detalleDistribPresupuesto7.porcentajeDist) \n" +
                "                                                       from " + Constants.KHIPUS_SCHEMA + ".distpresupuesto distribucionPresupuesto7 LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".distpresdet detalleDistribPresupuesto7 \n" +
                "                                                       on distribucionPresupuesto7.iddistpresupuesto = detalleDistribPresupuesto7.iddistpresupuesto LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion7 \n" +
                "                                                       on distribucionPresupuesto7.idgestion = gestion7.idgestion \n";
        if (null != executorUnitCode) {
            sql += "                                                    left join unidadnegocio unidadnegocio2  on distribucionPresupuesto7.idunidadnegocio =  unidadnegocio2.idunidadnegocio\n";
        }
        sql += "                                                        where gestion7.anio = " + year + " \n" +
                "                                                       and detalleDistribPresupuesto7.mes = '" + monthConstant + "' \n" +
                "                                                       and distribucionPresupuesto7.tipo='" + type + "') \n" +
                "                                                   else \n" +
                "                                                       (\n" +
                "                                                       select distpresdet1.PORCENTAJEDIST from " + Constants.KHIPUS_SCHEMA + ".DISTPRESDET distpresdet1 \n" +
                "                                                       WHERE distpresdet1.IDDISTPRESUPUESTO=presupuestogasto.IDDISTPRESUPUESTO \n" +
                "                                                       and distpresdet1.mes = '" + monthConstant + "' \n" +
                "                                                       ) \n" +
                "                                                   end \n";

        sql += "                                                     ) /100)) \n" +
                "        from " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + " \n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".unidadnegocio unidadNegocio on " + tableName + ".idunidadnegocio = unidadNegocio.idunidadnegocio\n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion on " + tableName + ".idgestion = gestion.idgestion \n" +
                "        where gestion.anio = " + year + "\n";
        if (null != executorUnitCode) {
            sql += "          AND TO_NUMBER(unidadNegocio.codunidadejecutora) = " + executorUnitCode + "\n";
        }

        sql += "       ) as presupuestoMensual \n" +
                "       \n" +
                "FROM dual";
        return sql;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void setMonthNumber(Integer monthNumber) {
        this.monthNumber = monthNumber;
    }

    public void setMonthConstant(String monthConstant) {
        this.monthConstant = monthConstant;
    }

    public Integer getYear() {
        return year;
    }
}
