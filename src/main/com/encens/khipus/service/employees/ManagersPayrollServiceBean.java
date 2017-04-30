package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.2
 */
@Name("managersPayrollService")
@Stateless
@AutoCreate
public class ManagersPayrollServiceBean extends GenericServiceBean implements ManagersPayrollService {

    /**
     * find employees related to a given generatedPayroll, that are active for payroll generation
     * in DECEMBER of the year related to the generatedPayroll
     *
     * @param generatedPayroll the GeneratedPayroll parameter
     * @return a List of Id of employees related to a given generatedPayroll, that are active for payroll generation
     */
    @SuppressWarnings(value = "unchecked")
    public List<Long> findEmployeeIdListByGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        try {
            GestionPayroll gestionPayroll = findById(GestionPayroll.class, generatedPayroll.getGestionPayrollId());
            Date initDate = DateUtils.getFirstDayOfMonth(Month.DECEMBER.getValueAsPosition(), gestionPayroll.getGestion().getYear());
            Date endDate = DateUtils.getLastDayOfMonth(initDate);
            return getEntityManager().createNamedQuery("ManagersPayroll.findEmployeeIdListByGeneratedPayroll")
                    .setParameter("generatedPayroll", generatedPayroll)
                    .setParameter("activeForPayrollGeneration", true)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Long>();
        } catch (EntryNotFoundException e) {
            return new ArrayList<Long>();
        }
    }

    /**
     * find employees related to a given generatedPayroll, that are active for payroll generation
     * in DECEMBER of the year related to the generatedPayroll and in a given employeeIdList
     *
     * @param generatedPayroll the GeneratedPayroll parameter
     * @return a List of Id of employees related to a given generatedPayroll, that are active for payroll generation
     */
    @SuppressWarnings(value = "unchecked")
    public List<Long> findEmployeeIdListByGeneratedPayrollInEmployeeIdList(GeneratedPayroll generatedPayroll, List<Long> employeeIdList) {
        try {
            GestionPayroll gestionPayroll = findById(GestionPayroll.class, generatedPayroll.getGestionPayrollId());
            Date initDate = DateUtils.getFirstDayOfMonth(Month.DECEMBER.getValueAsPosition(), gestionPayroll.getGestion().getYear());
            Date endDate = DateUtils.getLastDayOfMonth(initDate);
            return getEntityManager().createNamedQuery("ManagersPayroll.findEmployeeIdListByGeneratedPayrollInEmployeeIdList")
                    .setParameter("generatedPayroll", generatedPayroll)
                    .setParameter("employeeIdList", employeeIdList)
                    .setParameter("activeForPayrollGeneration", true)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Long>();
        } catch (EntryNotFoundException e) {
            return new ArrayList<Long>();
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<Object[]> findByGeneratedPayrollAndEmployeeIdList(GeneratedPayroll generatedPayroll, List<Long> employeeIdList) {
        try {
            return (List<Object[]>) getEntityManager().createNamedQuery("ManagersPayroll.findByGeneratedPayrollAndEmployeeIdList")
                    .setParameter("generatedPayroll", generatedPayroll)
                    .setParameter("employeeIdList", employeeIdList)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Object[]>();
        }
    }
}
