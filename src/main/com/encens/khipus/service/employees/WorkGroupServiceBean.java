package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("workGroupService")
@AutoCreate
public class WorkGroupServiceBean extends GenericServiceBean implements WorkGroupService {

    @SuppressWarnings({"unchecked"})
    public List<WorkGroup> findActiveList() {
        List<WorkGroup> resultList = getEntityManager()
                .createNamedQuery("WorkGroup.findActive")
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return null == resultList ? new ArrayList<WorkGroup>() : resultList;
    }

    public Long countByName(String name, Long excludedId) {
        if (null == excludedId) {
            return (Long) getEntityManager().createNamedQuery("WorkGroup.countByName")
                    .setParameter("name", name).getSingleResult();
        }
        return (Long) getEntityManager().createNamedQuery("WorkGroup.countByNameButThis")
                .setParameter("name", name)
                .setParameter("id", excludedId).getSingleResult();
    }

    @SuppressWarnings({"unchecked"})
    public WorkGroupMembership findMembership(Contract contract, Date date) {
        if (null == contract || null == date) {
            return null;
        }
        List<WorkGroupMembership> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findByContractAndDate")
                .setParameter("contract", contract)
                .setParameter("date", date)
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<WorkGroupMembership> findMemberships(WorkGroup workGroup) {
        if (null == workGroup || null == workGroup.getId()) {
            return new ArrayList<WorkGroupMembership>();
        }
        List<WorkGroupMembership> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findByGroup")
                .setParameter("workGroup", workGroup)
                .getResultList();
        return null == resultList ? new ArrayList<WorkGroupMembership>() : resultList;
    }

    @SuppressWarnings({"unchecked"})
    public WorkGroupMembership findOverlapping(WorkGroupMembership membership) {
        List<WorkGroupMembership> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findOverlapping")
                .setParameter("contract", membership.getContract())
                .setParameter("id", null == membership.getId() ? -1L : membership.getId())
                .setParameter("startDate", membership.getStartDate())
                .setParameter("endDate", membership.getEndDate())
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }
}
