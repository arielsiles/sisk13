package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.employees.GestionPayrollOfficialPayrollDeadlineException;
import com.encens.khipus.exception.employees.PayrollSelectItemsEmptyException;
import com.encens.khipus.exception.employees.PayrollSelectItemsHasAccountingRecordException;
import com.encens.khipus.exception.employees.UpdateActivePaymentException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.RotatoryFundCollection;
import com.encens.khipus.util.employees.PayrollGenerationResult;

import javax.ejb.Local;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service for GeneratedPayroll
 *
 * @author
 * @version 2.18
 */
@Local
public interface GeneratedPayrollService {

    List<GeneratedPayroll> findGeneratedPayrollByName(GeneratedPayroll generatedPayroll);

    Long countGeneratedPayrollByName(GeneratedPayroll generatedPayroll);

    List<GeneratedPayroll> findGeneratedPayrollByGestionPayroll(GestionPayroll gestionPayroll, GeneratedPayrollType generatedPayrollType);

    Long countGeneratedPayrollByGestionPayroll(GestionPayroll gestionPayroll, GeneratedPayrollType generatedPayrollType);

    List<GeneratedPayroll> findGeneratedPayrollsByGestionPayroll(GestionPayroll gestionPayroll);

    PayrollGenerationResult fillPayroll(GeneratedPayroll generatedPayroll) throws Exception;

    PayrollGenerationResult fillProffesorsPayroll(GeneratedPayroll generatedPayroll, List<Employee> employeeList, List<Date> specialDate4BusinessUnit, Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit, Map<Long, List<Date>> specialDate4OrganizationalUnit, Map<Long, Map<Date, List<TimeInterval>>> specialDateTimeForOrganizationalUnit, List<RotatoryFundCollection> newRotatoryFundCollectionList);

    void deleteGeneratedPayroll(Long generatedPayrollId);

    List<HoraryBandContract> filterBandsByContract(Contract contract, List<HoraryBandContract> hourlyBandContract4EmployeeList);

    List<HoraryBandContract> cleanDuplicate(List<HoraryBandContract> horaryBandContractList);

    List<GeneratedPayroll> findGeneratedPayrollsByGestion(Gestion gestion);

    Boolean hasNegativeAmount(GeneratedPayroll generatedPayroll);

    List<GeneratedPayroll> findValidGeneratedPayrollsByGestionAndMount(Gestion gestion, Month month);

    Long countValidGeneratedPayrollsByGestionAndMounth(Gestion gestion, Month month);

    Boolean haveBankAccounts(GeneratedPayroll generatedPayroll);

    void update(GeneratedPayroll generatedPayroll)
            throws ConcurrencyException, EntryDuplicatedException,
            GeneratedPayrollHasNegativeAmountException, EmployeeMissingBankAccountException,
            AlreadyExistsAnOfficialGeneratedPayrollException, CannotChangeToOutdatedGeneratedPayrollTypeException,
            CannotChangeFromOutdatedGeneratedPayrollTypeException, CannotChangeFromOfficialToTestGeneratedPayrollTypeException, QuotaInfoOutdatedException, GestionPayrollOfficialPayrollDeadlineException;

    Boolean validateHasAccountingRecord(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll, List<Long> selectIdList);

    Boolean validateHasAccountingRecordOrHasInactivePayment(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll, List<Long> selectIdList);

    List<Integer> updateActivePaymentToPayrollItems(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll, List<Long> selectIdList)
            throws PayrollSelectItemsEmptyException,
            PayrollSelectItemsHasAccountingRecordException, UpdateActivePaymentException;

    List<Long> getSelectIdList(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll);

    List<GenericPayroll> getSelectItemList(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll);

    Map<Long, FinancesBankAccount> getFinancesBankAccountMapByPayroll(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll) throws CompanyConfigurationNotFoundException;

    List<Employee> getPayPayrollEmployeeList(Class<? extends GenericPayroll> entityClass, List<Long> idList);

    @SuppressWarnings({"unchecked"})
    List<Long> getPayPayrollEmployeeIdList(Class<? extends GenericPayroll> entityClass, List<Long> idList);

    Boolean hasGeneratedPayrolls(GestionPayroll gestionPayroll);

    int[] calculateGeneratedPayrolls(int year, String month, Integer executorUnitId);

    List<Long> getDifferenceInHoursMinutesSecondsBetweenMarks(
            Calendar markOne, Calendar markTwo);

    List<Date> findInitEndRHMarks(List<Date> dateRhMarkList, HoraryBandContract dayHoraryBandContract, Calendar dayOfMonthCalendar);

    boolean isMarkInToleranceRange(Integer before, Integer after, Calendar hourlyBand, Calendar employeeMark);

    List<Long> getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
            Calendar bandHour, Calendar markHour);

    /**
     * Finds which marks can be associated to a band
     *
     * @param dateRhMarkList        a given list of marks
     * @param dayHoraryBandContract a given HoraryBandContract
     * @return a list of marks which can be associated to a band
     */
    List<Date> findAssociatedRHMarks(List<Date> dateRhMarkList, HoraryBandContract dayHoraryBandContract);

    PayrollGenerationResult fillChristmasPayroll(GeneratedPayroll generatedPayroll) throws Exception;

    List<GeneratedPayroll> findOfficialGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType(GestionPayroll gestionPayroll);

    /**
     * @param generatedPayroll               a GeneralPayroll that groups all ManagersPayroll entries
     * @param employeeList                   a list of employees to process
     * @param specialDate4BusinessUnit       a list of dates where the employee have permission
     * @param specialDateTime4BusinessUnit   a map of list of dates and time intervals where the employee have permission
     * @param specialDate4OrganizationalUnit a map that contains a list of special dates by OrganizationalUnit
     * @param specialDateTimeForOrganizationalUnit
     *                                       a map that contains a list of special dates by time by OrganizationalUnit
     * @param newRotatoryFundCollectionList  a list of Rotatory Fund Collections
     * @return PayrollGenerationResult for Managers
     */
    PayrollGenerationResult fillManagersPayroll(GeneratedPayroll generatedPayroll, List<Employee> employeeList,
                                                List<Date> specialDate4BusinessUnit,
                                                Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                Map<Long, List<Date>> specialDate4OrganizationalUnit,
                                                Map<Long, Map<Date, List<TimeInterval>>> specialDateTimeForOrganizationalUnit,
                                                List<RotatoryFundCollection> newRotatoryFundCollectionList);

    Long countOfficialGeneratedPayrollByGestionPayrollParameters(GestionPayroll gestionPayroll);

    List<ManagersPayroll> getManagersPayrollList(GeneratedPayroll generatedPayroll);

    List<GeneralPayroll> getGeneralPayrollList(GeneratedPayroll generatedPayroll);

    List<ManagersPayroll> loadManagersPayrollList(List<Long> idList);

    List<GeneralPayroll> loadGeneralPayrollList(List<Long> idList);

    List<ChristmasPayroll> loadChristmasPayrollList(List<Long> idList);

    @SuppressWarnings({"unchecked"})
    List<FiscalProfessorPayroll> loadFiscalProfessorPayrollList(List<Long> idList);
}
