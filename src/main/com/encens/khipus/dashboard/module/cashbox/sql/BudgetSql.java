package com.encens.khipus.dashboard.module.cashbox.sql;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.19
 */
public class BudgetSql implements SqlQuery {
    private Integer executorUnitCode = null;
    private Integer year = DateUtils.getCurrentYear(new Date());
    private String tableName;
    private String multiplier;
    private String type;

    public String getSql() {

        String sql = "select presupuestoAnual, ejecucionAcumulada, ejecucionMensual, presupuestoMensual, MES_LITERAL\n"
                + " from (\n";
        List<Month> availableMonths = getMonths();
        for (int i = 0; i < availableMonths.size(); i++) {
            Month month = availableMonths.get(i);
            sql += basicSql(tableName, multiplier, month.name(), month.getValue() + 1, type, MessageUtils.getMessage(month.getResourceKey()));

            if (i < availableMonths.size() - 1) {
                sql += "\n union all \n";
            }
        }
        sql += "\n)";

        return sql;
    }

    private List<Month> getMonths() {
        Month[] enumValues = Month.values();
        Integer currentMonth = DateUtils.getCurrentMonth(new Date());

        List<Month> result = new ArrayList<Month>();

        for (Month month : enumValues) {
            if (month.getValue() + 1 <= currentMonth) {
                result.add(month);
            }
        }

        return result;
    }

    private String basicSql(String tableName, String multiplier, String monthConstant, Integer monthNumber, String type, String monthName) {
        String yearlyBudgetSql = getYearBudget(tableName);
        String accumulatedExecutionSql = getAccumulatedExecution(tableName, multiplier, monthNumber);
        String monthlyExecutionSql = getMonthlyExecution(tableName, multiplier, monthNumber);
        String monthlyBudgetSql = getMonthlyBudget(tableName, monthConstant, type);

        return "select " + yearlyBudgetSql + ", "
                + accumulatedExecutionSql + ", "
                + monthlyExecutionSql + ", "
                + monthlyBudgetSql + ", "
                + "'" + monthName.toUpperCase() + "'" + " as MES_LITERAL \n"
                + " FROM dual";
    }

    private String getYearBudget(String tableName) {
        String sql = "       (select SUM(" + tableName + ".importe) \n" +
                "        from " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + " \n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".unidadnegocio unidadNegocio on " + tableName + ".idunidadnegocio = unidadNegocio.idunidadnegocio\n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion on " + tableName + ".idgestion = gestion.idgestion \n" +
                "        where gestion.anio = " + year + "\n";
        if (null != executorUnitCode) {
            sql += "          AND TO_NUMBER(unidadNegocio.codunidadejecutora) = " + executorUnitCode + "\n";
        }
        sql += "       ) as presupuestoAnual\n";

        return sql;
    }

    private String getAccumulatedExecution(String tableName, String multiplier, Integer monthNumber) {
        String sql = "       (select SUM(detalleMovimientoCuenta1.MONTO_MN)*" + multiplier + " \n" +
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

        sql += "       ) as ejecucionAcumulada\n";

        return sql;
    }

    private String getMonthlyExecution(String tableName, String multiplier, Integer monthNumber) {
        String sql = "       (select SUM(detalleMovimientoCuenta1.MONTO_MN)*" + multiplier + " \n" +
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

        sql += "       ) as ejecucionMensual\n";
        return sql;
    }

    private String getMonthlyBudget(String tableName, String monthConstant, String type) {
        String sql = "       (select SUM(" + tableName + ".importe * ((select avg(detalleDistribPresupuesto7.porcentajeDist)\n" +
                "                                               from " + Constants.KHIPUS_SCHEMA + ".distpresupuesto distribucionPresupuesto7 LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".distpresdet detalleDistribPresupuesto7 \n" +
                "                                                    on distribucionPresupuesto7.iddistpresupuesto = detalleDistribPresupuesto7.iddistpresupuesto LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion7 \n" +
                "                                                    on distribucionPresupuesto7.idgestion = gestion7.idgestion \n";
        if (null != executorUnitCode) {
            sql += "                                                    left join unidadnegocio unidadnegocio2  on distribucionPresupuesto7.idunidadnegocio =  unidadnegocio2.idunidadnegocio\n";
        }
        sql += "                                                    where gestion7.anio = " + year + " \n" +
                "                                                    and detalleDistribPresupuesto7.mes = '" + monthConstant + "' \n" +
                "                                                    and distribucionPresupuesto7.tipo='" + type + "'\n";

        if (null != executorUnitCode) {
            sql += "                                                    and TO_NUMBER(unidadnegocio2.codunidadejecutora) = " + executorUnitCode + " \n";
        }
        sql += "                                                     ) /100)) \n" +
                "        from " + Constants.KHIPUS_SCHEMA + "." + tableName + " " + tableName + " \n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".unidadnegocio unidadNegocio on " + tableName + ".idunidadnegocio = unidadNegocio.idunidadnegocio\n" +
                "           LEFT JOIN " + Constants.KHIPUS_SCHEMA + ".gestion gestion on " + tableName + ".idgestion = gestion.idgestion \n" +
                "        where gestion.anio = " + year + "\n";
        if (null != executorUnitCode) {
            sql += "          AND TO_NUMBER(unidadNegocio.codunidadejecutora) = " + executorUnitCode + "\n";
        }

        sql += "       ) as presupuestoMensual \n";

        return sql;
    }

    public void setExecutorUnitCode(Integer executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    public void setType(String type) {
        this.type = type;
    }
}
