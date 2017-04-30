package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Bonus;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.JobContract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("jobContractForPayrollService")
@AutoCreate
public class JobContractForPayrollServiceBean implements JobContractForPayrollService {

    @In
    private EntityManager entityManager;
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @Logger
    protected Log log;

    public List<JobContract> getList(int firstRow, int maxResults, String sortProperty, boolean sortAsc,
                                     String idNumber, String firstName, String maidenName, String lastName,
                                     Date startDate, Date endDate, BusinessUnit businessUnit, PayrollGenerationType payrollGenerationType,
                                     List<Long> selectedJobContractIdList, Bonus bonus) {
        Query query = buildQuery("select jobContract from JobContract jobContract",
                sortProperty, sortAsc, idNumber, firstName, maidenName, lastName, startDate, endDate,
                businessUnit, payrollGenerationType, selectedJobContractIdList, bonus);
        query.setFirstResult(firstRow);
        query.setMaxResults(maxResults);

        return query.getResultList();
    }

    public Long getCount(String sortProperty, boolean sortAsc,
                         String idNumber, String firstName, String maidenName, String lastName,
                         Date startDate, Date endDate, BusinessUnit businessUnit, PayrollGenerationType payrollGenerationType,
                         List<Long> selectedJobContractIdList, Bonus bonus) {
        Query query = buildQuery("select jobContract from JobContract jobContract",
                sortProperty, sortAsc, idNumber, firstName, maidenName, lastName, startDate, endDate,
                businessUnit, payrollGenerationType, selectedJobContractIdList, bonus);

        return (long) query.getResultList().size();
    }

    private Query buildQuery(String select, String sortProperty, boolean sortAsc,
                             String idNumber, String firstName, String maidenName, String lastName,
                             Date startDate, Date endDate, BusinessUnit businessUnit, PayrollGenerationType payrollGenerationType,
                             List<Long> selectedJobContractIdList, Bonus bonus) {
        StringBuilder queryString = new StringBuilder(select);
        queryString.append(" left join fetch jobContract.contract contract");
        queryString.append(" left join fetch contract.employee employee");
        queryString.append(" left join fetch jobContract.job job");
        queryString.append(" left join fetch job.charge charge");
        queryString.append(" left join fetch job.jobCategory jobCategory");
        queryString.append(" left join fetch jobCategory.sector sector");
        queryString.append(" left join fetch job.organizationalUnit organizationalUnit");
        queryString.append(" where contract.activeForTaxPayrollGeneration = :activeForTaxPayrollGeneration");
        queryString.append(" and jobContract.job.organizationalUnit.businessUnit = :businessUnit");
        queryString.append(" and ((contract.initDate <= :startDate and contract.endDate >= :endDate) or (contract.initDate >=:startDate and contract.initDate <= :endDate) or (contract.endDate >= :startDate and contract.endDate <= :endDate))");
        if (null != selectedJobContractIdList) {
            queryString.append(" and jobContract.id not in(:idList) ");
        }
        if (null != bonus) {
            queryString.append(" and jobContract not in (select jc from GrantedBonus grantedBonus join grantedBonus.jobContract jc where grantedBonus.bonus=:bonus) ");
        }
        if (idNumber != null) {
            queryString.append(" and employee.idNumber like concat(:idNumber, '%')");
        }
        if (firstName != null) {
            queryString.append(" and lower(employee.firstName) like concat('%', concat(lower(:firstName), '%'))");
        }
        if (maidenName != null) {
            queryString.append(" and lower(employee.maidenName) like concat('%', concat(lower(:maidenName), '%'))");
        }
        if (lastName != null) {
            queryString.append(" and lower(employee.lastName) like concat('%', concat(lower(:lastName), '%'))");
        }
        if (payrollGenerationType != null) {
            queryString.append(" and jobCategory.payrollGenerationType <>:payrollGenerationType");
        }
        queryString.append(" order by ").append(sortProperty).append(sortAsc ? " ASC" : " DESC");

        Query query = entityManager.createQuery(queryString.toString());
        query.setParameter("activeForTaxPayrollGeneration", true);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("businessUnit", businessUnit);
        if (null != selectedJobContractIdList) {
            query.setParameter("idList", selectedJobContractIdList);
        }
        if (null != bonus) {
            query.setParameter("bonus", bonus);
        }
        if (idNumber != null) {
            query.setParameter("idNumber", idNumber);
        }
        if (firstName != null) {
            query.setParameter("firstName", firstName);
        }
        if (maidenName != null) {
            query.setParameter("maidenName", maidenName);
        }
        if (lastName != null) {
            query.setParameter("lastName", lastName);
        }
        if (payrollGenerationType != null) {
            query.setParameter("payrollGenerationType", payrollGenerationType);
        }
        return query;
    }
}