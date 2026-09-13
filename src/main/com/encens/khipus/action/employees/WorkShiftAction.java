package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.service.employees.ContractScheduleService;
import com.encens.khipus.service.employees.GroupScheduleService;
import com.encens.khipus.service.employees.ScheduleExceptionService;
import com.encens.khipus.service.employees.ScheduleLockService;
import com.encens.khipus.service.employees.WorkShiftService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * CRUD del catalogo de turnos.
 *
 * @author
 * @version 6.1.0
 */
@Name("workShiftAction")
@Scope(ScopeType.CONVERSATION)
public class WorkShiftAction extends GenericAction<WorkShift> {

    /** Menos que esto deja afuera a quien marca apenas antes de su hora. */
    private static final int MIN_BEFORE_MARGIN_MINUTES = 30;

    @In
    private WorkShiftService workShiftService;

    @In
    private GroupScheduleService groupScheduleService;

    @In
    private ContractScheduleService contractScheduleService;

    @In
    private ScheduleExceptionService scheduleExceptionService;

    @In
    private ScheduleLockService scheduleLockService;

    @Factory(value = "workShift")
    @Restrict("#{s:hasPermission('WORKSHIFT','VIEW')}")
    public WorkShift initWorkShift() {
        return getInstance();
    }

    /**
     * La ventana del turno, en texto, para mostrarla debajo de los dos margenes.
     * <p/>
     * Es lo que hace que dos numeros abstractos se entiendan sin explicacion: quien crea un
     * turno corto ve al instante si la ventana le quedo desproporcionada, en lugar de tener que
     * imaginarsela. La persona es la parte adaptativa, y es mas confiable que una formula.
     */
    public String getWindowDescription() {
        WorkShift shift = getInstance();
        if (null == shift || null == shift.getStartHour() || null == shift.getEndHour()) {
            return "";
        }
        int before = null == shift.getBeforeMarginMinutes() ? 0 : shift.getBeforeMarginMinutes();
        int after = null == shift.getAfterMarginMinutes() ? 0 : shift.getAfterMarginMinutes();
        return hourAt(shift.getStartHour(), -before) + "  -  " + hourAt(shift.getEndHour(), after);
    }

    @SuppressWarnings({"deprecation"})
    private static String hourAt(Date hour, int offsetMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour.getHours());
        calendar.set(Calendar.MINUTE, hour.getMinutes());
        calendar.add(Calendar.MINUTE, offsetMinutes);
        return new SimpleDateFormat("HH:mm").format(calendar.getTime());
    }

    /**
     * Precarga de los margenes al crear un turno, segun cuanto dura.
     * <p/>
     * La ventana mide `duracion + antes + despues`, asi que con 2 y 4 horas queda por debajo del
     * doble del turno solo si el turno dura 6 horas o mas. Para los cortos -medios turnos de 3, 4
     * o 5 horas- la mitad. No es una formula escondida: son valores de arranque, visibles y
     * editables, para que ninguno de los turnos habituales necesite que alguien intervenga.
     */
    public void preloadMargins() {
        WorkShift shift = getInstance();
        if (null == shift || isManaged()) {
            return;
        }
        boolean shortShift = shift.getDurationMinutes() < WorkShift.SHORT_SHIFT_MINUTES;
        shift.setBeforeMarginMinutes(shortShift
                ? WorkShift.SHORT_BEFORE_MINUTES : WorkShift.DEFAULT_BEFORE_MINUTES);
        shift.setAfterMarginMinutes(shortShift
                ? WorkShift.SHORT_AFTER_MINUTES : WorkShift.DEFAULT_AFTER_MINUTES);
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WORKSHIFT','VIEW')}")
    public String select(WorkShift instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(WorkShift.class, instance.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','CREATE')}")
    public String create() {
        return validate() ? super.create() : Outcome.REDISPLAY;
    }

    /**
     * Un turno que ya se uso en un periodo pagado no se edita.
     * <p/>
     * Cambiarle las horas cambiaria hacia atras el calculo de faltas y atrasos de una planilla
     * que alguien ya reviso y firmo, y sin dejar rastro. Lo correcto es desactivarlo y crear
     * uno nuevo con las horas nuevas: asi la historia queda como fue.
     */
    public boolean isLocked() {
        return isManaged() && scheduleLockService.isWorkShiftLocked(getInstance());
    }

    /** Desactivar si se puede aunque este bloqueado: es como se retira un turno viejo. */
    @Restrict("#{s:hasPermission('WORKSHIFT','UPDATE')}")
    public String deactivate() {
        getInstance().setActive(Boolean.FALSE);
        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','UPDATE')}")
    public String update() {
        if (isLocked()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkShift.error.locked", getInstance().getName());
            return Outcome.REDISPLAY;
        }
        return validate() ? super.update() : Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','DELETE')}")
    public String delete() {
        /* Un turno usado en el cronograma no se borra: al hacerlo se perderia con que jornada
           se evaluaron esos dias. Se desactiva, y deja de ofrecerse en la paleta. */
        Long used = groupScheduleService.countByWorkShift(getInstance())
                + contractScheduleService.countByWorkShift(getInstance())
                + scheduleExceptionService.countByWorkShift(getInstance());
        if (used > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkShift.error.inUse", getInstance().getName(), used);
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    /**
     * Avisa -no bloquea- cuando la ventana quedo mal. Son avisos porque detras de cada uno puede
     * haber un caso legitimo, y quien configura el turno es quien sabe como se trabaja en el.
     */
    private void warnOnWindow(WorkShift shift) {
        int before = null == shift.getBeforeMarginMinutes() ? 0 : shift.getBeforeMarginMinutes();

        /* 1. La ventana toca a la del mismo turno del dia siguiente: entre las dos ya no se
              puede decir de que dia es una marca de la madrugada. */
        if (shift.getWindowMinutes() >= 24 * 60) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "WorkShift.warn.windowOverlapsNextDay");
        }

        /* 2. El caso traicionero, porque parece prolijo: con margen cero, quien marque un minuto
              antes queda afuera y su jornada se queda sin entrada. */
        if (before < MIN_BEFORE_MARGIN_MINUTES) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "WorkShift.warn.beforeMarginTooSmall");
        }

        /* 3. Ventana desproporcionada para lo que dura el turno. Dispara exactamente en los
              turnos cortos con la precarga larga, que es donde tiene que disparar. */
        if (shift.getDurationMinutes() > 0
                && shift.getWindowMinutes() > 2 * shift.getDurationMinutes()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "WorkShift.warn.windowTooWide");
        }
    }

    private boolean validate() {
        WorkShift instance = getInstance();
        Long duplicated = workShiftService.countByName(instance.getName(),
                isManaged() ? instance.getId() : null);
        if (null != duplicated && duplicated > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.message.duplicated", instance.getName());
            return false;
        }
        /* Un turno de cero minutos no es un turno. Como el cruce de medianoche se deduce de que
           el fin no sea posterior al inicio, dos horas iguales darian 24 horas en vez de cero,
           que es peor que rechazarlo. */
        if (instance.getDurationMinutes() == 24 * 60) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkShift.error.sameHours");
            return false;
        }
        warnOnWindow(instance);
        return true;
    }

    @Override
    protected GenericService getService() {
        return workShiftService;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
