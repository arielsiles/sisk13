package com.encens.khipus.action.employees;

import com.encens.khipus.action.employees.dto.WeekdayCell;
import com.encens.khipus.exception.employees.ClosedPeriodException;
import com.encens.khipus.model.employees.ContractWorkShift;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.employees.ContractScheduleService;
import com.encens.khipus.service.employees.ContractService;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * El horario fijo de una persona: la semana tipo de quien no rota.
 * <p/>
 * Se pinta igual que el cronograma del grupo -paleta arriba, clic en el dia- para que quien usa
 * las dos pantallas no tenga que aprender dos formas de hacer lo mismo. La diferencia es que acá
 * las columnas son dias de la semana y no fechas: el horario no vence, rige hasta que se cambie.
 *
 * @author
 * @version 6.1.0
 */
@Name("contractScheduleAction")
@Scope(ScopeType.SESSION)
public class ContractScheduleAction implements Serializable {

    /* Lunes primero, como en el cronograma; el domingo cierra la semana. */
    private static final int[] WEEK = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

    @Logger
    private Log log;

    @In
    private ContractScheduleService contractScheduleService;

    @In
    private ContractService contractService;

    @In
    private WorkGroupService workGroupService;

    @In
    private WorkShiftService workShiftService;

    @In(create = true)
    private FacesMessages facesMessages;

    private Employee employee;
    private Contract contract;

    /* Desde cuando rige el horario que se esta viendo. Es un DATO, no un campo editable:
       mostrarlo evita la lectura equivocada de que el horario empieza hoy. */
    private Date currentValidFrom;

    /* Desde cuando rige lo que se cambie ahora. Arranca en la fecha del horario cargado,
       no en hoy: corregir lo que uno acaba de cargar no deberia partir la vigencia en dos. */
    private Date validFrom = new Date();

    private WorkShift brush;
    private boolean brushRest = false;

    private List<WeekdayCell> cells;
    private List<WorkShift> shifts;

    /* Pintado por rango: lo pone en "1" el propio clic cuando venia con Shift. */
    private String rangeFlag = "0";
    private Integer lastPaintedDayOfWeek;

    // ----------------------------------------------------------------- carga

    @Restrict("#{s:hasPermission('CONTRACTSCHEDULE','VIEW')}")
    public void init() {
        cells = null;
    }

    public List<WeekdayCell> getCells() {
        if (null == cells) {
            load();
        }
        return cells;
    }

    public List<WorkShift> getShifts() {
        if (null == shifts) {
            shifts = workShiftService.findActiveList();
        }
        return shifts;
    }

    private void load() {
        cells = new ArrayList<WeekdayCell>();
        List<ContractWorkShift> current = null == contract
                ? new ArrayList<ContractWorkShift>() : contractScheduleService.findCurrent(contract);
        currentValidFrom = null;
        for (ContractWorkShift shift : current) {
            if (null == currentValidFrom || shift.getStartDate().before(currentValidFrom)) {
                currentValidFrom = shift.getStartDate();
            }
        }
        Calendar monday = mondayOfThisWeek();
        for (int i = 0; i < WEEK.length; i++) {
            Calendar sample = (Calendar) monday.clone();
            sample.add(Calendar.DAY_OF_MONTH, i);
            cells.add(new WeekdayCell(WEEK[i], sample.getTime(), shiftOf(current, WEEK[i])));
        }
        if (null == brush && !brushRest && !getShifts().isEmpty()) {
            brush = getShifts().get(0);
        }
    }

    private static ContractWorkShift shiftOf(List<ContractWorkShift> current, int dayOfWeek) {
        for (ContractWorkShift shift : current) {
            if (shift.getDayOfWeek().equals(dayOfWeek)) {
                return shift;
            }
        }
        return null;
    }

    // -------------------------------------------------------------- persona

    /**
     * Al elegir la persona se busca su contrato vigente. Sin contrato no hay horario que
     * cargar: quien no tiene contrato no entra en planillas y no se le controla asistencia.
     */
    public void assignEmployee(Employee selected) {
        this.employee = selected;
        this.contract = null;
        this.cells = null;
        if (null == selected) {
            return;
        }
        List<Contract> contracts = contractService.getContractsByEmployeeInDateRange(
                selected, validFrom, validFrom);
        if (contracts.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractSchedule.error.withoutContract", selected.getFullName());
            return;
        }
        this.contract = contracts.get(0);
        /* Se carga la grilla antes de decidir la fecha: si la persona ya tiene horario, los
           cambios arrancan rigiendo desde esa misma fecha y no desde hoy. */
        load();
        if (null != currentValidFrom) {
            this.validFrom = currentValidFrom;
        }
        if (null != workGroupService.findMembership(contract, validFrom)) {
            /* Pertenecer a un grupo gana sobre el horario fijo. Cargarlo igual no rompe nada,
               pero no se va a usar, y es mejor decirlo ahora que buscarlo despues. */
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "ContractSchedule.warn.belongsToGroup", selected.getFullName());
        }
    }

    public void clearEmployee() {
        this.employee = null;
        this.contract = null;
        this.cells = null;
    }

    public boolean isEmployeeSelected() {
        return null != contract;
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

    // --------------------------------------------------------------- pintar

    @Restrict("#{s:hasPermission('CONTRACTSCHEDULE','UPDATE')}")
    public void paint(WeekdayCell cell) {
        if (null == contract) {
            return;
        }
        WorkShift shift = brushRest ? null : brush;
        WorkShift current = cell.getWorkShift();
        boolean range = "1".equals(rangeFlag) && null != lastPaintedDayOfWeek;
        if (!range && null != shift && null != current && shift.getId().equals(current.getId())) {
            /* El borrado por repeticion es del clic simple: quien marca un rango quiere
               pintarlo, no vaciarlo. */
            shift = null;
        }
        try {
            for (int dayOfWeek : rangeOf(cell.getDayOfWeek(), range)) {
                contractScheduleService.setShift(contract, dayOfWeek, shift, validFrom);
            }
            lastPaintedDayOfWeek = cell.getDayOfWeek();
            cells = null;
        } catch (ClosedPeriodException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Schedule.error.closedPeriod", e.getClosedUntil());
        } catch (Exception e) {
            log.error("No se pudo guardar el horario fijo del contrato", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractSchedule.error.saveFailed");
        }
    }

    /** Cierra el horario fijo: a partir de la fecha, a esta persona no se le evalua asistencia. */
    @Restrict("#{s:hasPermission('CONTRACTSCHEDULE','UPDATE')}")
    public void close() {
        if (null == contract) {
            return;
        }
        try {
            contractScheduleService.close(contract, validFrom);
            cells = null;
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "ContractSchedule.info.closed");
        } catch (ClosedPeriodException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Schedule.error.closedPeriod", e.getClosedUntil());
        } catch (Exception e) {
            log.error("No se pudo cerrar el horario fijo del contrato", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractSchedule.error.saveFailed");
        }
    }

    public Double getWeekHours() {
        int minutes = 0;
        for (WeekdayCell cell : getCells()) {
            minutes += cell.getMinutes();
        }
        return minutes / 60d;
    }

    /**
     * Los dias a pintar. Con Shift, desde el ultimo tocado hasta este; sin Shift, solo este.
     * <p/>
     * El rango se toma por POSICION en la semana -lunes a domingo- y no por el numero de dia de
     * Calendar, donde el domingo es 1 y quedaria antes que el lunes.
     */
    private List<Integer> rangeOf(int dayOfWeek, boolean range) {
        List<Integer> result = new ArrayList<Integer>();
        int to = indexOf(dayOfWeek);
        int from = range ? indexOf(lastPaintedDayOfWeek) : to;
        if (from > to) {
            int swap = from;
            from = to;
            to = swap;
        }
        for (int i = from; i <= to; i++) {
            result.add(WEEK[i]);
        }
        return result;
    }

    private static int indexOf(int dayOfWeek) {
        for (int i = 0; i < WEEK.length; i++) {
            if (WEEK[i] == dayOfWeek) {
                return i;
            }
        }
        return 0;
    }

    public String getRangeFlag() {
        return rangeFlag;
    }

    public void setRangeFlag(String rangeFlag) {
        this.rangeFlag = rangeFlag;
    }

    private static Calendar mondayOfThisWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int shift = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        if (shift < 0) {
            shift += 7;
        }
        calendar.add(Calendar.DAY_OF_MONTH, -shift);
        return calendar;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Contract getContract() {
        return contract;
    }

    public Date getCurrentValidFrom() {
        getCells();
        return currentValidFrom;
    }

    public boolean isScheduleLoaded() {
        return null != getCurrentValidFrom();
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }
}
