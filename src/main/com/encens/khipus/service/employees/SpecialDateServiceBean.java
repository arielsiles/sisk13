package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.SpecialDateTarget;
import com.encens.khipus.model.employees.SpecialDateType;
import com.encens.khipus.model.employees.TimeInterval;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.DateIterator;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;

/**
 * Employee discount service implementation class
 *
 * @author: Ariel Siles Encinas
 */

@Stateless
@Name("specialDateService")
@AutoCreate
public class SpecialDateServiceBean implements SpecialDateService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Logger
    protected Log log;

    /**
     * This method return a list that content every special date that have been assigned to
     * the employee. E.g. This example is just a special date assigned to the employee.
     * <p/>
     * initPeriod=01/01/2010 endPeriod=05/01/2010
     * <p/>
     * The result list will be.   ResultList={01/01/2010,02/01/2010,03/01/2010,04/01/2010,05/01/2010}
     * <p/>
     * This process has take every date that is inside the de special date range(initPeriod, endPeriod),
     * but the current range  depend of initDate and endDate, if could has some intersections the method
     * will processed the intersection range.
     *
     * @param employee
     * @param initDate
     * @param endDate
     * @return A list that has the all days that employee has some special date.
     */
    public List<Date> getSpecialDateRange(Employee employee, Date initDate, Date endDate) {
        try {
            return getDateRangeList(em.createNamedQuery("SpecialDate.findSpecialDateRangeByEmployee")
                    .setParameter("specialDateTarget", SpecialDateTarget.EMPLOYEE)
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("credit", SpecialDateType.PAID)
                    .setParameter("allDay", true)
                    .getResultList(), initDate, endDate);
        } catch (Exception e) {

        }
        return new ArrayList<Date>();
    }

    public Map<Date, List<TimeInterval>> getSpecialDateTimeRange(Employee employee, Date initDate, Date endDate) {
        try {
            return getDateTimeIntervalRangeList(em.createNamedQuery("SpecialDate.findSpecialDateTimeRangeByEmployee")
                    .setParameter("specialDateTarget", SpecialDateTarget.EMPLOYEE)
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("credit", SpecialDateType.PAID)
                    .setParameter("allDay", false)
                    .getResultList(), initDate, endDate);
        } catch (NoResultException e) {
            return new LinkedHashMap<Date, List<TimeInterval>>();
        }
    }

    public List<Date> getSpecialDateRange(OrganizationalUnit organizationalUnit, Date initDate, Date endDate) {
        try {
            return getDateRangeList(em.createNamedQuery("SpecialDate.findSpecialDateRangeByOrganizationalUnit")
                    .setParameter("specialDateTarget", SpecialDateTarget.ORGANIZATIONALUNIT)
                    .setParameter("organizationalUnit", organizationalUnit)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("credit", SpecialDateType.PAID)
                    .setParameter("allDay", true)
                    .getResultList(), initDate, endDate);
        } catch (Exception e) {
        }
        return new ArrayList<Date>();
    }

    public Map<Date, List<TimeInterval>> getSpecialDateTimeRange(OrganizationalUnit organizationalUnit, Date initDate, Date endDate) {
        try {
            return getDateTimeIntervalRangeList(em.createNamedQuery("SpecialDate.findSpecialDateTimeRangeByOrganizationalUnit")
                    .setParameter("specialDateTarget", SpecialDateTarget.ORGANIZATIONALUNIT)
                    .setParameter("organizationalUnit", organizationalUnit)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("credit", SpecialDateType.PAID)
                    .setParameter("allDay", false)
                    .getResultList(), initDate, endDate);
        } catch (NoResultException e) {
            return new LinkedHashMap<Date, List<TimeInterval>>();
        }
    }

    public List<Date> getSpecialDateRange(BusinessUnit businessUnit, Date initDate, Date endDate) {
        try {
            return getDateRangeList(em.createNamedQuery("SpecialDate.findSpecialDateRangeByBusinessUnit")
                    .setParameter("specialDateTarget", SpecialDateTarget.BUSINESSUNIT)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("credit", SpecialDateType.PAID)
                    .setParameter("allDay", true)
                    .getResultList(), initDate, endDate);
        } catch (NoResultException e) {
            return new ArrayList<Date>();
        }
    }

    public Map<Date, List<TimeInterval>> getSpecialDateTimeRange(BusinessUnit businessUnit, Date initDate, Date endDate) {
        try {
            return getDateTimeIntervalRangeList(em.createNamedQuery("SpecialDate.findSpecialDateTimeRangeByBusinessUnit")
                    .setParameter("specialDateTarget", SpecialDateTarget.BUSINESSUNIT)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("credit", SpecialDateType.PAID)
                    .setParameter("allDay", false)
                    .getResultList(), initDate, endDate);
        } catch (NoResultException e) {
            return new LinkedHashMap<Date, List<TimeInterval>>();
        }
    }

    private List<Date> getDateRangeList(List<Object[]> dateResult, Date initDate, Date endDate) {
        List<Date> result = new ArrayList<Date>();
        if (dateResult != null) {
            for (Object[] periodDate : dateResult) {
                Date initPeriod = (Date) periodDate[0];
                Date endPeriod = (Date) periodDate[1];
                DateIterator iDate = new DateIterator(initDate.compareTo(initPeriod) < 0 ? initPeriod : initDate, endPeriod.compareTo(endDate) < 0 ? endPeriod : endDate);
                while (iDate.hasNext()) {
                    result.add(iDate.getCurrent());
                    iDate.next();
                }
            }
        }
        return result;
    }

    private Map<Date, List<TimeInterval>> getDateTimeIntervalRangeList(List<Object[]> dateTimeResult, Date initDate, Date endDate) {
        Map<Date, List<TimeInterval>> result = new LinkedHashMap<Date, List<TimeInterval>>();
        if (dateTimeResult != null) {
            for (Object[] periodDate : dateTimeResult) {
                Date initPeriod = (Date) periodDate[0];
                Date endPeriod = (Date) periodDate[1];
                Date initTime = (Date) periodDate[2];
                Date endTime = (Date) periodDate[3];
                DateIterator iDate = new DateIterator(
                        initDate.compareTo(initPeriod) < 0 ? initPeriod : initDate,
                        endPeriod.compareTo(endDate) < 0 ? endPeriod : endDate);
                while (iDate.hasNext()) {
                    TimeInterval timeInterval = new TimeInterval();
                    timeInterval.setStart(initTime);
                    timeInterval.setEnd(endTime);
                    Date current = iDate.getCurrent();
                    if (result.containsKey(current)) {
                        List<TimeInterval> timeIntervalList = result.get(current);
                        timeIntervalList.add(timeInterval);
                    } else {
                        List<TimeInterval> timeIntervalList = new ArrayList<TimeInterval>();
                        timeIntervalList.add(timeInterval);
                        result.put(current, timeIntervalList);
                    }
                    iDate.next();
                }
            }
        }
        return result;
    }
}