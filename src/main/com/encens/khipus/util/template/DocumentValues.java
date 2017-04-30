package com.encens.khipus.util.template;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.academics.Asignature;
import com.encens.khipus.model.academics.Horary;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HoraryBand;
import com.encens.khipus.model.employees.HoraryBandContract;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.academics.HoraryService;
import com.encens.khipus.service.employees.ContractService;
import com.encens.khipus.service.employees.EmployeeService;
import com.encens.khipus.service.employees.HoraryBandContractService;
import com.encens.khipus.util.*;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * Util to calculate variable values for document templates
 *
 * @author
 * @version $Id: DocumentValues.java  17-mar-2010 15:44:19$
 */
//@Name("documentValues")
public class DocumentValues {
    private Log log = Logging.getLog(DocumentValues.class);
    private GenericService genericService = (GenericService) Component.getInstance("genericService");
    private SessionUser sessionUser = (SessionUser) Component.getInstance("sessionUser");

    private HoraryService horaryService = (HoraryService) Component.getInstance("horaryService");

    /**
     * get current date field as literal expression
     *
     * @return Map
     */
    private Map<String, Object> getLiteralCurrentDateFieldValue() {
        Map<String, Object> variableValue = new HashMap<String, Object>();
        Date currentDate = new Date();

        variableValue.put(VariableConstants.FIELD_CURRENTDATE, formatDateAsLiteral(currentDate));
        variableValue.put(VariableConstants.FIELD_CURRENTDATE_DAYS, formatDateFilterDaysLiteral(currentDate));
        variableValue.put(VariableConstants.FIELD_CURRENTDATE_MONTH, formatDateFilterMonthLiteral(currentDate));
        variableValue.put(VariableConstants.FIELD_CURRENTDATE_YEAR, formatDateFilterYearLiteral(currentDate));
        return variableValue;
    }

    /**
     * calculate employee contract variable values
     *
     * @param employeeId    employee Id
     * @param jobContractId job contract Id
     * @return Map
     */
    public Map<String, Object> getEmployeeContractValues(Long employeeId, Long jobContractId) {
        log.debug("employee variable values...." + genericService + "-" + employeeId + "-" + jobContractId);

        Map<String, Object> variableValuesMap = new HashMap<String, Object>();

        Employee employee = findEmployee(employeeId);
        JobContract jobContract = findJobContrac(jobContractId);
        BusinessUnit businessUnit = jobContract.getJob().getOrganizationalUnit().getBusinessUnit();

        if (businessUnit != null) {
            variableValuesMap.putAll(getBusinessUnitValues(businessUnit));
        }
        variableValuesMap.putAll(getEmployeeValues(employee));
        variableValuesMap.putAll(getExtendedEmployeeValues(jobContract));
        variableValuesMap.putAll(getContractValues(jobContract));
        variableValuesMap.putAll(getLiteralCurrentDateFieldValue());

        return variableValuesMap;
    }

    private Map<String, Object> getBusinessUnitValues(BusinessUnit businessUnit) {
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();
        Employee responsibleRRHH = businessUnit.getHumanResourcesResponsible();

        variableValuesMap.put(VariableConstants.FIELD_BUSINESSUNIT_NAME, validValue(businessUnit.getOrganization().getName()));
        variableValuesMap.put(VariableConstants.FIELD_BUSINESSUNIT_ADDRESS, validValue(businessUnit.getAddress()));
        variableValuesMap.put(VariableConstants.FIELD_BUSINESSUNIT_RESPONSIBLE_RRHH, composeEmployeeName(responsibleRRHH));
        variableValuesMap.put(VariableConstants.FIELD_BUSINESSUNIT_RESPONSIBLE_RRHH_CI, composeEmployeeIdentification(responsibleRRHH));
        variableValuesMap.put(VariableConstants.FIELD_BUSINESSUNIT_RESPONSIBLE_RRHH_SALUTATION, (responsibleRRHH != null && responsibleRRHH.getSalutation() != null) ? validValue(responsibleRRHH.getSalutation().getName()) : "");
        variableValuesMap.put(VariableConstants.FIELD_BUSINESSUNIT_COMERCIAL_NUMBER, validValue(businessUnit.getCommercialEnrollmentNumber()));
        variableValuesMap.put(VariableConstants.FIELD_BUSINESSUNIT_NIT, validValue(businessUnit.getOrganization().getIdNumber()));

        return variableValuesMap;
    }

    private Map<String, Object> getEmployeeValues(Employee employee) {
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_NAME, composeEmployeeName(employee));
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_IDENTIFICATION, composeEmployeeIdentification(employee));
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_SALUTATION, (employee.getSalutation() != null) ? validValue(employee.getSalutation().getName()) : "");
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_NATIONALITY, (employee.getCountry() != null) ? validValue(employee.getCountry().getName()) : "");
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_MARITAL_STATUS, (employee.getMaritalStatus() != null) ? validValue(employee.getMaritalStatus().getName()) : "");
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_ADDRESS, validValue(employee.getHomeAddress()));

        return variableValuesMap;
    }

    private Map<String, Object> getExtendedEmployeeValues(JobContract jobContract) {
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();
        Job job = jobContract.getJob();
        OrganizationalUnit organizationalUnit = job.getOrganizationalUnit();

        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_SEDE, validValue(organizationalUnit.getBusinessUnit().getPublicity()));
        //department, is the direct relation of Job to Organizational unit
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_DEPARTMENT, validValue(organizationalUnit.getName()));
        //Area, is the internal relation between organizational unit
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_AREA, (organizationalUnit.getOrganizationalUnitRoot() != null) ? validValue(organizationalUnit.getOrganizationalUnitRoot().getName()) : "");

        return variableValuesMap;
    }

    private Map<String, Object> getContractValues(JobContract jobContract) {
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();
        Contract contract = jobContract.getContract();

        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CONTRACT_INITDATE, formatDateAsLiteral(contract.getInitDate()));
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CONTRACT_INITDATE_EXTENDED, formatDateAsExtendedLiteral(contract.getInitDate()));
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CONTRACT_ENDDATE, formatDateAsLiteral(contract.getEndDate()));
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CONTRACT_SALARY, composeEmployeeContractSalary(jobContract));

        addEmployeeContractPeriodFields(variableValuesMap, contract);
        return variableValuesMap;
    }

    /**
     * Add contract period fields in variable values map
     *
     * @param variableValuesMap Map
     * @param contract          contract
     */
    private void addEmployeeContractPeriodFields(Map<String, Object> variableValuesMap, Contract contract) {
        String duration = "";
        int installments = 0;

        DateTime initDateTime = new DateTime(contract.getInitDate());
        DateTime endDateTime = new DateTime(contract.getEndDate());

        int months = 0;
        int days = 0;
        //calculate periods
        if (initDateTime.isBefore(endDateTime)) {
            Period period = new Period(initDateTime, endDateTime);
            log.debug("PERIOD:" + initDateTime + "-" + endDateTime + ", year:" + period.getYears() + ", month:" + period.getMonths() + ",week:" + period.getWeeks() + ",days:" + period.getDays());

            months = (period.getYears() * 12) + period.getMonths();
            days = (period.getWeeks() * 7) + period.getDays();
        }

        if (months > 0) {
            installments = months;
            if (months == 1) {
                duration = MessageUtils.getMessage("GenerateContract.document.oneMonth");
            } else {
                duration = FormatUtils.getLiteralExpression(months, sessionUser.getLocale()) + " " + MessageUtils.getMessage("GenerateContract.document.months");
            }
        }

        if (days > 0) {
            installments++;
            if (months > 0) {
                duration = duration + " " + MessageUtils.getMessage("GenerateContract.document.and") + " ";
            }
            if (days == 1) {
                duration = duration + MessageUtils.getMessage("GenerateContract.document.oneDay");
            } else {
                duration = duration + FormatUtils.getLiteralExpression(days, sessionUser.getLocale()) + " " + MessageUtils.getMessage("GenerateContract.document.days");
            }
        }

        String installmentLiteral;
        if (installments == 1) {
            installmentLiteral = MessageUtils.getMessage("GenerateContract.document.oneCuote");
        } else {
            installmentLiteral = FormatUtils.getLiteralExpression(installments, sessionUser.getLocale()) + " " + MessageUtils.getMessage("GenerateContract.document.cuotes");
        }

        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CONTRACT_DURATION, duration);
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CONTRACT_PAYINSTALLMENT, installmentLiteral);
    }

    /**
     * Teacher and contract read variable values
     *
     * @param businessUnit businessUnit
     * @param employee     employee
     * @param initDate     range init date to filter contrac
     * @param endDate      range end date to filter contract
     * @return Map
     */
    public Map getTeacherVariableValues(BusinessUnit businessUnit, Employee employee, Date initDate, Date endDate) {
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();

        ContractService contractService = (ContractService) Component.getInstance("contractService");
        List<Contract> contractList = contractService.getContractsByEmployeeInGestion(employee, initDate, endDate);

        List<Map> teacherAsignatureValuesList = getTeacherAsignatureValues(contractList);

        variableValuesMap.putAll(getBusinessUnitValues(businessUnit));
        variableValuesMap.putAll(getEmployeeValues(employee));
        variableValuesMap.putAll(getTeacherContractValues(contractList));
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE, teacherAsignatureValuesList);
        variableValuesMap.put(VariableConstants.FIELD_TEACHER_CAREERS, composeTeacherCareerAssigned(teacherAsignatureValuesList));
        variableValuesMap.putAll(getLiteralCurrentDateFieldValue());

        return variableValuesMap;
    }

    /**
     * Get teacher contract related variables
     *
     * @param contractList all contract related to teacher
     * @return Map
     */
    private Map<String, Object> getTeacherContractValues(List<Contract> contractList) {
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();

        //sum all salary of all contracts
        BigDecimal teacherSalary = BigDecimal.ZERO;
        for (Contract contract : contractList) {
            if (contract.getOccupationalBasicAmount() != null) {
                teacherSalary = BigDecimalUtil.sum(teacherSalary, contract.getOccupationalBasicAmount());
            }
        }

        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CONTRACT_SALARY, composeTeacherContractSalary(teacherSalary));

        return variableValuesMap;
    }

    private List<Map> getTeacherAsignatureValues(List<Contract> contractList) {
        List<Map> teacherContractValuesList = new ArrayList<Map>();
        HoraryBandContractService horaryBandContractService = (HoraryBandContractService) Component.getInstance("horaryBandContractService");

        for (Contract contract : contractList) {
            List<JobContract> jobContractList = contract.getJobContractList();
            for (JobContract jobContract : jobContractList) {
                List<HoraryBandContract> horaryBandContractList = horaryBandContractService.getHoraryBandContractsByJobContractActive(jobContract, Boolean.TRUE);

                String asignatureHorary = "";
                for (int i = 0; i < horaryBandContractList.size(); i++) {
                    HoraryBandContract horaryBandContract = horaryBandContractList.get(i);
                    String horary = composeHorayBand(horaryBandContract);
                    asignatureHorary += asignatureHorary.isEmpty() ? horary : "\n" + horary;

                    if ((i + 1) < horaryBandContractList.size()) {
                        HoraryBandContract nextHoraryBandContract = horaryBandContractList.get(i + 1);
                        Asignature nextAsignature = nextHoraryBandContract.getAsignature();
                        if (nextAsignature == null || !nextAsignature.equals(horaryBandContract.getAsignature())) {
                            teacherContractValuesList.add(getCareerAsignatureVariableValues(jobContract, horaryBandContract, asignatureHorary));
                            asignatureHorary = "";
                        }
                    } else {
                        teacherContractValuesList.add(getCareerAsignatureVariableValues(jobContract, horaryBandContract, asignatureHorary));
                        asignatureHorary = "";
                    }
                }
            }
        }
        return teacherContractValuesList;
    }

    private Map<String, Object> getCareerAsignatureVariableValues(JobContract jobContract, HoraryBandContract horaryBandContract, String composedHorary) {
        Asignature asignature = horaryBandContract.getAsignature();
        Integer academicPeriod = (asignature != null) ? asignature.getScheduleCharge() : null;
        Salary salary = jobContract.getJob().getSalary();

        //employee = genericService.findById(Employee.class, employeeId);
        //Horary horary = (Horary)horaryService.getHoraryById(horaryBandContract.getAcademicSchedule());
        //Horary horary = findHorary(horaryBandContract.getAcademicSchedule());
        String careerName = getCareerName(horaryBandContract);
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();
        //variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_CAREER, validValue(jobContract.getJob().getOrganizationalUnit().getName()));
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_CAREER, validValue(careerName));
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_SUBJECT, validValue(asignature != null ? asignature.getName() : ""));
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_HORARYCHARGE, validValue(academicPeriod));
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_THEORETICALCHARGE, asignature != null ? validValue(asignature.getTheoreticalCharge()) : "");
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_PRACTICECHARGE, asignature != null ? validValue(asignature.getPracticalCharge()) : "");
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_HORARY, composedHorary);
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_AMOUNT, academicPeriod != null ? formatDecimalNumber(BigDecimalUtil.multiply(BigDecimal.valueOf(academicPeriod), salary.getAmount())) : "");
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_PERIODCOST, formatDecimalNumber(salary.getAmount()));
        variableValuesMap.put(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_PERIODCOST_CURRENCY, validValue(salary.getCurrency() != null ? salary.getCurrency().getSymbol() : ""));

        return variableValuesMap;
    }

    private String composeTeacherCareerAssigned(List<Map> teacherAsignatureValuesList) {
        String assignedCareers = "";

        Set<String> careerSet = new HashSet<String>();
        //filter repeated careers
        for (Map teacherAsignatureMap : teacherAsignatureValuesList) {
            Object careerValue = teacherAsignatureMap.get(VariableConstants.FIELD_LIST_TEACHERASIGNATURE_CAREER);
            if (careerValue != null && !careerValue.toString().isEmpty()) {
                careerSet.add(careerValue.toString());
            }
        }

        //compose the assigned careers
        for (String career : careerSet) {
            if (!assignedCareers.isEmpty()) {
                assignedCareers += ", ";
            }
            assignedCareers += career;
        }

        return assignedCareers;
    }

    private String composeHorayBand(HoraryBandContract horaryBandContract) {
        String horary = "";
        HoraryBand horaryBand = horaryBandContract.getHoraryBand();
        if (horaryBand != null) {
            horary = validValue(horaryBand.getInitDay()) + " " + formatTime(horaryBand.getInitHour()) + "-" + formatTime(horaryBand.getEndHour());
        }
        horary = horary + " " + MessageUtils.getMessage("GenerateContract.document.horary.group") + " " + validValue(horaryBandContract.getGroupSubject());
        return horary;
    }

    /**
     * Get variable values for employee accidental association document
     *
     * @param businessUnit
     * @param organizationalUnit
     * @param contractInitDate
     * @param contractEndDate
     * @return Map
     */
    public Map<String, Object> getAccidentalAssociationEmployeeListVariableValues(BusinessUnit businessUnit, OrganizationalUnit organizationalUnit, Date contractInitDate, Date contractEndDate) {
        Map<String, Object> variableValuesMap = new HashMap<String, Object>();

        EmployeeService employeeService = (EmployeeService) Component.getInstance("employeeService");
        List<Employee> employeeList = employeeService.getEmployeesByBusinessUnitOrganizationalUnit(businessUnit, organizationalUnit, contractInitDate, contractEndDate);

        List<Map> employeeValuesList = new ArrayList<Map>();
        for (int i = 0; i < employeeList.size(); i++) {
            Employee employee = employeeList.get(i);
            Map<String, Object> countMap = new HashMap<String, Object>();
            countMap.put(VariableConstants.FIELD_LIST_ACCIDENTALASSOCIATION_TEACHERNUMBER, String.valueOf(i + 1));

            employeeValuesList.add(countMap);
        }

        variableValuesMap.putAll(getBusinessUnitValues(businessUnit));
        variableValuesMap.put(VariableConstants.FIELD_LIST_ACCIDENTALASSOCIATION, employeeValuesList);
        variableValuesMap.put(VariableConstants.FIELD_EMPLOYEE_CAREER, validValue(organizationalUnit.getName()));
        variableValuesMap.putAll(getLiteralCurrentDateFieldValue());
        return variableValuesMap;
    }

    private String getCareerName(HoraryBandContract horaryBandContract) {
        String careerName = "";
        log.debug("getCareerName");
        if (null != horaryBandContract.getAcademicSchedule()) {
            try {
                Long academicSchedule = horaryBandContract.getAcademicSchedule();
                log.debug("academic schedule: " + academicSchedule);
                Horary horary = horaryService.getHoraryById(academicSchedule, horaryBandContract.getGestion(), horaryBandContract.getPeriod());
                log.debug("horary found: " + (null != horary));
                if (null != horary && horary.getCarrer() != null) {
                    log.debug("nombre carrera:" + horary.getCarrer());
                    careerName = horary.getCarrer().getName();
                }
            } catch (Exception e) {
                log.debug("Error in getCareerName of academic horary with id : " + horaryBandContract.getAcademicSchedule());
            }
        }
        if (ValidatorUtil.isBlankOrNull(careerName)) {
            careerName = horaryBandContract.getJobContract().getJob().getOrganizationalUnit().getName();
        }
        return careerName;
    }

    private Employee findEmployee(Long employeeId) {
        Employee employee = null;
        try {
            employee = genericService.findById(Employee.class, employeeId);
        } catch (EntryNotFoundException e) {
            log.debug("Not found Employee with id:" + employeeId);
        }
        return employee;
    }

    private Contract findContrac(Long contractId) {
        Contract contract = null;
        try {
            contract = genericService.findById(Contract.class, contractId);
        } catch (EntryNotFoundException e) {
            log.debug("Not found Contract with id:" + contractId);
        }
        return contract;
    }

    private JobContract findJobContrac(Long jobContractId) {
        JobContract jobContract = null;
        try {
            jobContract = genericService.findById(JobContract.class, jobContractId);
        } catch (EntryNotFoundException e) {
            log.debug("Not found JobContract with id:" + jobContractId);
        }
        return jobContract;
    }

    private String composeEmployeeName(Employee employee) {
        String fullName = "";
        if (employee != null) {
            fullName = (employee.getFirstName() != null ? employee.getFirstName() + " " : "") + (employee.getLastName() != null ? employee.getLastName() + " " : "") + (employee.getMaidenName() != null ? employee.getMaidenName() : "");
        }
        return fullName;
    }

    private String composeEmployeeIdentification(Employee employee) {
        String identification = "";
        if (employee != null) {
            identification = validValue(employee.getIdNumber());
            if (employee.getExtensionSite() != null) {
                identification = identification + " " + validValue(employee.getExtensionSite().getExtension());
            }
        }
        return identification;
    }

    private String composeEmployeeContractSalary(JobContract jobContract) {
        String literalSalary = "";
        Currency currency = jobContract.getJob().getSalary().getCurrency();
        BigDecimal salary = jobContract.getJob().getSalary().getAmount();

        literalSalary = currency.getSymbol() + " " + formatDecimalNumber(salary) + ".- (" + FormatUtils.getSpecialLiteralExpression(salary, sessionUser.getLocale()) + " " + validValue(currency.getDescription()) + ")";
        return literalSalary;
    }

    private String composeTeacherContractSalary(BigDecimal salary) {
        return formatDecimalNumber(salary) + ".- (" + FormatUtils.getSpecialLiteralExpression(salary, sessionUser.getLocale()) + ")";
    }

    private String formatTime(Date date) {
        return (date != null) ? DateUtils.format(date, MessageUtils.getMessage("patterns.hourMinuteTime")) : "";
    }

    private String formatDateAsLiteral(Date date) {
        return (date != null) ? DateUtils.format(date, MessageUtils.getMessage("patterns.dateLiteral")) : "";
    }

    private String formatDateAsExtendedLiteral(Date date) {
        String dateLiteral = "";
        if (date != null) {
            DateTime dateTime = new DateTime(date);
            if (dateTime.getDayOfMonth() == 1) {
                dateLiteral = DateUtils.format(date, MessageUtils.getMessage("patterns.dateLiteralExtendedFirstDay"));
            } else {
                dateLiteral = DateUtils.format(date, MessageUtils.getMessage("patterns.dateLiteralExtended"));
            }
        }
        return dateLiteral;
    }

    private String formatDateFilterDaysLiteral(Date date) {
        String daysLiteral = "";
        if (date != null) {
            DateTime dateTime = new DateTime(date.getTime());
            daysLiteral = FormatUtils.getLiteralExpression(dateTime.getDayOfMonth(), sessionUser.getLocale());
        }
        return daysLiteral;
    }

    private String formatDateFilterMonthLiteral(Date date) {
        return (date != null) ? DateUtils.format(date, MessageUtils.getMessage("patterns.dateFilterMonth")) : "";
    }

    private String formatDateFilterYearLiteral(Date date) {
        String yearLiteral = "";
        if (date != null) {
            DateTime dateTime = new DateTime(date.getTime());
            yearLiteral = FormatUtils.getLiteralExpression(dateTime.getYear(), sessionUser.getLocale());
        }
        return yearLiteral;
    }

    private String formatDecimalNumber(BigDecimal bigDecimal) {
        return (bigDecimal != null) ? FormatUtils.formatNumber(bigDecimal, MessageUtils.getMessage("patterns.decimalNumber"), sessionUser.getLocale()) : "";
    }

    private String validValue(String value) {
        return value != null && value.trim().length() > 0 ? value : "";
    }

    private String validValue(Integer integerValue) {
        return integerValue != null ? integerValue.toString() : "";
    }
}
