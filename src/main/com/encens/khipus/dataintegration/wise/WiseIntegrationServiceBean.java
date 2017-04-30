package com.encens.khipus.dataintegration.wise;

import com.encens.khipus.dataintegration.configuration.structure.IntegrationElement;
import com.encens.khipus.dataintegration.service.DataIntegrationServiceBean;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.academics.AcademicSubjectGroupPK;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.customers.DocumentType;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.employees.*;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.2.10
 */
@Stateless
@Name("wiseIntegrationService")
@TransactionManagement(javax.ejb.TransactionManagementType.BEAN)
public class WiseIntegrationServiceBean extends DataIntegrationServiceBean implements WiseIntegrationService {

    private static final int SCALE = 6;
    private static final int SISTEMA = 3;
    private static final long OCCUPATIONAL_CONTRACT_MODE = (long) 1;
    private static final long CIVIL_CONTRACT_MODE = (long) 2;
    private static final long NOT_SIGNED_CONTRACT_STATE = (long) 2;
    private static final String OCCUPATIONAL_FLAG_VALUE = "CL";
    private static final String DOCUMENT_COLUMN = "DOCUMENTO";
    private static final String EMPLOYEE_COLUMN = "EMPLEADO";
    private static final String LAST_NAME_COLUMN = "APELLIDO_PATERNO";
    private static final String MAIDEN_NAME_COLUMN = "APELLIDO_MATERNO";
    private static final String NAME_COLUMN = "NOMBRES";
    private static final String GENDER_COLUMN = "SEXO";
    private static final String SUBJECT_COLUMN = "ASIGNATURA";
    private static final String CURRICULUM_COLUMN = "PLAN_ESTUDIO";
    private static final String CYCLE_COLUMN = "GESTION";
    private static final String GROUP_TYPE_COLUMN = "TIPO_GRUPO";
    private static final String PERIOD_COLUMN = "PERIODO";
    private static final String SUBJECT_GROUP_COLUMN = "GRUPO_ASIGNATURA";
    private static final String REAL_COST_COLUMN = "COSTO_REAL";
    private static final String HORARY_COLUMN = "HORARIO";
    private static final BigDecimal DEFAULT_PERIOD_COST = new BigDecimal("3.59");
    private static final double MONTHS_PER_YEAR = 12d;
    private static final int DAYS_PER_YEAR = 360;
    private static final String OCCUPATIONAL_JOB_CATEGORY_ACRONYM = "DLH";
    private static final String HORARY_END_DATE = "FECHA_FIN_HR";
    private static final String HORARY_INIT_DATE = "FECHA_INICIO_HR";
    private static final int MINUTES_PER_PERIOD = 45;
    private static final String HOME_ADDRESS_COLUMN = "DOMICILIO";
    private static final String MARITAL_STATUS = "ESTADO_CIVIL";

    @In("#{entityManager}")
    private EntityManager em;

    @In
    private EmployeeService employeeService;
    @In
    private OrganizationalUnitService organizationalUnitService;
    @In
    private HoraryBandContractService horaryBandContractService;
    @In
    private JobContractService jobContractService;
    @In
    private ContractService contractService;
    @In
    private LimitService limitService;
    @In
    private ToleranceService toleranceService;
    @In
    private CurrencyService currencyService;
    @In
    private ContractModeService contractModeService;
    @In
    private ContractStateService contractStateService;
    @In
    private GestionService gestionService;
    @In
    private CycleTypeService cycleTypeService;
    @In
    private CycleService cycleService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private SalutationService salutationService;
    @In
    private MaritalStatusService maritalStatusService;

    @Resource
    protected UserTransaction userTransaction;

    private List<Long> modifiedContractList = new ArrayList<Long>();
    private List<Long> updatePivotJobContractList = new ArrayList<Long>();

    @Logger
    private Log log;

    @Override
    public void executeIntegration(String localDataSource, IntegrationElement integrationElement) {
        Contexts.getSessionContext().set("currentCompany", new Company(Constants.defaultCompanyId, Constants.defaultCompanyName));
        super.executeIntegration(localDataSource, integrationElement);
    }

    @Override
    public void customOperations(UserTransaction userTransaction, IntegrationElement integrationElement, Map<String, String> row) throws CompanyConfigurationNotFoundException {
        log.debug("Integrate data from WISE application.");

        Employee employee = employeeService.getEmployeeByCode(row.get(EMPLOYEE_COLUMN));
        OrganizationalUnit career = organizationalUnitService.getOrganizationalUnitByCareer(row.get(CURRICULUM_COLUMN));
        HoraryBandContract horaryBandContract = horaryBandContractService.getHoraryBandContractByAcademicSchedule(Long.parseLong(row.get(HORARY_COLUMN)));
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        AcademicSubjectGroupPK academicSubjectGroupPk = createAcademicSubjectGroupPk(row);

        BigDecimal cost = BigDecimalUtil.toBigDecimal(row.get(REAL_COST_COLUMN), SCALE);
        Boolean occupational = isOccupational(row.get("TIPO_CONTRATO"));

        JobContract jobContract;
        Contract contract;
        Gestion gestionHRKHIPUS = gestionService.getGestion(Integer.parseInt(row.get(CYCLE_COLUMN)));
        CycleType periodoHRKHIPUS = cycleTypeService.getCycleType(Integer.parseInt(row.get(PERIOD_COLUMN)));
        Cycle cycle = cycleService.findActiveCycle(gestionHRKHIPUS, periodoHRKHIPUS);
        Date cycleInitDate = convertStringToDate(row.get("FECHA_INI_CICLO_HR"));
        Date cycleEndDate = convertStringToDate(row.get("FECHA_FIN_CICLO_HR"));
        String day = getLiteralDay(row.get("DIA"));
        Date initHour = convertHourToDate(row.get("HORA_PERIODO_INICIAL"));
        Date endHour = convertHourToDate(row.get("HORA_PERIODO_FINAL"));
        String building = row.get("EDIFICIO");
        String classroom = row.get("AMBIENTE");
        JobContract movedJobContract = null;

        if (horaryBandContract != null) {//If the horary have been found
            Employee bandEmployee = horaryBandContract.getJobContract().getContract().getEmployee();
            log.debug("Employee being processed: " + bandEmployee.getEmployeeCode() + " - " + bandEmployee.getIdNumber() + " - " + bandEmployee.getFullName());
            log.debug("Horary Band: " + horaryBandContract.getHoraryBand().getInitHour() + " - " + horaryBandContract.getHoraryBand().getEndHour());
            jobContract = horaryBandContract.getJobContract();
            contract = jobContract.getContract();
            //if day, hour, building or classroom has changed band should be moved to another matching or new JobContract
            boolean shouldBandBeMoved = occupational && shouldBandBeMoved(horaryBandContract, day, building, classroom);
            if (shouldBandBeMoved) {
                //move band to another jobContract
                movedJobContract = horaryBandContract.getJobContract();
                List<JobContract> jobContractList = jobContractService.getJobContractList(employee);
                jobContract = matchJobContract(gestionHRKHIPUS, jobContractList, periodoHRKHIPUS, occupational, day,
                        initHour, endHour, building, classroom, career);
                if (null != jobContract) {
                    // change to corresponding JobContract
                    horaryBandContract.setJobContract(jobContract);
                    jobContract.getHoraryBandContractList().add(horaryBandContract);
                } else {
                    // If there is not previous contracts or if there is previous contracts but is occupational
                    Salary newSalary = createSalary(occupational, cost, row, cycle, companyConfiguration);
                    em.persist(newSalary);

                    Job newJob = createJob(career, newSalary, companyConfiguration.getDefaultProfessorsCharge(), companyConfiguration, occupational);
                    em.persist(newJob);

                    jobContract = createJobContract(occupational, newJob, contract, cost, academicSubjectGroupPk);
                    em.persist(jobContract);

                    newJob.getJobContractList().add(jobContract);
                    contract.getJobContractList().remove(movedJobContract);
                    movedJobContract.getHoraryBandContractList().remove(horaryBandContract);
                    contract.getJobContractList().add(jobContract);
                    horaryBandContract.setJobContract(jobContract);
                    jobContract.getHoraryBandContractList().add(horaryBandContract);
                    jobContract.setCostPivotHoraryBandContract(horaryBandContract);
                    em.flush();
                }
                removeMovedJobContract(occupational, movedJobContract, horaryBandContract);
            }
            updateHoraryBandContract(row, horaryBandContract, cost, day, initHour, endHour, building, classroom);
            em.flush();
        } else { //If it has no a horary
            HoraryBandContract newHoraryBandContract = createHoraryBandContract(row, cost, day, building, classroom);

            List<JobContract> jobContractList = employee != null ? jobContractService.getJobContractList(employee) : new ArrayList<JobContract>();
            jobContract = matchJobContract(gestionHRKHIPUS, jobContractList, periodoHRKHIPUS, occupational, day, initHour, endHour, building, classroom, career);
            boolean updateNewHoraryBandPivot = false;
            if (null != jobContract) {
                // assign to corresponding JobContract
                contract = jobContract.getContract();
                newHoraryBandContract.setJobContract(jobContract);
                jobContract.getHoraryBandContractList().add(newHoraryBandContract);
                //update pivot
                if (occupational) {
                    addUpdatePivotJobContractList(jobContract);
                }
            } else {
                if (!occupational && jobContractList.size() != 0) { //If it has previous contracts

                    JobContract previousJobContract = jobContractList.get(jobContractList.size() - 1); //Take the last contract

                    // Create a new salary with data of the last contract
                    Salary newSalary = createSalaryBasedOnPreviousJobContract(previousJobContract);
                    em.persist(newSalary);

                    // Create a new Job with last contract data, but defining the new career
                    Job newJob = createJob(career, newSalary,
                            previousJobContract.getJob().getCharge(), companyConfiguration, occupational);
                    em.persist(newJob);
                    List<Contract> contractList = contractService.getContractsByEmployeeInDateRange(employee, cycle.getStartDate(), cycle.getEndDate());
                    contract = matchContract(gestionHRKHIPUS, contractList, periodoHRKHIPUS, occupational);
                    if (null == contract) {
                        contract = createContract(
                                previousJobContract.getContract().getEmployee(), occupational,
                                cycle, cycleInitDate,
                                cycleEndDate,
                                previousJobContract.getContract().getContractMode(),
                                previousJobContract.getContract().getContractState());
                        em.persist(contract);
                    }

                    JobContract newJobContract = createJobContract(occupational, newJob, contract, cost, academicSubjectGroupPk);
                    em.persist(newJobContract);
                    contract.getJobContractList().add(newJobContract);
                    newJob.getJobContractList().add(newJobContract);

                    newHoraryBandContract.setJobContract(newJobContract);
                    newJobContract.getHoraryBandContractList().add(newHoraryBandContract);

                } else { // If there is not previous contracts or if there is previous contracts but is occupational
                    Salary newSalary = createSalary(occupational, cost, row, cycle, companyConfiguration);
                    em.persist(newSalary);

                    Job newJob = createJob(career, newSalary, companyConfiguration.getDefaultProfessorsCharge(), companyConfiguration, occupational);
                    em.persist(newJob);
                    List<Contract> contractList = employee != null ? contractService.getContractsByEmployeeInDateRange(employee, cycle.getStartDate(), cycle.getEndDate()) : new ArrayList<Contract>();
                    contract = matchContract(gestionHRKHIPUS, contractList, periodoHRKHIPUS, occupational);
                    if (null == contract) {
                        if (null == employee) {
                            //Create the employee
                            employee = createEmployee(row, occupational, companyConfiguration.getDefaultDocumentType());
                            em.persist(employee);
                        }
                        contract = createContract(
                                employee, occupational,
                                cycle, cycleInitDate,
                                cycleEndDate,
                                contractModeService.getContractModeById(occupational ? OCCUPATIONAL_CONTRACT_MODE : CIVIL_CONTRACT_MODE),
                                contractStateService.getContractStateById(NOT_SIGNED_CONTRACT_STATE));
                        em.persist(contract);
                    }
                    // pivot
                    updateNewHoraryBandPivot = true;
                    jobContract = createJobContract(occupational, newJob, contract, cost, academicSubjectGroupPk);
                    jobContract.setBuilding(newHoraryBandContract.getBuilding());
                    jobContract.setClassroom(newHoraryBandContract.getClassroom());
                    em.persist(jobContract);

                    newJob.getJobContractList().add(jobContract);
                    contract.getJobContractList().add(jobContract);
                    newHoraryBandContract.setJobContract(jobContract);
                    jobContract.getHoraryBandContractList().add(newHoraryBandContract);
                    jobContract.setCostPivotHoraryBandContract(newHoraryBandContract);
                }
            }
            em.persist(newHoraryBandContract.getHoraryBand());
            em.persist(newHoraryBandContract);
            if (updateNewHoraryBandPivot) {
                jobContract.setCostPivotHoraryBandContract(newHoraryBandContract);
            }
            em.flush();
        }
        em.flush();
        if (occupational) {
            //update amounts
            addModifiedContract(contract);
            addUpdatePivotJobContractList(jobContract);
            updatePivot(jobContract);
            if (null != movedJobContract) {
                updatePivot(movedJobContract);
            }
        }

        if (null != employee) {
            log.debug("Employee being processed: " + employee.getEmployeeCode() + " - " + employee.getIdNumber() + " - " + employee.getFullName());
            setEmployeeDefaultData(row, employee);
        }
        em.flush();
    }

    private void updatePivot(JobContract jobContract) {
        BigDecimal maxPrice = BigDecimal.ZERO;
        for (HoraryBandContract bandContract : jobContract.getHoraryBandContractList()) {
            if (BigDecimalUtil.isZeroOrNull(maxPrice)) {
//                if (Boolean.TRUE.equals(bandContract.getActive())) {
                maxPrice = bandContract.getPricePerPeriod();
                jobContract.setCostPivotHoraryBandContract(bandContract);
                jobContract.setClassroom(bandContract.getClassroom());
                jobContract.setBuilding(bandContract.getBuilding());
//                }
            } else {
                if (Boolean.TRUE.equals(bandContract.getActive())
                        && (Boolean.FALSE.equals(jobContract.getCostPivotHoraryBandContract().getActive())
                        || (bandContract.getPricePerPeriod().compareTo(maxPrice) > 0))) {
                    jobContract.setCostPivotHoraryBandContract(bandContract);
                    jobContract.setClassroom(bandContract.getClassroom());
                    jobContract.setBuilding(bandContract.getBuilding());
                }
            }
        }
    }

    private void updateJobContractPivot(HoraryBandContract horaryBandContract, JobContract actualJobContract) {
        if (null != actualJobContract.getCostPivotHoraryBandContract() && actualJobContract.getCostPivotHoraryBandContract().equals(horaryBandContract)) {
            //add pivot to update pivot list
            addUpdatePivotJobContractList(actualJobContract);
        }
    }

    private boolean shouldBandBeMoved(HoraryBandContract actualHoraryBandContract, String day, String building, String classroom) {
        return (!actualHoraryBandContract.getHoraryBand().getInitDay().equals(day) ||
                !actualHoraryBandContract.getHoraryBand().getEndDay().equals(day) ||
                !actualHoraryBandContract.getBuilding().equals(building) ||
                !actualHoraryBandContract.getClassroom().equals(classroom));
    }

    @Override
    public void postIterateRows() throws CompanyConfigurationNotFoundException {

        if (!ValidatorUtil.isEmptyOrNull(modifiedContractList)) {
            updateOccupationalJobContractAmounts(modifiedContractList);
            updateOccupationalContractAmounts(modifiedContractList);
            log.debug("********** update operations end");
        }
    }

    private void setEmployeeDefaultData(Map<String, String> row, Employee employee) {
        employee.setSalutation(salutationService.getDefaultSalutation(row.get(GENDER_COLUMN)));
        employee.setHomeAddress(row.get(HOME_ADDRESS_COLUMN));
        employee.setMaritalStatus(maritalStatusService.findByCode(row.get(MARITAL_STATUS)));
    }

    private void removeMovedJobContract(Boolean occupational, JobContract movedJobContract, HoraryBandContract horaryBandContract) {
        if (occupational && null != movedJobContract) {
            addModifiedContract(movedJobContract.getContract());
            updateJobContractPivot(horaryBandContract, movedJobContract);
        }
    }

    private void addModifiedContract(Contract contract) {
        if (null != contract && !modifiedContractList.contains(contract.getId())) {
            modifiedContractList.add(contract.getId());
        }
    }

    private void addUpdatePivotJobContractList(JobContract jobContract) {
        if (null != jobContract && !updatePivotJobContractList.contains(jobContract.getId())) {
            updatePivotJobContractList.add(jobContract.getId());
        }
    }

    private JobContract createJobContract(Boolean occupational, Job job, Contract contract, BigDecimal cost, AcademicSubjectGroupPK academicSubjectGroupPK) {
        JobContract newJobContract = new JobContract();
        newJobContract.setContract(contract);
        newJobContract.setJob(job);
        newJobContract.setAcademicSubjectGroupPK(academicSubjectGroupPK);
        if (occupational) {
            newJobContract.setOccupationalAmount(cost);
        }
        return newJobContract;
    }

    private void updateOccupationalContractAmounts(List<Long> contractIdList) {
        for (Long id : contractIdList) {
            em.createNamedQuery("Contract.sumOccupationalGlobalAmountByContract")
                    .setParameter("contractId", id)
                    .setParameter("active", Boolean.TRUE)
                    .executeUpdate();
            em.createNamedQuery("Contract.sumOccupationalBasicAmountByContract")
                    .setParameter("contractId", id)
                    .setParameter("active", Boolean.TRUE)
                    .executeUpdate();
        }
        em.flush();
        log.debug("execute updateOccupationalContractAmounts end");
    }

    @SuppressWarnings("unchecked")
    private void updateOccupationalJobContractAmounts(List<Long> contractIdList) {
        log.debug("execute updateOccupationalJobContractAmounts");
        List<Object[]> resultList = em.createNamedQuery("JobContract.findJobContractPricePerPeriodByContractList")
                .setParameter("contractIdList", contractIdList)
                .getResultList();
        for (Object[] objects : resultList) {
            JobContract jobContract = (JobContract) objects[0];
            BigDecimal totalCost = (BigDecimal) objects[1];

            BigDecimal cycleSalary = BigDecimalUtil.multiply(SCALE, totalCost, jobContract.getContract().getCycle().getLaboralWeeks());
            BigDecimal cycleNationalSalary = BigDecimalUtil.multiply(cycleSalary, jobContract.getContract().getCycle().getExchangeRate(), SCALE);
            //todo must always be 5 and contract day should be 150 (5*30)
            BigDecimal monthFactor = BigDecimalUtil.toBigDecimal(jobContract.getContract().getCycle().getLaboralDays() * MONTHS_PER_YEAR / DAYS_PER_YEAR);

            jobContract.setOccupationalAmount(cycleNationalSalary);
            jobContract.getJob().getSalary().setAmount(BigDecimalUtil.divide(cycleNationalSalary, monthFactor));
        }
        em.flush();
        log.debug("execute updateOccupationalJobContractAmounts end");
    }

    private Salary createSalary(Boolean occupational, BigDecimal cost, Map<String, String> row, Cycle cycle, CompanyConfiguration companyConfiguration) {
        Salary newSalary = new Salary();
        if (occupational) {
            newSalary.setAmount(calculateBasicSalary(cycle, cost, row));
            newSalary.setCurrency(currencyService.getCurrencyById(Constants.currencyIdBs));
            newSalary.setKindOfSalary(companyConfiguration.getKindOfSalaryDLH());
        } else {
            newSalary.setAmount(DEFAULT_PERIOD_COST);
            newSalary.setCurrency(currencyService.getCurrencyById(Constants.currencyIdSus));
            newSalary.setKindOfSalary(companyConfiguration.getKindOfSalaryDTH());
        }
        return newSalary;

    }


    private Salary createSalaryBasedOnPreviousJobContract(JobContract previousJobcontract) {
        Salary newSalary;
        newSalary = new Salary();
        newSalary.setAmount(previousJobcontract.getJob().getSalary().getAmount());
        newSalary.setCurrency(previousJobcontract.getJob().getSalary().getCurrency());
        newSalary.setKindOfSalary(previousJobcontract.getJob().getSalary().getKindOfSalary());
        return newSalary;
    }

    private AcademicSubjectGroupPK createAcademicSubjectGroupPk(Map<String, String> row) {
        AcademicSubjectGroupPK academicSubjectGroupPK = new AcademicSubjectGroupPK();
        academicSubjectGroupPK.setAsignature(row.get(SUBJECT_COLUMN));
        academicSubjectGroupPK.setCurricula(row.get(CURRICULUM_COLUMN));
        academicSubjectGroupPK.setGestion(Integer.parseInt(row.get(CYCLE_COLUMN)));
        academicSubjectGroupPK.setGroupType(row.get(GROUP_TYPE_COLUMN));
        academicSubjectGroupPK.setPeriod(Integer.parseInt(row.get(PERIOD_COLUMN)));
        academicSubjectGroupPK.setSubjectGroup(row.get(SUBJECT_GROUP_COLUMN));
        academicSubjectGroupPK.setSystemNumber(SISTEMA);
        return academicSubjectGroupPK;
    }

    private Employee createEmployee(Map<String, String> row, Boolean occupational, DocumentType defaultDocumentType) throws CompanyConfigurationNotFoundException {
        Employee newEmployee = new Employee();
        newEmployee.setIdNumber(row.get(DOCUMENT_COLUMN));
        newEmployee.setDocumentType(defaultDocumentType);
        newEmployee.setControlFlag(true);
        newEmployee.setAfpFlag(occupational);
        newEmployee.setRetentionFlag(true);
        newEmployee.setPaymentType(PaymentType.PAYMENT_BANK_ACCOUNT);
        newEmployee.setEmployeeCode(row.get(EMPLOYEE_COLUMN));
        newEmployee.setMarkCode(row.get(DOCUMENT_COLUMN));
        newEmployee.setLastName(row.get(LAST_NAME_COLUMN));
        newEmployee.setMaidenName(row.get(MAIDEN_NAME_COLUMN));
        newEmployee.setFirstName(row.get(NAME_COLUMN));
        setEmployeeDefaultData(row, newEmployee);
        return newEmployee;
    }

    private Job createJob(OrganizationalUnit career,
                          Salary newSalary, Charge charge, CompanyConfiguration companyConfiguration, Boolean occupational) {
        Job newJob = new Job();
        newJob.setCharge(charge);
        newJob.setJobCategory(occupational ? companyConfiguration.getJobCategoryDLH() : companyConfiguration.getJobCategoryDTH());
        newJob.setOrganizationalUnit(career);
        newJob.setSalary(newSalary);
        return newJob;
    }

    private void updateHoraryBandContract(Map<String, String> row, HoraryBandContract horaryBandContract,
                                          BigDecimal cost, String day, Date initHour, Date endHour, String building, String classroom) {
        horaryBandContract.setEndDate(convertStringToDate(row.get(HORARY_END_DATE)));
        horaryBandContract.setPricePerPeriod(cost);
        horaryBandContract.setInitDate(convertStringToDate(row.get(HORARY_INIT_DATE)));
        horaryBandContract.setTimeType(row.get("TIPO_HORA"));
        horaryBandContract.setActive((row.get("ACTIVO_HR").equals("SI")));
        horaryBandContract.setBuilding(building);
        horaryBandContract.setClassroom(classroom);

        horaryBandContract.getHoraryBand().setDuration(Integer.parseInt(row.get("DURACION")));
        horaryBandContract.getHoraryBand().setInitDay(day);
        horaryBandContract.getHoraryBand().setEndDay(day);

        horaryBandContract.getHoraryBand().setInitHour(initHour);
        horaryBandContract.getHoraryBand().setEndHour(endHour);
        em.merge(horaryBandContract.getHoraryBand());
        em.merge(horaryBandContract);
        em.flush();
    }

    private Contract createContract(Employee employee, Boolean occupational, Cycle cycle,
                                    Date initDate, Date endDate, ContractMode contractMode, ContractState contractState) {
        Contract contract = new Contract();
        //todo ask gustavo if FECHA_INI_CICLO_HR, FECHA_FIN_CICLO_HR can be replaced by FECHA_INICIO_HR and FECHA_FIN_HR
        contract.setInitDate(initDate);
        contract.setEndDate(endDate);
        // 1 is laboral and 2 is civil
        contract.setContractMode(contractMode);
        // 2 is not signed
        contract.setContractState(contractState);
        contract.setEmployee(employee);
        contract.setCycle(cycle);
        contract.setActiveForTaxPayrollGeneration(occupational);
        contract.setActivePensionFund(occupational);
        contract.setContractModificationAuthorization(false);
        contract.setAcademic(true);
        return contract;
    }

    /**
     * Calculates the basic salary for a occupational professor given a contract and a cost per cycle
     *
     * @param cycle the academic cycle
     * @param cost  Cost per cycle
     * @param row   a row band data
     * @return the amount
     */
    private BigDecimal calculateBasicSalary(Cycle cycle, BigDecimal cost, Map<String, String> row) {
        Integer bandPeriods = Integer.parseInt(row.get("DURACION")) / MINUTES_PER_PERIOD;
        BigDecimal cycleSalary = BigDecimalUtil.multiply(SCALE, BigDecimalUtil.toBigDecimal(bandPeriods), cycle.getLaboralWeeks(), cost);
        BigDecimal cycleNationalSalary = BigDecimalUtil.multiply(cycleSalary, cycle.getExchangeRate(), SCALE);
        //todo must always be 5 and contract day should be 150 (5*30)
        BigDecimal monthFactor = BigDecimalUtil.toBigDecimal(cycle.getLaboralDays() * MONTHS_PER_YEAR / DAYS_PER_YEAR);
        return BigDecimalUtil.divide(cycleNationalSalary, monthFactor);
    }

    private JobContract matchJobContract(Gestion gestionHRKHIPUS,
                                         List<JobContract> jobContractList,
                                         CycleType periodoHRKHIPUS, Boolean occupational, String day,
                                         Date initHour, Date endHour, String building, String classroom, OrganizationalUnit career) {
        for (JobContract jContract : jobContractList) { //Contract list of employees
            Cycle contractCycle = jContract.getContract().getCycle();
            if (equalsCycle(contractCycle, gestionHRKHIPUS, periodoHRKHIPUS) &&
                    (jContract.getJob().getJobCategory().getAcronym().equals("DTH") ||
                            isOccupational(jContract) ||
                            jContract.getJob().getJobCategory().getAcronym().equals("DTH1") ||
                            jContract.getJob().getJobCategory().getAcronym().equals("LINGUA") ||
                            jContract.getJob().getJobCategory().getAcronym().equals("HOSPITAL"))) {
                if (occupational) {
                    if (isOccupational(jContract)) {
                        HoraryBand horaryBand = jContract.getCostPivotHoraryBandContract().getHoraryBand();
                        if (horaryBand.getInitDay().equals(day) &&
                                horaryBand.getEndDay().equals(day) &&
                                horaryBand.getInitHour().equals(initHour) &&
                                horaryBand.getEndHour().equals(endHour)
                                ) {
                            return jContract;
                        }
                    }
                } else {
                    if (!isOccupational(jContract) && career.getCareer().equals(jContract.getJob().getOrganizationalUnit().getCareer())) {
                        return jContract;
                    }
                }
            }
        }
        return null;
    }

    private Contract matchContract(Gestion gestionHRKHIPUS,
                                   List<Contract> contractList,
                                   CycleType periodoHRKHIPUS, Boolean occupational) {
        for (Contract contract : contractList) { //Contract list of employees
            Cycle contractCycle = contract.getCycle();
            if (equalsCycle(contractCycle, gestionHRKHIPUS, periodoHRKHIPUS)) {
                if (occupational) {
                    if (contract.getActiveForPayrollGeneration()) {
                        return contract;
                    }
                } else {
                    if (!contract.getActiveForPayrollGeneration()) {
                        return contract;
                    }
                }
            }
        }
        return null;
    }

    private boolean isOccupational(JobContract jContract) {
        return jContract.getJob().getJobCategory().getAcronym().equals(OCCUPATIONAL_JOB_CATEGORY_ACRONYM);
    }

    /**
     * Determines if the band is related to a occupational contract
     *
     * @param occupational a string that determines if the contract shoul be occupational
     * @return true if is occupational
     */
    private boolean isOccupational(String occupational) {
        return null != occupational && OCCUPATIONAL_FLAG_VALUE.equalsIgnoreCase(occupational);
    }

    /**
     * Create HoraryBand, HoraryBandContract and relationships with Limit and tolerance.
     *
     * @param row       Integration row, Academic system
     * @param cost      price per period
     * @param day       day of week
     * @param building  the building of classroom
     * @param classroom @return HoraryBandContract
     * @return HoraryBandContract the created HoraryBandContract
     */
    private HoraryBandContract createHoraryBandContract(Map<String, String> row, BigDecimal cost, String day, String building, String classroom) {
        HoraryBand newHoraryBand = new HoraryBand();
        newHoraryBand.setDuration(Integer.parseInt(row.get("DURACION")));
        newHoraryBand.setInitDay(day);
        newHoraryBand.setEndDay(day);
        newHoraryBand.setInitHour(convertHourToDate(row.get("HORA_PERIODO_INICIAL")));
        newHoraryBand.setEndHour(convertHourToDate(row.get("HORA_PERIODO_FINAL")));

        HoraryBandContract newHoraryBandContract = new HoraryBandContract();
        newHoraryBandContract.setInitDate(convertStringToDate(row.get(HORARY_INIT_DATE)));
        newHoraryBandContract.setEndDate(convertStringToDate(row.get(HORARY_END_DATE)));
        newHoraryBandContract.setTimeType(row.get("TIPO_HORA"));
        newHoraryBandContract.setAcademicSchedule(Long.parseLong(row.get(HORARY_COLUMN)));
        newHoraryBandContract.setActive((row.get("ACTIVO_HR").equals("SI")));
        newHoraryBandContract.setShared(("SI".equals(row.get("COMPARTIDO"))));
        newHoraryBandContract.setBuilding(building);
        newHoraryBandContract.setClassroom(classroom);
        newHoraryBandContract.setSubjet(row.get("ASIGNATURA"));
        newHoraryBandContract.setGroupSubject(row.get(SUBJECT_GROUP_COLUMN));
        newHoraryBandContract.setNameSubject(row.get("NOMBRE"));
        newHoraryBandContract.setLimit(limitService.getLimit((long) 50000));
        newHoraryBandContract.setTolerance(toleranceService.getTolerance((long) 1));
        newHoraryBandContract.setPricePerPeriod(cost);
        newHoraryBandContract.setHoraryBand(newHoraryBand);
        newHoraryBandContract.setGestion(Integer.parseInt(row.get(CYCLE_COLUMN)));
        newHoraryBandContract.setPeriod(Integer.parseInt(row.get(PERIOD_COLUMN)));

        return newHoraryBandContract;
    }

    private boolean equalsCycle(Cycle contractCycle, Gestion gestionHRKHIPUS, CycleType periodHRKHIPUS) {
        boolean result = false;
        Cycle cycleHRKHIPUS = cycleService.findActiveCycle(gestionHRKHIPUS, periodHRKHIPUS);

        if (cycleHRKHIPUS != null && contractCycle.equals(cycleHRKHIPUS)) {
            result = true;
        }

        return result;
    }

    private Date convertStringToDate(String date) {
        Date result = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            result = dateFormat.parse(date);
            result = new Timestamp(result.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Date convertHourToDate(String hour) {
        Date result = null;
        try {
            result = parseToTime(hour);
        } catch (Exception e) {
            log.error(e, "Unexpected error parsing the hour string");
        }
        return result;
    }

    /**
     * Convert String to Time
     *
     * @param hora "hh:mm"
     * @return Time
     */
    private Time parseToTime(String hora) {
        int h, m, s;
        h = Integer.parseInt(hora.charAt(0) + "" + hora.charAt(1));
        m = Integer.parseInt(hora.charAt(3) + "" + hora.charAt(4));
        s = 0;
        return (new Time(h, m, s));
    }

    /**
     * Return literal day
     *
     * @param day, possible values LU, MA, MI, JU, VI, SA, DO
     * @return String literal day
     */
    private String getLiteralDay(String day) {
        String result = "";
        if (day.equals("LU")) {
            result = "LUNES";
        }
        if (day.equals("MA")) {
            result = "MARTES";
        }
        if (day.equals("MI")) {
            result = "MIERCOLES";
        }
        if (day.equals("JU")) {
            result = "JUEVES";
        }
        if (day.equals("VI")) {
            result = "VIERNES";
        }
        if (day.equals("SA")) {
            result = "SABADO";
        }
        if (day.equals("DO")) {
            result = "DOMINGO";
        }
        return result;
    }
}
