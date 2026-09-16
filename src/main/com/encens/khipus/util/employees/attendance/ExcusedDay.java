package com.encens.khipus.util.employees.attendance;

import com.encens.khipus.model.employees.SpecialDateReason;

import java.io.Serializable;

/**
 * Un dia que la persona no tiene que justificar con marcas: permiso, vacacion, maternidad,
 * compensatorio o descanso, cargado en `fechaespecial` como dia completo.
 * <p/>
 * El motor solo conocia los feriados, y eso era un agujero con fecha de vencimiento: el dia que
 * RRHH cargara la primera vacacion, el motor le habria puesto <b>falta a cada dia de vacaciones</b>.
 * No se veia en los datos porque en la base habia una sola fecha especial -un feriado-, asi que
 * ninguna prueba lo habria encontrado.
 * <p/>
 * <b>El feriado NO es un dia excusado.</b> Tiene su propio efecto y es distinto: suprime la
 * jornada del horario fijo, pero no la del cronograma de grupo, porque produccion trabaja los
 * feriados. Si ademas excusara, a quien tiene el feriado planificado y no vino no se le contaria
 * la falta.
 *
 * @author
 * @version 6.1.0
 */
public final class ExcusedDay implements Serializable {

    private final SpecialDateReason reason;
    private final String title;
    private final boolean paid;

    public ExcusedDay(SpecialDateReason reason, String title, boolean paid) {
        this.reason = reason;
        this.title = title;
        this.paid = paid;
    }

    /**
     * Con goce de haber: el dia se paga y no se pierde nada.
     * <p/>
     * Sin goce no es una falta: es una licencia aprobada. El dia no se paga, pero <b>no se
     * sanciona al doble</b>, porque la persona no falto: pidio permiso y se lo dieron.
     */
    public boolean isPaid() {
        return paid;
    }

    public SpecialDateReason getReason() {
        return reason;
    }

    public String getTitle() {
        return title;
    }

    /** La clave i18n del motivo, para mostrarlo. Null cuando la fila no tiene motivo cargado. */
    public String getResourceKey() {
        return null == reason ? null : reason.getResourceKey();
    }
}
