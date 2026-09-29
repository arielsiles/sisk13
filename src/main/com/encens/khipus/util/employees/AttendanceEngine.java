package com.encens.khipus.util.employees;

import java.util.Date;

/**
 * Con que motor se controla la asistencia de una planilla.
 * <p/>
 * Los dos conviven a proposito y por varios periodos. Una empresa que viene usando bandas desde
 * hace anios no puede cambiar de criterio de un mes al otro: necesita pasar una categoria por vez,
 * comparar una planilla entera, y recien despues la siguiente.
 * <p/>
 * <b>La decision es una FECHA y no un interruptor.</b> Con un interruptor, cambiar en marzo y
 * regenerar enero daria numeros distintos a la planilla oficial de enero: un interruptor no tiene
 * memoria. Con una fecha de corte en la categoria de puesto, cada periodo resuelve siempre igual y
 * regenerar un mes viejo reproduce el mes viejo.
 * <p/>
 * El motor que se uso <b>se sella en la planilla generada</b>. No para decidir nada -eso ya lo
 * decidio la fecha- sino para que el reporte sepa que columnas mostrar y para que dentro de dos
 * anios se pueda ver con que motor se pago sin volver a calcular ninguna regla.
 *
 * @author
 * @version 6.1.0
 */
public enum AttendanceEngine {

    /** Bandas horarias sobre el puesto-contrato, con la tolerancia de la categoria. */
    BANDS("AttendanceEngine.bands"),

    /** Jornadas resueltas por las cuatro capas, con la tolerancia de cada turno. */
    JOURNEYS("AttendanceEngine.journeys");

    private final String resourceKey;

    AttendanceEngine(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public boolean isJourneys() {
        return JOURNEYS.equals(this);
    }

    /**
     * El motor que le toca a un periodo.
     *
     * @param journeysFrom fecha desde la cual la categoria se controla por jornadas; null
     *                     significa que todavia no cambio y sigue con bandas
     * @param periodStart  el primer dia del periodo que se esta generando
     */
    public static AttendanceEngine of(Date journeysFrom, Date periodStart) {
        if (null == journeysFrom || null == periodStart) {
            return BANDS;
        }
        return periodStart.before(journeysFrom) ? BANDS : JOURNEYS;
    }
}
