package com.encens.khipus.service.employees;


import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.RHMark;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * IvaPayroll services interface
 *
 * @author
 */

@Local
public interface RHMarkService {
    /*
   @param firstDayOfTheMonth the bottom date that is the bottom limit to consider a HoraryBand as valid.
    */

    List<RHMark> findRHMarkByEmployeeIdNumberByInitDateByEndDate(Employee employee, Date initDate, Date endDate);

    Map<Date, List<Date>> getRHMarkDateTimeMapByDateRange(Employee employee, Date initDate, Date endDate);

    List<RHMark> findRHMarkByInitDateByEndDate(Date initDate, Date endDate);

    boolean verificateIdPerson(int idPerson);

    public void registerMark(RHMark rhMark);
}