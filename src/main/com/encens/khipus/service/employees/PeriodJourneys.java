package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Todo lo que las cuatro capas resuelven para un periodo, en una sola pasada.
 * <p/>
 * Existe por una razon de costo, no de estilo. Resolver un dia suelto cuesta entre tres y cuatro
 * consultas -excepcion, pertenencia al grupo, horario fijo, feriado-, y la verificacion las pedia
 * <b>por dia y dos veces</b>: una para el origen y otra para la jornada que el feriado suprimio.
 * Con una persona son unas doscientas consultas y no se nota; con los doscientos cuarenta y cinco
 * de una planilla son decenas de miles y la generacion se vuelve inusable.
 * <p/>
 * Las claves son el inicio del dia en milisegundos, que es como el motor identifica un dia en
 * todas partes.
 *
 * @author
 * @version 6.1.0
 */
public class PeriodJourneys {

    private final Map<Long, ScheduledJourney> journeys = new HashMap<Long, ScheduledJourney>();
    private final Map<Long, JourneyResolverService.JourneySource> sources
            = new HashMap<Long, JourneyResolverService.JourneySource>();
    private final Map<Long, ScheduledJourney> suppressed = new HashMap<Long, ScheduledJourney>();
    private final Map<Long, WorkGroupMembership> memberships = new HashMap<Long, WorkGroupMembership>();

    public void put(Long day, ScheduledJourney journey,
                    JourneyResolverService.JourneySource source,
                    ScheduledJourney suppressedJourney,
                    WorkGroupMembership membership) {
        if (null != journey) {
            journeys.put(day, journey);
        }
        sources.put(day, source);
        if (null != suppressedJourney) {
            suppressed.put(day, suppressedJourney);
        }
        if (null != membership) {
            memberships.put(day, membership);
        }
    }

    public ScheduledJourney journeyOf(Long day) {
        return journeys.get(day);
    }

    /** Nunca null: un dia sin nada resuelto es NONE, que es un resultado y no un vacio. */
    public JourneyResolverService.JourneySource sourceOf(Long day) {
        JourneyResolverService.JourneySource source = sources.get(day);
        return null == source ? JourneyResolverService.JourneySource.NONE : source;
    }

    /** Lo que le habria tocado si el dia no fuera feriado. Solo para mostrar. */
    public ScheduledJourney suppressedOf(Long day) {
        return suppressed.get(day);
    }

    public WorkGroupMembership membershipOf(Long day) {
        return memberships.get(day);
    }

    /** Las jornadas del periodo, en el orden en que se resolvieron. */
    public List<ScheduledJourney> getJourneys() {
        List<ScheduledJourney> ordered = new ArrayList<ScheduledJourney>();
        List<Long> days = new ArrayList<Long>(journeys.keySet());
        java.util.Collections.sort(days);
        for (Long day : days) {
            ordered.add(journeys.get(day));
        }
        return ordered;
    }
}
