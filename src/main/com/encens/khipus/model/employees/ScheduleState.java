package com.encens.khipus.model.employees;

/**
 * Estado de una semana del cronograma.
 * <p/>
 * Existe porque el cronograma se carga por semana y a mitad de mes esta incompleto. Sin estado,
 * los dias que todavia nadie planifico se leerian igual que los dias de descanso: en los dos
 * casos no hay jornada. Una semana olvidada pasaria como "nadie tenia que trabajar" y nadie se
 * enteraria.
 * <p/>
 * <b>El motor solo lee las semanas publicadas.</b> Mientras esta en borrador, el planificador
 * trabaja tranquilo y lo que deje a medias no afecta a nada.
 *
 * @author
 * @version 6.1.0
 */
public enum ScheduleState {

    /** Se esta planificando. El motor no la mira. */
    DRAFT("ScheduleState.draft"),

    /** Terminada y disponible para el control de asistencia. */
    PUBLISHED("ScheduleState.published");

    private String resourceKey;

    ScheduleState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
