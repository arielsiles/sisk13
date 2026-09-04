package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.contacts.Gender;
import com.encens.khipus.model.employees.WeeklyWorkload;
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
@Name("weeklyWorkloadService")
@AutoCreate
public class WeeklyWorkloadServiceBean extends GenericServiceBean implements WeeklyWorkloadService {

    @SuppressWarnings({"unchecked"})
    public WeeklyWorkload findByGender(Gender gender) {
        if (null == gender) {
            return null;
        }
        List<WeeklyWorkload> resultList = getEntityManager()
                .createNamedQuery("WeeklyWorkload.findByGender")
                .setParameter("gender", gender)
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<WeeklyWorkload> findActiveList() {
        List<WeeklyWorkload> resultList = getEntityManager()
                .createNamedQuery("WeeklyWorkload.findActiveList")
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return null == resultList ? new ArrayList<WeeklyWorkload>() : resultList;
    }

    public Long countActiveByGender(Gender gender, Long excludedId) {
        if (null == excludedId) {
            return (Long) getEntityManager().createNamedQuery("WeeklyWorkload.countActiveByGender")
                    .setParameter("gender", gender)
                    .setParameter("active", Boolean.TRUE)
                    .getSingleResult();
        }
        return (Long) getEntityManager().createNamedQuery("WeeklyWorkload.countActiveByGenderButThis")
                .setParameter("gender", gender)
                .setParameter("active", Boolean.TRUE)
                .setParameter("id", excludedId)
                .getSingleResult();
    }
}
