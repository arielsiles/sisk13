package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HoraryBand;
import com.encens.khipus.model.employees.HoraryBandContract;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.OrganizationalUnit;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import java.util.*;

/**
 * HoraryBandContractService services interface
 *
 * @author
 * @version 1.1.0
 */

@Local
public interface HoraryBandContractService {

    void create(HoraryBandContract horaryBandContract, HoraryBand horaryBand) throws EntryDuplicatedException;

    void update(HoraryBandContract horaryBandContract, HoraryBand horaryBand) throws ConcurrencyException, EntryDuplicatedException;

    void delete(HoraryBandContract horaryBandContract, HoraryBand horaryBand) throws ConcurrencyException, ReferentialIntegrityException;

    Boolean checkOverlap(Long id, String idNumber, Date initDate, Date endDate, Date initHour, Date endHour, String initDay, String endDay);

    Boolean checkOverlapWithoutReference(String idNumber, Date initDate, Date endDate, Date initHour, Date endHour, String initDay, String endDay);

    Boolean checkContractRange(Employee employee, OrganizationalUnit organizationalUnit, Date date);

    List<HoraryBandContract> getValidHoraryBandContractsByContract(Contract contract, Date initDate, Date endDate);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @org.jboss.ejb3.annotation.TransactionTimeout(60000)
    List<HoraryBandContract> getValidHoraryBandContractsByEmployee(Employee employee, Date initDate, Date endDate);

    /**
     * @param controlDayOfMonth the day of the month for wich we ask if the HoraryBand is valid.
     * @return a list of HoraryBandContract list valid for a specific date of month. Notice that an employee may have
     *         some HoraryBands that are valid only a few days in a month.
     */

    List<HoraryBandContract> findValidHoraryBandContracts4Date(List<HoraryBandContract> hourlyBandContract4EmployeeList, Calendar controlDayOfMonth);

    Hashtable<Integer, List<HoraryBandContract>> getHoraryBandContractMapByDay(List<HoraryBandContract> horaryBandContractList);

    List<HoraryBandContract> filterBandsByContract(Contract contract, List<HoraryBandContract> hourlyBandContract4EmployeeList);

    List<HoraryBandContract> cleanDuplicate(List<HoraryBandContract> horaryBandContractList);

    HoraryBandContract getHoraryBandContractByAcademicSchedule(Long academicSchedule);

    List<HoraryBandContract> getHoraryBandContractsByJobContractActive(JobContract jobContract, Boolean isActive);

    @SuppressWarnings({"unchecked"})
    List<HoraryBandContract> findValidHoraryBandContractForMarProcessorByEmployeeAndDateRangeAndCurrentDate(
            Employee employee, Date initDate, Date endDate, Date currentDate, EntityManager entityManager);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @org.jboss.ejb3.annotation.TransactionTimeout(60000)
    List<HoraryBandContract> getValidHoraryBandContractsByEmployeeAndBusinessUnitAndJobCategory(
            Employee employee, BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate);

    Map<Long, List<JobContract>> getJobContractWithValidHoraryBandContractByEmployeeBusinessUnitJobCategory(List<Long> employeeIdList, BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate);

    /**
     * @param horaryBandContract
     * @param controlDayOfMonth  the day of the month for wich we ask if the HoraryBand is valid.
     * @return a list of HoraryBandContract list valid for a specific date of month. Notice that an employee may have
     *         some HoraryBands that are valid only a few days in a month.
     */

    HoraryBandContract findValidHoraryBand4Date(HoraryBandContract horaryBandContract, Calendar controlDayOfMonth);
}
