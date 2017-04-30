package com.encens.khipus.initialize.service;

import com.encens.khipus.initialize.timer.CustomQuartzProcessorSync;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.*;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.ExecutionTimeUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.PersistenceException;
import javax.transaction.*;
import java.util.*;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name(MarkProcessorServiceBean.SERVICE_SEAM_NAME)
@TransactionManagement(TransactionManagementType.BEAN)
public class MarkProcessorServiceBean extends CustomQuartzProcessorServiceBean implements CustomQuartzProcessorService {
    public static final int INT_ONE_NEGATIVE = -1;
    public static final String SERVICE_SEAM_NAME = "markProcessorService";
    private static final int DELAY_DAYS = 0;
    @Resource
    private UserTransaction userTransaction;

    @In
    private EmployeeService employeeService;
    @In
    private HoraryBandContractService horaryBandContractService;
    @In
    private GeneratedPayrollService generatedPayrollService;
    @In
    private HoraryBandStateService horaryBandStateService;
    @In
    private MarkStateHoraryBandStateService markStateHoraryBandStateService;

    @In
    private MarkStateService markStateService;
    @In
    private RHMarkService rhMarkService;

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void execute() {
        ExecutionTimeUtil executionTimeUtil = new ExecutionTimeUtil();
        executionTimeUtil.startExecution();
        try {
            userTransaction.setTransactionTimeout(600);
            userTransaction.begin();
            // marks from Mark that have not been processed yet
            List<RHMark> markList;
            // find out init and end date ranges
            Calendar quartzInitDateTime = Calendar.getInstance();
            //todo must be -1
            quartzInitDateTime.add(Calendar.DAY_OF_MONTH, -DELAY_DAYS);
            quartzInitDateTime.set(Calendar.HOUR_OF_DAY, 23);
            quartzInitDateTime.set(Calendar.MINUTE, 59);
            quartzInitDateTime.set(Calendar.SECOND, 59);
            quartzInitDateTime.set(Calendar.MILLISECOND, 59);

            // set init range as the zero hours of the day
            // midNight is zero hours of today
            Date initRange = DateUtils.removeTime(quartzInitDateTime.getTime());

            // end range is one millisecond before tomorrow
            Calendar endRange = DateUtils.toCalendar(initRange);
            endRange.add(Calendar.HOUR, 23);
            endRange.add(Calendar.MINUTE, 59);
            endRange.add(Calendar.SECOND, 59);
            endRange.add(Calendar.MILLISECOND, 59);

            // holds a map of <marRefCard,List<Marks>> in order to group by marRefCard
            HashMap<String, List<RHMark>> marRefCardMarkHashMap = new HashMap<String, List<RHMark>>();
            HashMap<Employee, List<RHMark>> employeeMarkHashMap = new HashMap<Employee, List<RHMark>>();

            markList = rhMarkService.findRHMarkByInitDateByEndDate(initRange, quartzInitDateTime.getTime());
            // load unprocessedMarkList in the marRefCardMarkHashMap
            loadMarksIntoMarRefCardMarkMap(markList, marRefCardMarkHashMap);


            // load marks by employee and process unregistered employee marks
            loadEmployeeMarkMap(marRefCardMarkHashMap, employeeMarkHashMap);


            userTransaction.commit();
            userTransaction.setTransactionTimeout(0);

            Integer pageSize = 50;
            Integer firstResult = 0;
            Boolean existsElements = true;
            while (existsElements) {
                userTransaction.setTransactionTimeout(600 * pageSize);
                userTransaction.begin();
                List<Employee> employeeList = employeeService.findEmployeesForMarkAndHoraryBandProcessByDateRange(initRange, endRange.getTime(), firstResult, pageSize + 1);

                log.debug("*********************");
                log.debug("init: " + initRange);
                log.debug("end : " + quartzInitDateTime.getTime());
                fillPayroll(employeeList, employeeMarkHashMap, DateUtils.toCalendar(initRange), quartzInitDateTime.getTime());

                if (existsElements = employeeList.size() > pageSize) {
                    employeeList = employeeList.subList(0, pageSize);
                }

                firstResult += pageSize;
                userTransaction.commit();
                userTransaction.setTransactionTimeout(0);
            }
        } catch (Exception e) {

            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
                userTransaction.setTransactionTimeout(0);
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }

        executionTimeUtil.endExecution();
        log.info("\t\t\t\tEXECUTION TIME FOR MARK PROCESSOR: ");
        log.info("\t\texecutionTimeUtil.timeInSecons(): " + executionTimeUtil.timeInSecons());
        log.info("\t\texecutionTimeUtil.timeInMillis(): " + executionTimeUtil.timeInMillis());
        CustomQuartzProcessorSync.i.end(SERVICE_SEAM_NAME);
    }

    public void fillPayroll(List<Employee> employeeList,
                            HashMap<Employee, List<RHMark>> employeeMarkHashMap,
                            Calendar initDate, Date quartzInitDateTime)
            throws NotSupportedException, SystemException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException {
        // this var is to control the days of the month. It is initially setted to the first day of the range
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(initDate.getTime());
        currentDate.set(Calendar.MILLISECOND, 0);

        // iterates each employee
        for (Employee employee : employeeList) {
            log.debug("Processing employee = " + employee.getId() + employee.getFullName());

            //find bands related to employee which aren't in HoraryBandStates that are not PENDING.
            List<HoraryBandContract> hourlyBandContract4EmployeeList =
                    horaryBandContractService.findValidHoraryBandContractForMarProcessorByEmployeeAndDateRangeAndCurrentDate(
                            employee, initDate.getTime(), initDate.getTime(), initDate.getTime(), getEntityManager());
            // bands that ends or begins before quartz time other bands will not be processed.
            // map HoraryBandContract by day for a given date
            Hashtable<Integer, List<HoraryBandContract>> horaryBandContractMapByDay = horaryBandContractService.getHoraryBandContractMapByDay(hourlyBandContract4EmployeeList);
            // HoraryBandContract list by day
            List<HoraryBandContract> validDayHoraryBand4DateList = horaryBandContractMapByDay.get(currentDate.get(Calendar.DAY_OF_WEEK));

            // clean duplicate and bands that overlap
            validDayHoraryBand4DateList = generatedPayrollService.cleanDuplicate(validDayHoraryBand4DateList);

            // holds bands where the employee haven't chance to make any valid mark according to quartzInitDateTime
            Map<HoraryBandContract, Boolean> readyHoraryBandContractMap = new HashMap<HoraryBandContract, Boolean>();

            // Loads the readyHoraryBandContractMap with bands that don't wait for more marks
            // and removes the bands that wait marks after quartzInitDateTime from the hourlyBandContract4EmployeeList
            filterAndLoadReadyAndPendingHoraryBandContracts(quartzInitDateTime, validDayHoraryBand4DateList, readyHoraryBandContractMap);

            log.debug("date been processed: " + currentDate.getTime());
            log.debug("day been processed: " + currentDate.get(Calendar.DAY_OF_WEEK));
            log.debug("all ready or pendant bands size for day: " + validDayHoraryBand4DateList.size());
            log.debug("ready HoraryBandContracts for day size: " + readyHoraryBandContractMap.size());

            List<RHMark> rhMarkList4Employee = employeeMarkHashMap.get(employee);
            if (rhMarkList4Employee == null) {
                rhMarkList4Employee = new ArrayList<RHMark>();
            }
            executeAttendanceControl(currentDate,
                    employee, validDayHoraryBand4DateList,
                    readyHoraryBandContractMap,
                    rhMarkList4Employee);
        }
    }

    private void executeAttendanceControl(Calendar currentDate,
                                          Employee employee,
                                          List<HoraryBandContract> horaryBandContractList,
                                          Map<HoraryBandContract, Boolean> readyHoraryBandContractMap,
                                          List<RHMark> rhMarkList4Employee)
            throws SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
        if (currentDate.get(Calendar.DAY_OF_WEEK) != 1) {
            for (HoraryBandContract validDayHoraryBandContract : horaryBandContractList) {
                HoraryBandState horaryBandState = horaryBandStateService.findByDateAndHoraryBand(
                        currentDate.getTime(), validDayHoraryBandContract.getHoraryBand().getId());
                // holds a list of marks associated to bands that are still in pending state
                // the MarkState associated to it have to be removed to be reprocessed
                List<MarkStateHoraryBandState> pendingMarkStateHoraryBandStateList = new ArrayList<MarkStateHoraryBandState>();

                //Loads pending marks to rhMarkList4Employee and load pending MarkStateHoraryBandState
                // into pendingMarkStateHoraryBandStateList in case horaryBandState is not null
                loadPendingMarks(rhMarkList4Employee, horaryBandState, pendingMarkStateHoraryBandStateList);

                // Removes pending MarkSateHoraryBandState and MarkState if they are not associated to other relationships

                log.debug("band init day:" + validDayHoraryBandContract.getHoraryBand().getInitDay());
                log.debug("band end day:" + validDayHoraryBandContract.getHoraryBand().getEndDay());
                log.debug("band init hour:" + validDayHoraryBandContract.getHoraryBand().getInitHour());
                log.debug("band end hour:" + validDayHoraryBandContract.getHoraryBand().getEndHour());
                log.debug("is ready band:" + readyHoraryBandContractMap.containsKey(validDayHoraryBandContract));
                int minutosAcumaladosRestraso = 0;
                int incomeCummulativeMinuteLateness = 0;
                int outcomeCummulativeMinuteLateness = 0;
                // check if the employee marked this date at this band period and retrieve his marks as a list
                List<Date> dateTimeMarkList = new ArrayList<Date>();

                // holds datetime mark format and mark
                Map<Date, RHMark> dateTimeMarkMap = new HashMap<Date, RHMark>();
                for (RHMark mark : rhMarkList4Employee) {
                    Date dateTime = DateUtils.joinDateAndTime(mark.getMarDate(), mark.getMarTime()).getTime();
                    dateTimeMarkList.add(dateTime);
                    dateTimeMarkMap.put(dateTime, mark);
                }
                // load associated marks to a list
                List<Date> associatedDateTimeMarks = generatedPayrollService.findAssociatedRHMarks(dateTimeMarkList, validDayHoraryBandContract);
                List<RHMark> associatedMarkList = new ArrayList<RHMark>();
                for (Date associatedDateTimeMark : associatedDateTimeMarks) {
                    associatedMarkList.add(dateTimeMarkMap.get(associatedDateTimeMark));
                }

                List<Date> correctMarks = generatedPayrollService.findInitEndRHMarks(dateTimeMarkList, validDayHoraryBandContract, currentDate);
                Calendar initBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract.getHoraryBand().getInitHour());
                Calendar endBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract.getHoraryBand().getEndHour());

                // if there are no valid quantity of marks for the HoraryBand It is absence.
                // check in case of lateness
                if (employee.getControlFlag() && correctMarks.size() >= 1) {
                    Calendar employeeInitMarkCalendar = DateUtils.toCalendar(correctMarks.get(0));
                    // set year and month in case marTime saves only time mark
                    employeeInitMarkCalendar.set(Calendar.YEAR, correctMarks.get(0).getYear());
                    employeeInitMarkCalendar.set(Calendar.MONTH, correctMarks.get(0).getMonth());

                    // checks if the employee out of the tolerance range at income in order to apply discounts
                    if (!generatedPayrollService.isMarkInToleranceRange(validDayHoraryBandContract.getTolerance().getBeforeInit(),
                            validDayHoraryBandContract.getTolerance().getAfterInit(), initBandHourCalendar, employeeInitMarkCalendar)) {
                        // apply discount
                        // gets the differences between employees init mark and HoraryBand init time
                        List<Long> differenceList = generatedPayrollService.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                initBandHourCalendar, employeeInitMarkCalendar);
                        Long differenceInHours = differenceList.get(0);
                        Long differenceInMinutes = differenceList.get(1);
                        // sum of hour and minutes in a day to be shown in the payroll
                        Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                        boolean isNegative = false;
                        if (cumulativeDifferenceInMinutes < 0) {
                            isNegative = true;
                        }
                        minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                        cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);
                        // in case that the tardiness is at left side of init band mark
                        int tolerance = 0;
                        if (isNegative) {
                            tolerance = validDayHoraryBandContract.getTolerance().getBeforeInit();
                        }
                        // in case that the tardiness is at right side of init band mark
                        if (!isNegative) {
                            tolerance = validDayHoraryBandContract.getTolerance().getAfterInit();
                        }
                        if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                            minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                        }
                        incomeCummulativeMinuteLateness = minutosAcumaladosRestraso;
                    }

                    // cast to calendar employees end mark
                    Calendar employeeEndMarkCalendar = DateUtils.toCalendar(correctMarks.get(1));
                    // set year and month in case marTime saves only time mark
                    employeeEndMarkCalendar.set(Calendar.YEAR, correctMarks.get(1).getYear());
                    employeeEndMarkCalendar.set(Calendar.MONTH, correctMarks.get(1).getMonth());

                    // discount in outcome
                    if (!generatedPayrollService.isMarkInToleranceRange(validDayHoraryBandContract.getTolerance().getBeforeEnd(),
                            validDayHoraryBandContract.getTolerance().getAfterEnd(), endBandHourCalendar, employeeEndMarkCalendar)) {
                        // apply discount
                        // gets the differences between employees end mark and HoraryBand end time
                        List<Long> differenceList = generatedPayrollService.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                endBandHourCalendar, employeeEndMarkCalendar);
                        Long differenceInHours = differenceList.get(0);
                        Long differenceInMinutes = differenceList.get(1);
                        Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                        boolean isNegative = false;
                        if (cumulativeDifferenceInMinutes < 0) {
                            isNegative = true;
                        }
                        cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);

                        int tolerance = 0;
                        if (isNegative) {
                            tolerance = validDayHoraryBandContract.getTolerance().getBeforeEnd();
                        }
                        // in case that the tardiness is at right side of init band mark
                        if (!isNegative) {
                            tolerance = validDayHoraryBandContract.getTolerance().getAfterEnd();
                        }
                        if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                            minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                        }
                        outcomeCummulativeMinuteLateness = minutosAcumaladosRestraso;
                    }
                    // end if correct marks control
                }
                int cumulativeMinuteLatenessSum = incomeCummulativeMinuteLateness + outcomeCummulativeMinuteLateness;
                // register new HoraryBandState or override it
                horaryBandState = registerHoraryBandState(cumulativeMinuteLatenessSum, validDayHoraryBandContract,
                        readyHoraryBandContractMap, correctMarks, currentDate, horaryBandState);

                // register MarkState
                log.debug("correct marks size: " + correctMarks.size());
                // load a list of the correct marks
                List<RHMark> correctMarkList = new ArrayList<RHMark>();
                for (Date correctMark : correctMarks) {
                    correctMarkList.add(dateTimeMarkMap.get(correctMark));
                }

                for (Date correctMark : correctMarks) {
                    if (dateTimeMarkMap.containsKey(correctMark)) {
                        //so mark as valid mark
                        RHMark mark = dateTimeMarkMap.get(correctMark);
                        MarkState markState;
                        // create relationship between MarkState and HoraryBandState if necessary
                        MarkStateHoraryBandState markStateHoraryBandState;

                        markState = markStateService.findByMark(mark);
                        if (null == markState) {
                            markState = buildMarkState(mark, employee);
                        }
                        markStateHoraryBandState = markStateHoraryBandStateService.findByMarkStateAndHoraryBandState(markState, horaryBandState.getId(), getEntityManager());
                        if (null == markStateHoraryBandState) {
                            markStateHoraryBandState = new MarkStateHoraryBandState();
                            markStateHoraryBandState.setMarkState(markState);
                            markStateHoraryBandState.setHoraryBandState(horaryBandState);
                            markStateHoraryBandState.setCompany(markState.getCompany());
                            assignMarkStateHoraryBandStateType(
                                    horaryBandState, incomeCummulativeMinuteLateness,
                                    outcomeCummulativeMinuteLateness, correctMarks, cumulativeMinuteLatenessSum,
                                    correctMark, markStateHoraryBandState);
                            getEntityManager().persist(markStateHoraryBandState);

                        } else {
                            assignMarkStateHoraryBandStateType(
                                    horaryBandState, incomeCummulativeMinuteLateness,
                                    outcomeCummulativeMinuteLateness, correctMarks, cumulativeMinuteLatenessSum,
                                    correctMark, markStateHoraryBandState);
                            if (!getEntityManager().contains(markStateHoraryBandState)) {
                                getEntityManager().merge(markStateHoraryBandState);
                            }
                        }
                        getEntityManager().flush();
                    }
                }
                getEntityManager().flush();
                associateMarkToHoraryBand(employee, associatedMarkList, correctMarkList, horaryBandState);
                getEntityManager().flush();
            }// end if holiday control
        } // end if is not sunday
    }

    private void assignMarkStateHoraryBandStateType(
            HoraryBandState horaryBandState,
            int incomeCummulativeMinuteLateness, int outcomeCummulativeMinuteLateness,
            List<Date> correctMarks, int cumulativeMinuteLatenessSum, Date correctMark,
            MarkStateHoraryBandState markStateHoraryBandState) {
        if (horaryBandState.getType().compareTo(HoraryBandStateType.PENDING) == 0) {
            markStateHoraryBandState.setType(MarkStateType.PENDING);
        } else {
            if (cumulativeMinuteLatenessSum == 0) {
                markStateHoraryBandState.setType(MarkStateType.ON_TIME);
                markStateHoraryBandState.setMinutesDiscount(0);
            } else {
                if (incomeCummulativeMinuteLateness > 0 && correctMarks.indexOf(correctMark) == 0) {
                    markStateHoraryBandState.setType(MarkStateType.LATE);
                    markStateHoraryBandState.setMinutesDiscount(incomeCummulativeMinuteLateness);
                }
                if (outcomeCummulativeMinuteLateness > 0 && correctMarks.indexOf(correctMark) == 1) {
                    markStateHoraryBandState.setType(MarkStateType.LATE);
                    markStateHoraryBandState.setMinutesDiscount(outcomeCummulativeMinuteLateness);
                }
            }
        }
    }

    private void associateMarkToHoraryBand(Employee employee, List<RHMark> associatedMarkList, List<RHMark> correctMarkList,
                                           HoraryBandState horaryBandState) {
        // register MarkState for associated marks that are not correct marks
        for (RHMark associatedMark : associatedMarkList) {
            MarkState markState = null;
            if (!correctMarkList.contains(associatedMark)) {
                //so mark as processed mark
                markState = markStateService.findByMark(associatedMark);
                if (null == markState) {
                    markState = buildMarkState(associatedMark, employee);
                }
                if (null != markState) {
                    // create relationship between MarkState and HoraryBandState
                    MarkStateHoraryBandState markStateHoraryBandState;
                    markStateHoraryBandState = markStateHoraryBandStateService.findByMarkStateAndHoraryBandState(markState, horaryBandState.getId(), getEntityManager());
                    if (null == markStateHoraryBandState) {
                        markStateHoraryBandState = new MarkStateHoraryBandState();
                        markStateHoraryBandState.setMarkState(markState);
                        markStateHoraryBandState.setHoraryBandState(horaryBandState);
                        markStateHoraryBandState.setCompany(markState.getCompany());
                        if (horaryBandState.getType().compareTo(HoraryBandStateType.PENDING) == 0) {
                            markStateHoraryBandState.setType(MarkStateType.PENDING);
                        }
                        getEntityManager().persist(markStateHoraryBandState);
                    }
                }
            }
        }
        getEntityManager().flush();
    }

    /**
     * Loads pending marks to rhMarkList4Employee and load pending MarkStateHoraryBandState into
     * pendingMarkStateHoraryBandStateList in case horaryBandState is not null
     *
     * @param rhMarkList4Employee a given list of marks lo add pending marks
     * @param horaryBandState     a given HoraryBandState database instance
     * @param pendingMarkStateHoraryBandStateList
     *                            a List to load MarkStateHoraryBandState objects associated to pending marks
     */
    private void loadPendingMarks(List<RHMark> rhMarkList4Employee, HoraryBandState horaryBandState, List<MarkStateHoraryBandState> pendingMarkStateHoraryBandStateList) {
        if (null != horaryBandState) {
            // load marks associated
            for (MarkStateHoraryBandState markStateHoraryBandState : horaryBandState.getMarkStateHoraryBandStateList()) {
                RHMark pendingMark = markStateHoraryBandState.getMarkState().getMark();
                rhMarkList4Employee.add(pendingMark);
                if (!pendingMarkStateHoraryBandStateList.contains(markStateHoraryBandState)) {
                    pendingMarkStateHoraryBandStateList.add(markStateHoraryBandState);
                }
            }
        }
    }

    private HoraryBandState registerHoraryBandState(
            int minutosAcumaladosRestraso, HoraryBandContract validDayHoraryBandContract,
            Map<HoraryBandContract, Boolean> readyHoraryBandContractMap,
            List<Date> correctMarks, Calendar currentDate, HoraryBandState horaryBandState) {
        // cast to calendar employees end mark
        Calendar endBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract.getHoraryBand().getEndHour());
        Calendar upperRange = Calendar.getInstance();
        upperRange.set(endBandHourCalendar.get(Calendar.YEAR), endBandHourCalendar.get(Calendar.MONTH), endBandHourCalendar.get(Calendar.DAY_OF_MONTH),
                endBandHourCalendar.get(Calendar.HOUR_OF_DAY), endBandHourCalendar.get(Calendar.MINUTE), endBandHourCalendar.get(Calendar.SECOND));
        upperRange.add(Calendar.MINUTE, validDayHoraryBandContract.getTolerance().getAfterEnd());

        if (null == horaryBandState) {
            horaryBandState = buildHoraryBandState(validDayHoraryBandContract, currentDate);
        }

        horaryBandState.setMinutesDiscount(minutosAcumaladosRestraso);
        if (!validDayHoraryBandContract.getJobContract().getContract().getEmployee().getControlFlag()) {
            horaryBandState.setType(HoraryBandStateType.ON_TIME);
        } else {
            if (correctMarks.size() >= 1) {
                if (minutosAcumaladosRestraso == 0) {
                    horaryBandState.setType(HoraryBandStateType.ON_TIME);
                } else {
                    if (null == horaryBandState.getType() || horaryBandState.getType().compareTo(HoraryBandStateType.ON_TIME) != 0) {
                        if (readyHoraryBandContractMap.containsKey(validDayHoraryBandContract)) {
                            horaryBandState.setType(HoraryBandStateType.LATE);
                        } else {
                            horaryBandState.setType(HoraryBandStateType.PENDING);
                        }
                    }
                }
            } else {
                if (!readyHoraryBandContractMap.containsKey(validDayHoraryBandContract)) {
                    horaryBandState.setType(HoraryBandStateType.PENDING);
                } else {
                    // mark band as an absence if it is not registered yet or
                    // if its state isn't HoraryBandStateType.ON_TIME nor HoraryBandStateType.LATE
                    if (null == horaryBandState.getType() ||
                            (horaryBandState.getType().compareTo(HoraryBandStateType.ON_TIME) != 0
                                    && horaryBandState.getType().compareTo(HoraryBandStateType.LATE) != 0)) {
                        horaryBandState.setType(HoraryBandStateType.MISSING);
                    }
                }
            }
        }
        if (null == horaryBandState.getId()) {
            getEntityManager().persist(horaryBandState);
        }
        // todo find out why here merge method cause optimistic lock exception
        /*else {
            if (!getEntityManager().contains(horaryBandState)) {
                horaryBandState = getEntityManager().merge(horaryBandState);
            }
        }*/
        getEntityManager().flush();
        return horaryBandState;
    }

    private HoraryBandState buildHoraryBandState(HoraryBandContract validDayHoraryBandContract, Calendar currentDate) {
        HoraryBandState horaryBandState = new HoraryBandState();
        horaryBandState.setCompany(validDayHoraryBandContract.getCompany());
        horaryBandState.setAfterEnd(validDayHoraryBandContract.getTolerance().getAfterEnd());
        horaryBandState.setAfterInit(validDayHoraryBandContract.getTolerance().getAfterInit());
        horaryBandState.setBeforeEnd(validDayHoraryBandContract.getTolerance().getBeforeEnd());
        horaryBandState.setBeforeInit(validDayHoraryBandContract.getTolerance().getBeforeInit());
        horaryBandState.setOrganizationalUnit(validDayHoraryBandContract.getJobContract().getJob().getOrganizationalUnit());
        horaryBandState.setBusinessUnit(validDayHoraryBandContract.getJobContract().getJob().getOrganizationalUnit().getBusinessUnit());
        horaryBandState.setCostCenter(validDayHoraryBandContract.getJobContract().getJob().getOrganizationalUnit().getCostCenter());
        horaryBandState.setDate(currentDate.getTime());
        horaryBandState.setDuration(validDayHoraryBandContract.getHoraryBand().getDuration());
        horaryBandState.setEmployee(validDayHoraryBandContract.getJobContract().getContract().getEmployee());
        horaryBandState.setEndHour(validDayHoraryBandContract.getHoraryBand().getEndHour());
        horaryBandState.setInitHour(validDayHoraryBandContract.getHoraryBand().getInitHour());
        horaryBandState.setHoraryBand(validDayHoraryBandContract.getHoraryBand());
        horaryBandState.setHoraryBandContract(validDayHoraryBandContract);
        return horaryBandState;
    }

    /**
     * This marks will be processed as unregistered MarkStateType.NOT_REGISTERED
     *
     * @param marRefCardMarkHashMap a map that holds all the markRefCard and Mark entries
     * @param marRefCard            a String that identifies who make a Mark
     */
    @SuppressWarnings({"NullableProblems"})
    private void processUnregisteredMarks(HashMap<String, List<RHMark>> marRefCardMarkHashMap, String marRefCard) {
        for (RHMark mark : marRefCardMarkHashMap.get(marRefCard)) {
            buildMarkState(mark, null);
        }
    }

    private MarkState buildMarkState(RHMark mark, Employee employee) {
        MarkState markState = new MarkState();
        markState.setMark(mark);
        markState.setMarTime(mark.getMarTime());
        markState.setMarDate(mark.getMarDate());
        markState.setMarkCode(mark.getMarRefCard());
        if (null != employee) {
            markState.setIdentified(true);
            markState.setEmployee(employee);
        } else {
            markState.setIdentified(false);
        }
        markState.setCompany(mark.getCompany());
        try {
            getEntityManager().persist(markState);
            getEntityManager().flush();
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new RuntimeException(e.getCause());
        }
        return markState;
    }

    /**
     * Load the employeeMarkHashMap according if an employee can be identified given a markRefCard code
     * and process unregistred employee marks as unregistered
     *
     * @param marRefCardMarkHashMap map that holds Marks by markRefCard that is the mark code
     * @param employeeMarkHashMap   map to load all the marks if the employee have been identified
     */
    private void loadEmployeeMarkMap(HashMap<String, List<RHMark>> marRefCardMarkHashMap, HashMap<Employee, List<RHMark>> employeeMarkHashMap) {
        for (String marRefCard : marRefCardMarkHashMap.keySet()) {
            //find employee and discard not employee marks
            List<Employee> employeeList = employeeService.getEmployeesByMarkCode(marRefCard);
            Employee employee = null;
            // the marRefCard should be unique but there is no constraint defined
            if (!ValidatorUtil.isEmptyOrNull(employeeList)) {
                employee = employeeList.get(0);
            }
            // if the employee has been found its marks will be processed
            if (null != employee) {
                employeeMarkHashMap.put(employee, marRefCardMarkHashMap.get(marRefCard));
            } else {
                processUnregisteredMarks(marRefCardMarkHashMap, marRefCard);
            }
        }
    }

    /**
     * Loads a list of Marks into the given map marRefCardMarkHashMap according or grouped by markRefCard code
     *
     * @param unprocessedMarkList   a given list of Marks not yet processed
     * @param marRefCardMarkHashMap a given map to hold marks by markRefCard code
     */
    private void loadMarksIntoMarRefCardMarkMap(List<RHMark> unprocessedMarkList, HashMap<String, List<RHMark>> marRefCardMarkHashMap) {
        // load unprocessedMarkList in the marRefCardMarkHashMap
        for (RHMark mark : unprocessedMarkList) {
            if (marRefCardMarkHashMap.containsKey(mark.getMarRefCard())) {
                marRefCardMarkHashMap.get(mark.getMarRefCard())
                        .add(mark);
            } else {
                List<RHMark> newMarkList = new ArrayList<RHMark>();
                newMarkList.add(mark);
                marRefCardMarkHashMap.put(mark.getMarRefCard(), newMarkList);
            }
        }
    }

    /**
     * Loads the readyHoraryBandContractMap with bands that don't wait for more marks
     * and removes the bands that wait marks after quartzInitDateTime from the hourlyBandContract4EmployeeList
     *
     * @param quartzInitDateTime         a given init time of the quartz
     * @param hourlyBandContract4EmployeeList
     *                                   a given list that contains all the valid HoraryBandContract for a day
     *                                   where the the bands that wait marks after quartzInitDateTime will be removed of it
     * @param readyHoraryBandContractMap a map to load the ready HoraryBandContract
     */
    private void filterAndLoadReadyAndPendingHoraryBandContracts(
            Date quartzInitDateTime,
            List<HoraryBandContract> hourlyBandContract4EmployeeList,
            Map<HoraryBandContract, Boolean> readyHoraryBandContractMap) {
        List<HoraryBandContract> skipHoraryBandContractList = new ArrayList<HoraryBandContract>();
        for (HoraryBandContract horaryBandContract : hourlyBandContract4EmployeeList) {

            Calendar bandRightLimit = DateUtils.toCalendar(horaryBandContract.getHoraryBand().getEndHour());
            // give the same date format to compare time
            bandRightLimit.set(Calendar.YEAR, quartzInitDateTime.getYear() + 1900);
            bandRightLimit.set(Calendar.MONTH, quartzInitDateTime.getMonth());
            bandRightLimit.set(Calendar.DAY_OF_MONTH, quartzInitDateTime.getDate());
            Calendar bandLeftLimit = DateUtils.toCalendar(horaryBandContract.getHoraryBand().getInitHour());
            // give the same date format to compare time
            bandLeftLimit.set(Calendar.YEAR, quartzInitDateTime.getYear() + 1900);
            bandLeftLimit.set(Calendar.MONTH, quartzInitDateTime.getMonth());
            bandLeftLimit.set(Calendar.DAY_OF_MONTH, quartzInitDateTime.getDate());

            bandRightLimit.add(Calendar.MINUTE, horaryBandContract.getLimit().getAfterEnd());
            bandLeftLimit.add(Calendar.MINUTE, (horaryBandContract.getLimit().getBeforeInit() * INT_ONE_NEGATIVE));
            if (bandLeftLimit.getTime().compareTo(quartzInitDateTime) <= 0) {
                if (bandRightLimit.getTime().compareTo(quartzInitDateTime) <= 0) {
                    readyHoraryBandContractMap.put(horaryBandContract, true);
                }
            } else {
                //add to skipHoraryBandContractList to remove later
                skipHoraryBandContractList.add(horaryBandContract);
            }
        }
        // remove HoraryBandContract to skip
        hourlyBandContract4EmployeeList.removeAll(skipHoraryBandContractList);
    }
}
