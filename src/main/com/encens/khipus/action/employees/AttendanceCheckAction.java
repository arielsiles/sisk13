package com.encens.khipus.action.employees;

import com.encens.khipus.action.employees.dto.AttendanceDay;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.employees.AttendanceCheckService;
import com.encens.khipus.service.employees.ContractService;
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
 * Verificacion del control de asistencia: que le tocaba a una persona cada dia, que marco y
 * como cierra.
 * <p/>
 * No calcula ni guarda nada. Corre el mismo motor que va a usar la planilla, de modo que lo que
 * se ve aca es lo que se va a pagar; si algo no cuadra, se ve antes de generar y no despues de
 * un reclamo.
 *
 * @author
 * @version 6.1.0
 */
@Name("attendanceCheckAction")
@Scope(ScopeType.SESSION)
public class AttendanceCheckAction implements Serializable {

    @Logger
    private Log log;

    @In
    private AttendanceCheckService attendanceCheckService;

    @In
    private ContractService contractService;

    @In(create = true)
    private FacesMessages facesMessages;

    private Employee employee;
    private Contract contract;
    private Date startDate;
    private Date endDate;

    private List<AttendanceDay> days = new ArrayList<AttendanceDay>();
    private boolean checked = false;

    public AttendanceCheckAction() {
        /* Arranca en el mes anterior completo, que es el que normalmente se revisa: la planilla
           de un mes se genera y se controla al mes siguiente. */
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = calendar.getTime();
    }

    @Restrict("#{s:hasPermission('ATTENDANCECHECK','VIEW')}")
    public void check() {
        days = new ArrayList<AttendanceDay>();
        checked = false;
        if (null == employee) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "AttendanceCheck.error.employeeRequired");
            return;
        }
        if (null == startDate || null == endDate || endDate.before(startDate)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "AttendanceCheck.error.badRange");
            return;
        }
        List<Contract> contracts = contractService.getContractsByEmployeeInDateRange(
                employee, startDate, endDate);
        if (contracts.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "AttendanceCheck.error.withoutContract", employee.getFullName());
            return;
        }
        contract = contracts.get(0);
        if (null == employee.getMarkCode() || employee.getMarkCode().trim().length() == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "AttendanceCheck.warn.withoutMarkCode", employee.getFullName());
        }
        try {
            days = attendanceCheckService.check(contract, startDate, endDate);
            checked = true;
        } catch (Exception e) {
            log.error("No se pudo verificar la asistencia", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "AttendanceCheck.error.failed");
        }
    }

    public void assignEmployee(Employee selected) {
        this.employee = selected;
        this.days = new ArrayList<AttendanceDay>();
        this.checked = false;
    }

    public void clearEmployee() {
        assignEmployee(null);
    }

    // --------------------------------------------------------------- totales

    public int getWorkedDays() {
        int total = 0;
        for (AttendanceDay day : days) {
            if (day.isWorking() && !day.isAbsent()) {
                total++;
            }
        }
        return total;
    }

    public int getAbsentDays() {
        int total = 0;
        for (AttendanceDay day : days) {
            if (day.isAbsent()) {
                total++;
            }
        }
        return total;
    }

    public int getLateDays() {
        int total = 0;
        for (AttendanceDay day : days) {
            if (day.isLate()) {
                total++;
            }
        }
        return total;
    }

    public int getLatenessMinutes() {
        int total = 0;
        for (AttendanceDay day : days) {
            total += day.getLatenessMinutes();
        }
        return total;
    }

    public Double getScheduledHours() {
        long minutes = 0;
        for (AttendanceDay day : days) {
            if (day.isWorking()) {
                minutes += day.getJourney().getMinutes();
            }
        }
        return minutes / 60d;
    }

    public boolean isEmptyResult() {
        return checked && days.isEmpty();
    }

    // --------------------------------------------------------------- getters

    public List<AttendanceDay> getDays() {
        return days;
    }

    public boolean isChecked() {
        return checked;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
