package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.TimeInterval;
import com.encens.khipus.model.finances.OrganizationalUnit;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Special Date service interface
 *
 * @author: Ariel Siles Encinas
 */

@Local
public interface SpecialDateService {

    /**
     * This method return a list that content every special date that have been asigned to
     * the employee. E.g. This example is just a special date asigned to the employee.
     * <p/>
     * initPeriod=01/01/2010 endPeriod=05/01/2010
     * <p/>
     * The result list will be.   ResultList={01/01/2010,02/01/2010,03/01/2010,04/01/2010,05/01/2010}
     * <p/>
     * This process take every date that is insithe de special date range(initPeriod, endPeriod)
     *
     * @param employee
     * @param initDate
     * @param endDate
     * @return A list that has the all days that employee has some special date.
     */
    List<Date> getSpecialDateRange(Employee employee, Date initDate, Date endDate);

    List<Date> getSpecialDateRange(OrganizationalUnit organizationalUnit, Date initDate, Date endDate);

    List<Date> getSpecialDateRange(BusinessUnit businessUnit, Date initDate, Date endDate);

    Map<Date, List<TimeInterval>> getSpecialDateTimeRange(Employee employee, Date initDate, Date endDate);

    Map<Date, List<TimeInterval>> getSpecialDateTimeRange(OrganizationalUnit organizationalUnit, Date initDate, Date endDate);

    Map<Date, List<TimeInterval>> getSpecialDateTimeRange(BusinessUnit businessUnit, Date initDate, Date endDate);
}