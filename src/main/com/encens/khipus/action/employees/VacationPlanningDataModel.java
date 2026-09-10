package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.VacationPlanning;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Scope;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.4
 */
@Name("vacationPlanningDataModel")
@Scope(ScopeType.PAGE)
public class VacationPlanningDataModel extends QueryDataModel<Long, VacationPlanning> {

    @In
    private Map<String, String> messages;

    private String employeeName;
    private BusinessUnit businessUnit;

    private static final String[] RESTRICTIONS = {
            "vacationPlanning.code = #{vacationPlanningDataModel.criteria.code}",
            "businessUnit = #{vacationPlanningDataModel.businessUnit}",
            /* Un solo campo para la persona. Antes habia que abrir un modal, buscar, elegir y
               volver -cuatro pasos para filtrar por alguien-. Se busca en nombre y los dos
               apellidos concatenados: con campos separados hay que acertar en cual esta el
               texto. El coalesce va porque un apellido materno nulo anularia la concatenacion
               entera y esa persona no aparecería nunca. */
            "lower(concat(concat(concat(coalesce(employee.firstName,''),' '),concat(coalesce(employee.lastName,''),' ')),coalesce(employee.maidenName,''))) like concat('%', concat(lower(#{vacationPlanningDataModel.employeeName}), '%'))",
            /* Abiertos y cerrados. Por defecto solo los abiertos: quien reingreso tiene mas de
               un plan y sin esto aparece repetido sin que se sepa cual es el vigente.
               Se filtra comparando contra una fecha tope en lugar de escribir "is null" porque
               cada #{} se convierte en un parametro, no en texto: no hay forma de meter "is
               null" por ahi. Con la fecha tope, "cerrado antes del ano 9999" es lo mismo que
               "tiene fecha de cierre", y cada restriccion se apaga sola cuando su valor es
               nulo, que es como se ven todos. */
            "(vacationPlanning.closeDate is null or vacationPlanning.closeDate > #{vacationPlanningDataModel.openLimit})",
            "vacationPlanning.closeDate <= #{vacationPlanningDataModel.closedLimit}"
    };

    public static final String OPEN_ONLY = "OPEN";
    public static final String CLOSED_ONLY = "CLOSED";

    /** Una fecha que ningun cierre real va a alcanzar. */
    private static final Date FAR_FUTURE = new GregorianCalendar(9999, Calendar.DECEMBER, 31).getTime();

    private String closed = OPEN_ONLY;

    public Date getOpenLimit() {
        return OPEN_ONLY.equals(closed) ? FAR_FUTURE : null;
    }

    public Date getClosedLimit() {
        return CLOSED_ONLY.equals(closed) ? FAR_FUTURE : null;
    }

    public List<SelectItem> getClosedItems() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem(OPEN_ONLY, messages.get("VacationPlanning.open")));
        items.add(new SelectItem(CLOSED_ONLY, messages.get("VacationPlanning.closed")));
        items.add(new SelectItem(null, messages.get("VacationPlanning.allPeriods")));
        return items;
    }

    public String getClosed() {
        return closed;
    }

    public void setClosed(String closed) {
        this.closed = (null == closed || 0 == closed.trim().length()) ? null : closed.trim();
    }

    @Create
    public void init() {
        sortProperty = "vacationPlanning.code";
    }

    @Override
    public String getEjbql() {
        return "select vacationPlanning from VacationPlanning vacationPlanning" +
                " left join fetch vacationPlanning.jobContract jobContract " +
                " left join fetch jobContract.job job" +
                " left join fetch job.organizationalUnit organizationalUnit" +
                " left join fetch organizationalUnit.businessUnit businessUnit" +
                " left join fetch jobContract.contract contract" +
                " left join fetch contract.employee employee";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }
}
