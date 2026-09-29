package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.WorkShift;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("workShiftService")
@AutoCreate
public class WorkShiftServiceBean extends GenericServiceBean implements WorkShiftService {

    @SuppressWarnings({"unchecked"})
    public List<WorkShift> findActiveList() {
        List<WorkShift> resultList = getEntityManager()
                .createNamedQuery("WorkShift.findActive")
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return null == resultList ? new ArrayList<WorkShift>() : resultList;
    }

    public Long countByName(String name, Long excludedId) {
        if (null == excludedId) {
            return (Long) getEntityManager().createNamedQuery("WorkShift.countByName")
                    .setParameter("name", name).getSingleResult();
        }
        return (Long) getEntityManager().createNamedQuery("WorkShift.countByNameButThis")
                .setParameter("name", name)
                .setParameter("id", excludedId).getSingleResult();
    }
}
