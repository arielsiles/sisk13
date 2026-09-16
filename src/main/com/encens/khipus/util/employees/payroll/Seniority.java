package com.encens.khipus.util.employees.payroll;

import com.encens.khipus.model.employees.SeniorityBonus;
import com.encens.khipus.model.employees.SeniorityBonusDetail;
import com.encens.khipus.util.DateUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * La antiguedad: cuantos anios lleva la persona y cuanto bono le corresponde.
 * <p/>
 * Vivia dentro de los calculadores de la planilla tributaria, asi que solo existia para quienes
 * generan planilla fiscal. La planilla de sueldos -que es la de revision general- no podia
 * mostrarla, y copiarla ahi habria dejado dos reglas que algun dia dirian anios distintos para la
 * misma persona.
 *
 * @author
 * @version 6.1.0
 */
public final class Seniority {

    private static final int DAYS_OF_YEAR = 365;

    private Seniority() {
    }

    /**
     * Los anios cumplidos al cierre del periodo.
     * <p/>
     * Se suma un dia a la fecha de ingreso para que la antiguedad se cuente recien el mes
     * siguiente, y se exige superar el anio -no alcanzarlo- para sumar uno. Las dos reglas venian
     * del calculador de la tributaria y se conservan tal cual: cambiarlas movería el bono de
     * quien ya lo cobra.
     */
    public static int yearsOf(Date hireDate, Date periodEnd) {
        if (null == hireDate || null == periodEnd) {
            return 0;
        }
        Long days = DateUtils.daysBetween(DateUtils.addDay(hireDate, 1), periodEnd, false);
        if (null == days) {
            return 0;
        }
        int years = 0;
        while (days > DAYS_OF_YEAR) {
            years++;
            days = days - DAYS_OF_YEAR;
        }
        return years;
    }

    /**
     * El bono que corresponde a esos anios, o cero.
     * <p/>
     * Cero significa <b>sin tramo configurado</b>, no "no le toca". Si la empresa paga el bono y
     * la tabla de tramos esta vacia, el resultado es el mismo que si no lo pagara: por eso la
     * pantalla muestra la columna solo cuando hay algo que mostrar.
     */
    public static BigDecimal bonusOf(SeniorityBonus seniorityBonus, Integer years) {
        SeniorityBonusDetail rule = ruleOf(seniorityBonus, years);
        return null == rule || null == rule.getAmount() ? BigDecimal.ZERO : rule.getAmount();
    }

    private static SeniorityBonusDetail ruleOf(SeniorityBonus seniorityBonus, Integer years) {
        if (null == seniorityBonus || null == years) {
            return null;
        }
        for (SeniorityBonusDetail detail : seniorityBonus.getDetails()) {
            if (null == detail.getStartYear() || detail.getStartYear() > years) {
                continue;
            }
            if (null == detail.getEndYear() || years <= detail.getEndYear()) {
                return detail;
            }
        }
        return null;
    }
}
