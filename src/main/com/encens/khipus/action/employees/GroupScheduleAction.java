package com.encens.khipus.action.employees;

import com.encens.khipus.action.employees.dto.ScheduleCell;
import com.encens.khipus.action.employees.dto.ScheduleRow;
import com.encens.khipus.exception.employees.ClosedPeriodException;
import com.encens.khipus.model.employees.GroupScheduleDay;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.service.employees.GroupScheduleService;
import com.encens.khipus.service.employees.WorkGroupService;
import com.encens.khipus.service.employees.WorkShiftService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * La planificacion del cronograma: una semana, un grupo por fila, un dia por columna.
 * <p/>
 * La forma de trabajar es la de una planilla y la de cualquier sistema de turnos moderno: se
 * elige un turno en la paleta y se hace clic en las celdas. No hay boton de guardar porque cada
 * clic ya guarda; mientras la semana este en borrador eso no le llega al motor de asistencia, y
 * publicar es el paso deliberado que la habilita.
 * <p/>
 * Se planifica por semana y no por mes porque asi lo carga RRHH -"se va cargando cada semana"- y
 * porque una grilla de 31 columnas no se puede leer en pantalla.
 *
 * @author
 * @version 6.1.0
 */
@Name("groupScheduleAction")
@Scope(ScopeType.SESSION)
public class GroupScheduleAction implements Serializable {

    @Logger
    private Log log;

    @In
    private GroupScheduleService groupScheduleService;

    @In
    private WorkGroupService workGroupService;

    @In
    private WorkShiftService workShiftService;

    @In(create = true)
    private FacesMessages facesMessages;

    /* Lunes de la semana en pantalla. */
    private Date weekStart;

    /* El turno que se va a pintar. Nulo con brushRest en true significa "sin jornada". */
    private WorkShift brush;
    private boolean brushRest = false;

    private List<ScheduleRow> rows;
    private List<Date> days;

    /* Semanas del mes en pantalla que tienen dias pintados pero SIN publicar. Se avisa arriba:
       olvidarse de publicar una semana es silencioso -esos dias no llegan al motor y se pagan
       como faltas- y ya paso en la practica. */
    private Set<Long> draftWeeks = new HashSet<Long>();
    private List<WorkShift> shifts;

    /* Pintado por rango: lo pone en "1" el propio clic cuando venia con Shift. */
    private String rangeFlag = "0";
    private Date lastPaintedDay;
    private Long lastPaintedGroupId;

    /* Copiar de un grupo a otro. */
    private Long sourceGroupId;
    private Long targetGroupId;
    private boolean invertShifts = true;

    // ---------------------------------------------------------------- carga

    /**
     * Se dispara al entrar a la pantalla y en cada peticion (page action). Tira la grilla
     * cacheada para que se relea de la base: el componente vive en la sesion y sin esto se
     * quedaria mostrando lo que habia la primera vez.
     */
    @Restrict("#{s:hasPermission('GROUPSCHEDULE','VIEW')}")
    public void init() {
        invalidate();
    }

    /**
     * La grilla se arma una vez por pedido y queda cacheada: la vista la recorre muchas veces
     * -filas, celdas, totales- y sin cache serian decenas de consultas por render.
     */
    public List<ScheduleRow> getRows() {
        if (null == rows) {
            load();
        }
        return rows;
    }

    public List<Date> getDays() {
        if (null == days) {
            load();
        }
        return days;
    }

    public List<WorkShift> getShifts() {
        if (null == shifts) {
            shifts = workShiftService.findActiveList();
        }
        return shifts;
    }

    private void load() {
        days = weekDays();
        rows = new ArrayList<ScheduleRow>();
        Date from = days.get(0);
        Date to = days.get(days.size() - 1);
        Date monthFrom = firstDayOfMonth(from);
        Date monthTo = lastDayOfMonth(from);
        draftWeeks = new HashSet<Long>();
        for (WorkGroup workGroup : workGroupService.findActiveList()) {
            List<GroupScheduleDay> scheduleDays = groupScheduleService.findDays(workGroup, from, to);
            ScheduleRow row = new ScheduleRow(workGroup, days, scheduleDays);
            List<GroupScheduleDay> monthDays = groupScheduleService.findDays(workGroup, monthFrom, monthTo);
            row.setMonthMinutes(minutesOf(monthDays));
            collectDraftWeeks(monthDays);
            rows.add(row);
        }
        if (null == brush && !brushRest && !getShifts().isEmpty()) {
            /* Arranca con un turno elegido: asi el primer clic ya pinta y no hay un estado
               inicial en el que hacer clic no hace nada. */
            brush = getShifts().get(0);
        }
    }

    /** Un dia con turno que sigue en borrador marca a toda su semana como pendiente. */
    private void collectDraftWeeks(List<GroupScheduleDay> monthDays) {
        for (GroupScheduleDay scheduleDay : monthDays) {
            if (null != scheduleDay.getWorkShift() && !scheduleDay.isPublished()) {
                draftWeeks.add(mondayOf(scheduleDay.getDay()).getTime());
            }
        }
    }

    public int getDraftWeekCount() {
        getRows();
        return draftWeeks.size();
    }

    public boolean isHasDraftWeeks() {
        return getDraftWeekCount() > 0;
    }

    private void invalidate() {
        rows = null;
        days = null;
    }

    // ------------------------------------------------------------ navegacion

    public void previousWeek() {
        moveWeek(-1);
    }

    public void nextWeek() {
        moveWeek(1);
    }

    public void currentWeek() {
        weekStart = mondayOf(new Date());
        invalidate();
    }

    private void moveWeek(int weeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getWeekStart());
        calendar.add(Calendar.WEEK_OF_YEAR, weeks);
        weekStart = calendar.getTime();
        invalidate();
    }

    public Date getWeekStart() {
        if (null == weekStart) {
            weekStart = mondayOf(new Date());
        }
        return weekStart;
    }

    /** Elegir una fecha cualquiera lleva a la semana que la contiene. */
    public void setWeekStart(Date date) {
        this.weekStart = null == date ? null : mondayOf(date);
        invalidate();
    }

    public Date getWeekEnd() {
        List<Date> weekDays = getDays();
        return weekDays.get(weekDays.size() - 1);
    }

    // --------------------------------------------------------------- paleta

    public void selectBrush(WorkShift workShift) {
        this.brush = workShift;
        this.brushRest = false;
    }

    public void selectRestBrush() {
        this.brushRest = true;
    }

    public boolean isBrushSelected(WorkShift workShift) {
        return !brushRest && null != brush && brush.getId().equals(workShift.getId());
    }

    public boolean isRestBrushSelected() {
        return brushRest;
    }

    private WorkShift brushShift() {
        return brushRest ? null : brush;
    }

    // --------------------------------------------------------------- pintar

    /**
     * Pinta una celda con el turno de la paleta. Volver a hacer clic sobre una celda que ya
     * tiene ese mismo turno la borra: es el gesto natural para corregir sin cambiar de pincel.
     */
    @Restrict("#{s:hasPermission('GROUPSCHEDULE','UPDATE')}")
    public void paint(ScheduleCell cell) {
        WorkShift shift = brushShift();
        WorkShift current = cell.getWorkShift();
        if (null != shift && null != current && shift.getId().equals(current.getId())) {
            shift = null;
        }
        boolean range = "1".equals(rangeFlag)
                && null != lastPaintedDay
                && cell.getWorkGroup().getId().equals(lastPaintedGroupId);
        if (range) {
            /* Con Shift se pinta desde la ultima celda tocada hasta esta. En un rango no se
               aplica el borrado por repeticion: quien marca un rango quiere pintarlo, no
               vaciarlo. */
            shift = brushShift();
        }
        try {
            if (range) {
                Date from = lastPaintedDay.after(cell.getDay()) ? cell.getDay() : lastPaintedDay;
                Date to = lastPaintedDay.after(cell.getDay()) ? lastPaintedDay : cell.getDay();
                groupScheduleService.setShiftRange(cell.getWorkGroup(), from, to, shift);
            } else {
                groupScheduleService.setShift(cell.getWorkGroup(), cell.getDay(), shift);
            }
            lastPaintedDay = cell.getDay();
            lastPaintedGroupId = cell.getWorkGroup().getId();
            invalidate();
        } catch (ClosedPeriodException e) {
            addClosedPeriodMessage(e);
        } catch (Exception e) {
            log.error("No se pudo pintar el dia del cronograma", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.paintFailed");
        }
    }

    /** Pinta la semana entera de un grupo. Es el atajo para los grupos de horario parejo. */
    @Restrict("#{s:hasPermission('GROUPSCHEDULE','UPDATE')}")
    public void paintRow(ScheduleRow row) {
        try {
            groupScheduleService.setShiftRange(row.getWorkGroup(), getWeekStart(), getWeekEnd(),
                    brushShift());
            invalidate();
        } catch (ClosedPeriodException e) {
            addClosedPeriodMessage(e);
        } catch (Exception e) {
            log.error("No se pudo pintar la semana del grupo", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.paintFailed");
        }
    }

    // ---------------------------------------------------------------- copiar

    /**
     * Trae la semana anterior tal cual, para todos los grupos. Es el atajo que mas se usa: la
     * rotacion se repite cada dos semanas, asi que la semana 3 es igual a la 1.
     * <p/>
     * Copia siempre en borrador, aunque el origen estuviera publicado: lo copiado hay que
     * mirarlo antes de que cuente.
     */
    @Restrict("#{s:hasPermission('GROUPSCHEDULE','UPDATE')}")
    public void copyPreviousWeek() {
        Calendar from = Calendar.getInstance();
        from.setTime(getWeekStart());
        from.add(Calendar.WEEK_OF_YEAR, -1);
        Calendar to = Calendar.getInstance();
        to.setTime(getWeekEnd());
        to.add(Calendar.WEEK_OF_YEAR, -1);
        try {
            for (ScheduleRow row : getRows()) {
                groupScheduleService.copyRange(row.getWorkGroup(), from.getTime(), to.getTime(),
                        row.getWorkGroup(), getWeekStart(), false);
            }
            invalidate();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GroupSchedule.info.copiedPreviousWeek");
        } catch (ClosedPeriodException e) {
            addClosedPeriodMessage(e);
        } catch (Exception e) {
            log.error("No se pudo copiar la semana anterior", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.copyFailed");
        }
    }

    /**
     * Copia la semana de un grupo a otro, opcionalmente cambiando dia por noche. Es el segundo
     * atajo real: el Grupo 2 suele ser el Grupo 1 con los turnos invertidos.
     */
    @Restrict("#{s:hasPermission('GROUPSCHEDULE','UPDATE')}")
    public void copyGroup() {
        if (null == sourceGroupId || null == targetGroupId) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.copyGroupRequired");
            return;
        }
        if (sourceGroupId.equals(targetGroupId)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.copySameGroup");
            return;
        }
        try {
            WorkGroup source = workGroupService.findById(WorkGroup.class, sourceGroupId);
            WorkGroup target = workGroupService.findById(WorkGroup.class, targetGroupId);
            groupScheduleService.copyRange(source, getWeekStart(), getWeekEnd(),
                    target, getWeekStart(), invertShifts);
            invalidate();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GroupSchedule.info.copiedGroup", source.getName(), target.getName());
        } catch (ClosedPeriodException e) {
            addClosedPeriodMessage(e);
        } catch (Exception e) {
            log.error("No se pudo copiar el cronograma de un grupo a otro", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.copyFailed");
        }
    }

    public List<SelectItem> getGroupSelectItems() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        for (ScheduleRow row : getRows()) {
            items.add(new SelectItem(row.getWorkGroup().getId(), row.getWorkGroup().getName()));
        }
        return items;
    }

    // ------------------------------------------------------------- publicar

    /**
     * Publica la semana de todos los grupos en pantalla. Si alguna fila quedo sin tocar avisa,
     * pero no lo impide: "esta semana nadie trabaja" tambien es una decision valida y hay que
     * poder tomarla a proposito.
     */
    @Restrict("#{s:hasPermission('GROUPSCHEDULE','UPDATE')}")
    public void publishWeek() {
        int untouched = 0;
        try {
            for (ScheduleRow row : getRows()) {
                if (row.isUntouched()) {
                    untouched++;
                }
                groupScheduleService.publish(row.getWorkGroup(), getWeekStart(), getWeekEnd());
            }
            invalidate();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GroupSchedule.info.published");
            if (untouched > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "GroupSchedule.warn.untouchedGroups", untouched);
            }
        } catch (ClosedPeriodException e) {
            addClosedPeriodMessage(e);
        } catch (Exception e) {
            log.error("No se pudo publicar la semana del cronograma", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.publishFailed");
        }
    }

    /** Vuelve la semana a borrador para corregirla. El motor deja de verla en el acto. */
    @Restrict("#{s:hasPermission('GROUPSCHEDULE','UPDATE')}")
    public void unpublishWeek() {
        try {
            for (ScheduleRow row : getRows()) {
                groupScheduleService.unpublish(row.getWorkGroup(), getWeekStart(), getWeekEnd());
            }
            invalidate();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GroupSchedule.info.unpublished");
        } catch (ClosedPeriodException e) {
            addClosedPeriodMessage(e);
        } catch (Exception e) {
            log.error("No se pudo volver a borrador la semana del cronograma", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GroupSchedule.error.publishFailed");
        }
    }

    /** La semana esta publicada cuando lo estan todas las filas. */
    public boolean isWeekPublished() {
        List<ScheduleRow> scheduleRows = getRows();
        if (scheduleRows.isEmpty()) {
            return false;
        }
        for (ScheduleRow row : scheduleRows) {
            if (!row.isPublished()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmptyOfGroups() {
        return getRows().isEmpty();
    }

    // ---------------------------------------------------------------- fechas

    private List<Date> weekDays() {
        List<Date> weekDays = new ArrayList<Date>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getWeekStart());
        for (int i = 0; i < 7; i++) {
            weekDays.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return weekDays;
    }

    /**
     * El lunes de la semana de una fecha. La semana arranca en lunes y no en domingo porque asi
     * la leen las planillas de produccion con las que se trabaja.
     */
    private static Date mondayOf(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int shift = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        if (shift < 0) {
            shift += 7; /* domingo */
        }
        calendar.add(Calendar.DAY_OF_MONTH, -shift);
        return calendar.getTime();
    }

    /**
     * El periodo esta cerrado por una planilla oficial. No es un error del usuario: es el
     * sistema protegiendo un calculo ya pagado, y el mensaje dice hasta cuando.
     */
    private void addClosedPeriodMessage(ClosedPeriodException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Schedule.error.closedPeriod", e.getClosedUntil());
    }

    private static int minutesOf(List<GroupScheduleDay> scheduleDays) {
        int minutes = 0;
        for (GroupScheduleDay scheduleDay : scheduleDays) {
            if (null != scheduleDay.getWorkShift()) {
                minutes += scheduleDay.getWorkShift().getDurationMinutes();
            }
        }
        return minutes;
    }

    private static Date firstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    private static Date lastDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public WorkShift getBrush() {
        return brush;
    }

    public String getRangeFlag() {
        return rangeFlag;
    }

    public void setRangeFlag(String rangeFlag) {
        this.rangeFlag = rangeFlag;
    }

    public Long getSourceGroupId() {
        return sourceGroupId;
    }

    public void setSourceGroupId(Long sourceGroupId) {
        this.sourceGroupId = sourceGroupId;
    }

    public Long getTargetGroupId() {
        return targetGroupId;
    }

    public void setTargetGroupId(Long targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public boolean isInvertShifts() {
        return invertShifts;
    }

    public void setInvertShifts(boolean invertShifts) {
        this.invertShifts = invertShifts;
    }
}
