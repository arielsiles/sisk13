package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.ScheduleException;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("scheduleExceptionService")
@AutoCreate
public class ScheduleExceptionServiceBean extends GenericServiceBean implements ScheduleExceptionService {

    @SuppressWarnings({"unchecked"})
    public ScheduleException find(Contract contract, Date date) {
        if (null == contract || null == date) {
            return null;
        }
        List<ScheduleException> resultList = getEntityManager()
                .createNamedQuery("ScheduleException.findByContractAndDate")
                .setParameter("contract", contract)
                .setParameter("date", startOfDay(date))
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    public Long countByWorkShift(WorkShift workShift) {
        if (null == workShift || null == workShift.getId()) {
            return 0L;
        }
        return (Long) getEntityManager().createNamedQuery("ScheduleException.countByWorkShift")
                .setParameter("workShift", workShift).getSingleResult();
    }

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
