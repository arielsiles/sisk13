package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ControlReport;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data model for ControlReport
 *
 * @author
 */

@Name("controlReportDataModel")
@Scope(ScopeType.CONVERSATION)
public class ControlReportDataModel extends QueryDataModel<Long, ControlReport> {


    /**
     * La busqueda por persona cuelga del CONTRATO y ya no de la banda horaria.
     * <p/>
     * Con el motor nuevo no hay banda: el camino viejo -banda, puesto, contrato, empleado- dejaba
     * la fila huerfana y la busqueda por nombre no encontraba nada.
     */
    private String person;

    private static final String[] RESTRICTIONS = {
            "controlReport.generatedPayroll = #{controlReportAction.generatedPayroll}",
            "lower(concat(concat(concat(concat(employee.lastName, ' '), concat(employee.maidenName, ' ')), employee.firstName), concat(' ', employee.idNumber)))"
                    + " like concat('%', concat(lower(#{controlReportDataModel.person}), '%'))"};


    @Create
    public void init() {
        sortProperty = "controlReport.date";
    }

    @Override
    public String getEjbql() {
        return "select controlReport from ControlReport controlReport " +
                " left join fetch controlReport.contract contract" +
                " left join fetch contract.employee employee";

    }

    /** Un solo campo para buscar: apellidos, nombre o documento. */
    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = (null == person || 0 == person.trim().length()) ? null : person.trim();
    }

    public List<ControlReport> getSelectedControlReportDates() {
        List ids = super.getSelectedIdList();

        List<ControlReport> result = new ArrayList<ControlReport>();
        for (Object id : ids) {
            ControlReport item = getEntityManager().find(ControlReport.class, id);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
