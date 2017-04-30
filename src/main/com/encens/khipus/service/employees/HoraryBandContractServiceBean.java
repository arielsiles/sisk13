package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.DayMap;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import java.util.*;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * HoraryBandContractServiceBean service implementation class
 *
 * @author
 * @version 1.1.0
 */

@Stateless
@Name("horaryBandContractService")
@AutoCreate
public class HoraryBandContractServiceBean implements HoraryBandContractService {
    @In("#{entityManager}")
    private EntityManager em;

    @Logger
    private Log log;

    @TransactionAttribute(REQUIRES_NEW)
    public void create(HoraryBandContract horaryBandContract, HoraryBand horaryBand) throws EntryDuplicatedException {
        try {
            for (int i = DayMap.dayStringToInt(horaryBand.getInitDay()); i <= DayMap.dayStringToInt(horaryBand.getEndDay()); i++) {
                HoraryBand horaryBandI = new HoraryBand();
                horaryBandI.setCompany(horaryBand.getCompany());
                // assign the same day for init and end so that its posible create the range in each single day part
                horaryBandI.setInitDay(DayMap.dayIntToString(i));
                horaryBandI.setEndDay(DayMap.dayIntToString(i));
                horaryBandI.setInitHour(horaryBand.getInitHour());
                horaryBandI.setEndHour(horaryBand.getEndHour());
                horaryBandI.setDuration(horaryBand.getDuration());
                horaryBandI.setEveryOtherDay(horaryBand.getEveryOtherDay());
                horaryBandI.setVersion(horaryBand.getVersion());
                em.persist(horaryBandI);
                em.flush();

                HoraryBandContract horaryBandContractI = new HoraryBandContract();
                horaryBandContractI.setHoraryBand(horaryBandI);
                horaryBandContractI.setCompany(horaryBandContract.getCompany());
                horaryBandContractI.setVersion(horaryBandContract.getVersion());
                horaryBandContractI.setInitDate(horaryBandContract.getInitDate());
                horaryBandContractI.setEndDate(horaryBandContract.getEndDate());
                horaryBandContractI.setJobContract(horaryBandContract.getJobContract());
                horaryBandContractI.setLimit(horaryBandContract.getLimit());
                horaryBandContractI.setTimeType(horaryBandContract.getTimeType());
                horaryBandContractI.setTolerance(horaryBandContract.getTolerance());
                em.persist(horaryBandContractI);
                em.flush();

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new EntryDuplicatedException();
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void update(HoraryBandContract horaryBandContract, HoraryBand horaryBand) throws ConcurrencyException, EntryDuplicatedException {
        try {
            if (!em.contains(horaryBand)) {
                em.merge(horaryBand);
                em.flush();
            }
            if (!em.contains(horaryBandContract)) {
                em.merge(horaryBandContract);
            }
            em.flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            throw new EntryDuplicatedException(ee);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void delete(HoraryBandContract horaryBandContract, HoraryBand horaryBand) throws ConcurrencyException, ReferentialIntegrityException {
        try {
            em.remove(horaryBandContract);
            em.flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }
        try {
            em.remove(horaryBand);
            em.flush();
        } catch (Exception e) {

        }
    }

    public Boolean checkOverlap(Long id, String idNumber, Date initDate, Date endDate, Date initHour, Date endHour, String initDay, String endDay) {
        try {
            return em.createNamedQuery("HoraryBandContract.findByIdNumberAndHourRange")
                    .setParameter("id", id)
                    .setParameter("idNumber", idNumber)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("initHour", initHour)
                    .setParameter("endHour", endHour)
                    .setParameter("initDay", initDay)
                    .setParameter("endDay", endDay)
                    .getResultList().size() > 0;
        } catch (Exception e) {
        }
        return Boolean.TRUE;
    }

    public Boolean checkOverlapWithoutReference(String idNumber, Date initDate, Date endDate, Date initHour, Date endHour, String initDay, String endDay) {
        try {
            return em.createNamedQuery("HoraryBandContract.findByIdNumberAndHourRangeWithoutId")
                    .setParameter("idNumber", idNumber)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("initHour", initHour)
                    .setParameter("endHour", endHour)
                    .setParameter("initDay", initDay)
                    .setParameter("endDay", endDay)
                    .getResultList().size() > 0;
        } catch (Exception e) {
        }
        return Boolean.TRUE;
    }

    public Boolean checkContractRange(Employee employee, OrganizationalUnit organizationalUnit, Date date) {
        try {
            return em.createNamedQuery("Contract.findByEmployeeAndOrgUnit")
                    .setParameter("employee", employee)
                    .setParameter("organizationalUnit", organizationalUnit)
                    .setParameter("date", date).getResultList().size() > 0;
        } catch (Exception e) {
        }
        return Boolean.FALSE;
    }

    public List<HoraryBandContract> getValidHoraryBandContractsByContract(Contract contract, Date initDate, Date endDate) {
        try {
            return em.createNamedQuery("HoraryBandContract.findValidHoraryBandContractByContract")
                    .setParameter("contract", contract)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<HoraryBandContract>();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @org.jboss.ejb3.annotation.TransactionTimeout(60000)
    public List<HoraryBandContract> getValidHoraryBandContractsByEmployee(Employee employee, Date initDate, Date endDate) {
        try {
            return em.createNamedQuery("HoraryBandContract.findValidHoraryBandContractByEmployee")
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
        } catch (Exception e) {
            log.info(e);
        }
        return new ArrayList<HoraryBandContract>();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @org.jboss.ejb3.annotation.TransactionTimeout(60000)
    public List<HoraryBandContract> getValidHoraryBandContractsByEmployeeAndBusinessUnitAndJobCategory(
            Employee employee, BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate) {
        try {
            return em.createNamedQuery("HoraryBandContract.getValidHoraryBandContractsByEmployeeAndBusinessUnitAndJobCategory")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                    .getResultList();
        } catch (Exception e) {
            log.info(e);
        }
        return new ArrayList<HoraryBandContract>();
    }

    /**
     * Map with employee and all job contracts related to valid pivot horary band contract
     *
     * @param employeeIdList
     * @param businessUnit
     * @param jobCategory
     * @param initDate
     * @param endDate
     * @return Map
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Map<Long, List<JobContract>> getJobContractWithValidHoraryBandContractByEmployeeBusinessUnitJobCategory(List<Long> employeeIdList, BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate) {
        Map<Long, List<JobContract>> employeeJobContractMap = new HashMap<Long, List<JobContract>>();
        for (Long employeeId : employeeIdList) {
            List<JobContract> jobContractList = getJobContractWithValidHoraryBandContractByEmployeeBusinessUnitJobCategory(employeeId, businessUnit, jobCategory, initDate, endDate);
            employeeJobContractMap.put(employeeId, jobContractList);
        }
        return employeeJobContractMap;
    }

    private List<JobContract> getJobContractWithValidHoraryBandContractByEmployeeBusinessUnitJobCategory(Long employeeId, BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate) {
        List<JobContract> jobContractList = new ArrayList<JobContract>();
        try {
            jobContractList = em.createNamedQuery("HoraryBandContract.getJobContractWithValidHoraryBandContractByEmployeeBusinessUnitJobCategory")
                    .setParameter("employeeId", employeeId)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                    .getResultList();
        } catch (Exception e) {
            log.info(e);
        }
        return jobContractList;
    }

    @SuppressWarnings({"unchecked"})
    public List<HoraryBandContract> findValidHoraryBandContractForMarProcessorByEmployeeAndDateRangeAndCurrentDate(
            Employee employee, Date initDate, Date endDate, Date currentDate, EntityManager entityManager) {
        try {
            return entityManager.createNamedQuery("HoraryBandContract.findValidHoraryBandContractForMarProcessorByEmployeeAndDateRangeAndCurrentDate")
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                    .setParameter("currentDate", currentDate, TemporalType.DATE)
                    .setParameter("pendingHoraryBandStateType", HoraryBandStateType.PENDING)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<HoraryBandContract>();
        }
    }

    /**
     * @param controlDayOfMonth the day of the month for wich we ask if the HoraryBand is valid.
     * @return a list of HoraryBandContract list valid for a specific date of month. Notice that an employee may have
     *         some HoraryBands that are valid only a few days in a month.
     */

    public List<HoraryBandContract> findValidHoraryBandContracts4Date(List<HoraryBandContract> hourlyBandContract4EmployeeList, Calendar controlDayOfMonth) {
        List<HoraryBandContract> validHourlyBandContract4DateList = new ArrayList<HoraryBandContract>();
        for (HoraryBandContract horaryBandContract : hourlyBandContract4EmployeeList) {
            // if the HoraryBand endDate is null, means that the band is valid still the last day of the month
            if (horaryBandContract.getEndDate() == null) {
                // if the controlDayOfMonth is greater than or equal to initDate of the HoraryBand. So the band is valid
                if (controlDayOfMonth.compareTo(DateUtils.toCalendar(horaryBandContract.getInitDate())) >= 0) {
                    validHourlyBandContract4DateList.add(horaryBandContract);
                }
            } else {
                // In this case end and init values are calculated.
                // if the controlDayOfMonth is greater than or equal to initDate of the HoraryBand. So the band is valid
                if ((controlDayOfMonth.compareTo(DateUtils.toCalendar(horaryBandContract.getInitDate())) >= 0) &&
                        (controlDayOfMonth.compareTo(DateUtils.toCalendar(horaryBandContract.getEndDate())) <= 0)) {
                    validHourlyBandContract4DateList.add(horaryBandContract);
                }
            }
        }
        return validHourlyBandContract4DateList;
    }

    /**
     * @param horaryBandContract
     * @param controlDayOfMonth  the day of the month for wich we ask if the HoraryBand is valid.
     * @return a list of HoraryBandContract list valid for a specific date of month. Notice that an employee may have
     *         some HoraryBands that are valid only a few days in a month.
     */

    public HoraryBandContract findValidHoraryBand4Date(HoraryBandContract horaryBandContract, Calendar controlDayOfMonth) {
        // if the HoraryBand endDate is null, means that the band is valid still the last day of the month
        if (horaryBandContract.getEndDate() == null) {
            // if the controlDayOfMonth is greater than or equal to initDate of the HoraryBand. So the band is valid
            if (controlDayOfMonth.compareTo(DateUtils.toCalendar(horaryBandContract.getInitDate())) >= 0) {
                return horaryBandContract;
            }
        } else {
            // In this case end and init values are calculated.
            // if the controlDayOfMonth is greater than or equal to initDate of the HoraryBand. So the band is valid
            if ((controlDayOfMonth.compareTo(DateUtils.toCalendar(horaryBandContract.getInitDate())) >= 0) &&
                    (controlDayOfMonth.compareTo(DateUtils.toCalendar(horaryBandContract.getEndDate())) <= 0)) {
                return horaryBandContract;
            }
        }
        return null;
    }

    // returns a map where the key is the day as a integer and the value is a list of HoraryBand . monday=1

    public Hashtable<Integer, List<HoraryBandContract>> getHoraryBandContractMapByDay(List<HoraryBandContract> horaryBandContractList) {
        Hashtable<Integer, List<HoraryBandContract>> horaryBandContractMapByDay = new Hashtable<Integer, List<HoraryBandContract>>();
//        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
        List<HoraryBandContract> mondayHoraryBandContractList = new ArrayList<HoraryBandContract>();
        List<HoraryBandContract> tuesdayHoraryBandContractList = new ArrayList<HoraryBandContract>();
        List<HoraryBandContract> wednesdayHoraryBandContractList = new ArrayList<HoraryBandContract>();
        List<HoraryBandContract> thursdayHoraryBandContractList = new ArrayList<HoraryBandContract>();
        List<HoraryBandContract> fridayHoraryBandContractList = new ArrayList<HoraryBandContract>();
        List<HoraryBandContract> saturdayHoraryBandContractList = new ArrayList<HoraryBandContract>();

        for (HoraryBandContract aHoraryBandContractList : horaryBandContractList) {
            HoraryBandContract horaryBandContract = em.find(HoraryBandContract.class, aHoraryBandContractList.getId());
            HoraryBand horaryBand = horaryBandContract.getHoraryBand();
            // a HoraryBand may cover 1 or more days so we save a list
            String initDay = horaryBand.getInitDay().toUpperCase();
            String endDay = horaryBand.getEndDay().toUpperCase();
            Integer initDayInt = DayMap.dayStringToInt(initDay);
            Integer endDayInt = DayMap.dayStringToInt(endDay);
            // iterate each day of the period (1 or more days)
            for (int j = initDayInt; j <= endDayInt; j++) {
                if ("LUNES".equalsIgnoreCase(DayMap.dayIntToString(j))) {
                    mondayHoraryBandContractList.add(horaryBandContract);
                }
                if ("MARTES".equalsIgnoreCase(DayMap.dayIntToString(j))) {
                    tuesdayHoraryBandContractList.add(horaryBandContract);
                }
                if ("MIERCOLES".equalsIgnoreCase(DayMap.dayIntToString(j))) {
                    wednesdayHoraryBandContractList.add(horaryBandContract);
                }
                if ("JUEVES".equalsIgnoreCase(DayMap.dayIntToString(j))) {
                    thursdayHoraryBandContractList.add(horaryBandContract);
                }
                if ("VIERNES".equalsIgnoreCase(DayMap.dayIntToString(j))) {
                    fridayHoraryBandContractList.add(horaryBandContract);
                }
                if ("SABADO".equalsIgnoreCase(DayMap.dayIntToString(j))) {
                    saturdayHoraryBandContractList.add(horaryBandContract);
                }
            }
        }
        horaryBandContractMapByDay.put(2, mondayHoraryBandContractList);
        horaryBandContractMapByDay.put(3, tuesdayHoraryBandContractList);
        horaryBandContractMapByDay.put(4, wednesdayHoraryBandContractList);
        horaryBandContractMapByDay.put(5, thursdayHoraryBandContractList);
        horaryBandContractMapByDay.put(6, fridayHoraryBandContractList);
        horaryBandContractMapByDay.put(7, saturdayHoraryBandContractList);
        return horaryBandContractMapByDay;
    }


    public List<HoraryBandContract> filterBandsByContract(Contract contract, List<HoraryBandContract> hourlyBandContract4EmployeeList) {
        List<HoraryBandContract> returnList = new ArrayList<HoraryBandContract>();
        for (HoraryBandContract horaryBandContract : hourlyBandContract4EmployeeList) {
            if (horaryBandContract.getJobContract().getContract().equals(contract)) {
                returnList.add(horaryBandContract);
            }
        }
        return returnList;
    }

    public List<HoraryBandContract> cleanDuplicate(List<HoraryBandContract> horaryBandContractList) {
        Map<String, HoraryBandContract> hbcMap = new HashMap<String, HoraryBandContract>();

        for (HoraryBandContract horaryBandContract : horaryBandContractList) {
            String keyMap = horaryBandContract.getInitDate().getTime() + "_" +
                    horaryBandContract.getEndDate().getTime() + "_" +
                    horaryBandContract.getHoraryBand().getInitDay() + "_" +
                    horaryBandContract.getHoraryBand().getEndDay() + "_" +
                    horaryBandContract.getHoraryBand().getInitHour().getTime() + "_" +
                    horaryBandContract.getHoraryBand().getEndHour().getTime() + "_" +
                    horaryBandContract.getHoraryBand().getDuration() + "_" +
                    horaryBandContract.getHoraryBand().getEveryOtherDay();
            if (!hbcMap.containsKey(keyMap)) {
                hbcMap.put(keyMap, horaryBandContract);
            }
        }

        return new ArrayList<HoraryBandContract>(hbcMap.values());
    }

    public HoraryBandContract getHoraryBandContractByAcademicSchedule(Long academicSchedule) {
        HoraryBandContract result = null;
        try {
            result = (HoraryBandContract) em.createNamedQuery("HoraryBandContract.findHoraryBandContractByAcademicSchedule").setParameter("academicSchedule", academicSchedule).getSingleResult();
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * find HoraryBandContracts by job category and active or inactive
     *
     * @param jobContract job contract
     * @param isActive    true or false
     * @return List
     */
    public List<HoraryBandContract> getHoraryBandContractsByJobContractActive(JobContract jobContract, Boolean isActive) {
        log.debug("Excecuting getHoraryBandContractsByJobContractActive method.....");
        return em.createNamedQuery("HoraryBandContract.findByJobContractActive")
                .setParameter("jobContract", jobContract)
                .setParameter("isActive", isActive)
                .getResultList();
    }

}
