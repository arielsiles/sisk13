package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.SpecialDateService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Actions for ControlReport
 *
 * @author Ariel Siles
 */

@Name("controlReportAction")
@Scope(ScopeType.CONVERSATION)
public class ControlReportAction extends GenericAction<ControlReport> {

    @In
    private SpecialDateService specialDateService;

    private GeneratedPayroll generatedPayroll;

    @Factory(value = "controlReport", scope = ScopeType.STATELESS)
    public ControlReport initControlReport() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "id";
    }

    @Begin(nested = true)
    public String viewControlReport(GeneratedPayroll generatedPayroll) {
        this.setGeneratedPayroll(generatedPayroll);
        return Outcome.SUCCESS;
    }

    public void generateSpecialDates(List<ControlReport> controlReportDateList){

        List<SpecialDate> specialDateSelectedList = new ArrayList<SpecialDate>();

        for (ControlReport control : controlReportDateList){

            Date initHour = control.getHoraryBandContract().getHoraryBand().getInitHour();
            Date endHour = control.getHoraryBandContract().getHoraryBand().getEndHour();

            System.out.println("----------> Horario inicio: " + initHour + " - " + initHour.getTime());
            System.out.println("----------> Horario fin: " + endHour + " - " + endHour.getTime());

            SpecialDate specialDate = new SpecialDate();
            specialDate.setEmployee(control.getHoraryBandContract().getJobContract().getContract().getEmployee());
            specialDate.setTitle("JUSTIFICACION DE HORARIO");
            specialDate.setInitPeriod(control.getDate());
            specialDate.setEndPeriod(control.getDate());

            specialDate.setStartTime(DateUtils.removeDate(initHour));
            specialDate.setEndTime(DateUtils.removeDate(endHour));

            specialDate.setCredit(SpecialDateType.PAID);
            specialDate.setAllDay(Boolean.FALSE);
            specialDate.setRolType(SpecialDateRol.FECHA);
            specialDate.setSpecialDateTarget(SpecialDateTarget.EMPLOYEE);

            System.out.println("-----> StartTime: " + specialDate.getStartTime());
            System.out.println("-----> EndTime: " + specialDate.getEndTime());

            specialDateSelectedList.add(specialDate);
        }

        specialDateService.createSpecialDates(specialDateSelectedList);

    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}