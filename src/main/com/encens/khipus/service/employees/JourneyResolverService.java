package com.encens.khipus.service.employees;

import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Que jornada le tocaba a una persona un dia.
 * <p/>
 * Es la unica pieza que conoce las cuatro capas y el unico punto por el que el motor de
 * asistencia pregunta por horarios. El motor recibe una lista de jornadas y no se entera de si
 * salieron de un cronograma, de un horario fijo o de una excepcion; por eso una empresa sin
 * produccion puede usar solo el horario fijo y el calculo es el mismo.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface JourneyResolverService {

    /**
     * @return la jornada del contrato ese dia, o null si ese dia no habia jornada -descanso,
     *         feriado, semana sin publicar o persona sin horario configurado-
     */
    ScheduledJourney resolve(Contract contract, Date day);

    /**
     * Las jornadas de un periodo, dia por dia. Es lo que consume la generacion de planilla: se
     * resuelve el mes entero de una vez en lugar de preguntar dia por dia.
     *
     * @return solo los dias que tienen jornada; los dias sin jornada no aparecen
     */
    List<ScheduledJourney> resolve(Contract contract, Date from, Date to);

    /**
     * De donde salio la jornada de ese dia. Se usa en las pantallas de control para poder
     * explicarle a alguien por que se le evaluo contra ese horario.
     */
    JourneySource sourceOf(Contract contract, Date day);

    /**
     * La jornada que el horario fijo habria dado si ese dia no fuera feriado, o null si no
     * es feriado. Es solo para mostrar: sirve para poder decir "le tocaba 09:00 a 17:30,
     * pero era feriado" en lugar de un "sin jornada" que no explica nada.
     */
    ScheduledJourney suppressedByHoliday(Contract contract, Date day);

    /** De donde salio -o por que no hay- la jornada de ese dia. */
    enum JourneySource {
        EXCEPTION("JourneySource.exception"),
        GROUP_SCHEDULE("JourneySource.groupSchedule"),
        CONTRACT_SCHEDULE("JourneySource.contractSchedule"),
        /**
         * Le tocaba por su horario tipo, pero es feriado y no se trabaja.
         * <p/>
         * Es un origen y no un simple "sin jornada" porque la diferencia importa: quien marca
         * un feriado trabajo de verdad, y eso se paga distinto o se acumula.
         */
        HOLIDAY("JourneySource.holiday"),
        NONE("JourneySource.none");

        private String resourceKey;

        JourneySource(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        public String getResourceKey() {
            return resourceKey;
        }
    }
}
