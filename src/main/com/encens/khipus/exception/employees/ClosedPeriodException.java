package com.encens.khipus.exception.employees;

import java.util.Date;

/**
 * Se intento tocar un horario de un periodo ya cerrado por una planilla oficial.
 * <p/>
 * Cambiar el horario de un dia que ya se pago cambiaria en silencio como se calcularon faltas y
 * atrasos que alguien ya reviso y firmo. Si de verdad hay que corregirlo, el camino es revertir
 * la planilla, no editar el cronograma por debajo.
 *
 * @author
 * @version 6.1.0
 */
public class ClosedPeriodException extends Exception {

    private Date closedUntil;

    public ClosedPeriodException(Date closedUntil) {
        super("El periodo esta cerrado hasta " + closedUntil);
        this.closedUntil = closedUntil;
    }

    public Date getClosedUntil() {
        return closedUntil;
    }
}
