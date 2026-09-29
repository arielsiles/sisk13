package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.ManagersPayroll;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data model for ManagersPayroll
 *
 * @author
 */

@Name("managersPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class ManagersPayrollDataModel extends GenericPayrollDataModel<Long, ManagersPayroll> {
    /**
     * Dos filtros y no cuatro: el documento y un solo campo de persona.
     * <p/>
     * El campo de persona busca sobre nombres y apellidos juntos, en el mismo orden en que la
     * lista los muestra, asi que se puede escribir un apellido, un nombre o los dos.
     */
    private static final String[] RESTRICTIONS = {
            "lower(managersPayroll.employee.idNumber) like concat(lower(#{managersPayrollDataModel.idNumber}), '%')",
            "lower(concat(concat(concat(employee.firstName, ' '), concat(employee.lastName, ' ')), employee.maidenName))"
                    + " like concat('%', concat(lower(#{managersPayrollDataModel.person}), '%'))"
    };

    @Create
    public void init() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        sortProperty = "managersPayroll.employee.lastName";
    }

    @Override
    public void search() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        super.search();
    }

    @Override
    public String getEjbql() {
        return "select managersPayroll from ManagersPayroll managersPayroll" +
                " left join fetch managersPayroll.employee employee" +
                " where managersPayroll.generatedPayroll.id=" + getGeneratedPayroll().getId();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    /* Sigla de la unidad organizacional por empleado, de la planilla que se esta mirando. */
    private Map<Long, String> areaAcronyms;
    private Long areaAcronymsPayrollId;

    /**
     * La sigla de la unidad organizacional (Area) de cada empleado: empleado -> contrato vigente
     * en el periodo de la planilla -> puesto -> unidad organizacional. La planilla guarda solo el
     * nombre de la unidad, no la sigla. Una sola consulta por planilla; si hay dos contratos en
     * el periodo manda el mas reciente.
     */
    @SuppressWarnings({"unchecked"})
    public Map<Long, String> getAreaAcronyms() {
        GeneratedPayroll generatedPayroll = getGeneratedPayroll();
        if (null == generatedPayroll || null == generatedPayroll.getGestionPayroll()) {
            return new HashMap<Long, String>();
        }
        if (null == areaAcronyms || !generatedPayroll.getId().equals(areaAcronymsPayrollId)) {
            List<Object[]> rows = getEntityManager().createQuery(
                    "select contract.employee.id, organizationalUnit.acronym from JobContract jobContract" +
                            " join jobContract.contract contract" +
                            " join jobContract.job job" +
                            " join job.organizationalUnit organizationalUnit" +
                            " where contract.employee.id in (select managersPayroll.employee.id" +
                            " from ManagersPayroll managersPayroll" +
                            " where managersPayroll.generatedPayroll.id = :generatedPayrollId)" +
                            " and contract.initDate <= :endDate" +
                            " and (contract.endDate is null or contract.endDate >= :initDate)" +
                            " order by contract.initDate")
                    .setParameter("generatedPayrollId", generatedPayroll.getId())
                    .setParameter("initDate", generatedPayroll.getGestionPayroll().getInitDate())
                    .setParameter("endDate", generatedPayroll.getGestionPayroll().getEndDate())
                    .getResultList();
            areaAcronyms = new HashMap<Long, String>();
            for (Object[] row : rows) {
                areaAcronyms.put((Long) row[0], (String) row[1]);
            }
            areaAcronymsPayrollId = generatedPayroll.getId();
        }
        return areaAcronyms;
    }
}