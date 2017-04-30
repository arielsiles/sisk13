package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.customers.ExtensionService;
import com.encens.khipus.service.employees.JobCategoryService;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.employees.KindOfSalaryService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Manager;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : 3.3
 */
@Name("jobContractAction")
@Scope(ScopeType.CONVERSATION)
public class JobContractAction extends GenericAction<JobContract> {

    public static final int BLOCK_CODE_LENGTH = 6;
    private Employee employee;
    private Contract contract = new Contract();
    private Salary salary = new Salary();
    private Job job = new Job(new Salary());
    private Charge charge;
    private Long deleteJobId;

    private OrganizationalUnit organizationalUnit;

    private JobCategory jobCategory;
    private Sector sector;

    public List<Extension> extensionList;
    private boolean showExtension = false;

    private String inputModificationCode;
    private Boolean modificationCodeUnlock = false;

    @In
    private ExtensionService extensionService;
    @In
    private JobCategoryService jobCategoryService;
    @In
    private KindOfSalaryService kindOfSalaryService;
    @In
    private JobContractService jobContractService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @Factory("jobContract")
    @Restrict("#{s:hasPermission('JOBCONTRACT','VIEW')}")
    public JobContract initJobContract() {
        return getInstance();
    }

    @Override
    protected GenericService getService() {
        return jobContractService;
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("JobContract.title");
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Salary getSalary() {
        return salary;
    }

    public void setSalary(Salary salary) {
        this.salary = salary;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public void clearCharge() {
        setCharge(null);
    }

    public Long getDeleteJobId() {
        return deleteJobId;
    }

    public void setDeleteJobId(Long deleteJobId) {
        this.deleteJobId = deleteJobId;
    }

    public void clearPensionFundInfo() {
        clearPensionFundOrganization();
        getContract().setPensionFundRegistrationCode(null);
        clearSocialSecurityOrganization();
        getContract().setSocialSecurityRegistrationCode(null);
    }

    public void assignPensionFundOrganization(SocialWelfareEntity pensionFundOrganization) {
        getContract().setPensionFundOrganization(pensionFundOrganization);
    }

    public void clearPensionFundOrganization() {
        getContract().setPensionFundOrganization(null);
    }

    public void assignSocialSecurityOrganization(SocialWelfareEntity socialSecurityOrganization) {
        getContract().setSocialSecurityOrganization(socialSecurityOrganization);
    }

    public void clearSocialSecurityOrganization() {
        getContract().setSocialSecurityOrganization(null);
    }

    /*For organizational unit selectors*/

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }


    public String getBusinessUnitName() {
        return getOrganizationalUnit() != null ? getOrganizationalUnit().getBusinessUnit().getOrganization().getName() : "";
    }

    public String getOrganizationalUnitName() {
        return getOrganizationalUnit() != null ? getOrganizationalUnit().getFullName() : null;
    }


    public void assignOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        if (organizationalUnit != null) {
            try {
                organizationalUnit = getService().findById(OrganizationalUnit.class, organizationalUnit.getId());
                setSector(organizationalUnit.getSector());
            } catch (EntryNotFoundException e) {
                entryNotFoundLog();
            }
            setOrganizationalUnit(organizationalUnit);
            //noinspection NullableProblems
            setJobCategory(null);
        } else {
            clearOrganizationalUnit();
        }
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearOrganizationalUnit() {
        setOrganizationalUnit(null);
        setSector(null);
        setJobCategory(null);
        getSalary().setKindOfSalary(null);
    }

    /*For job contract selectors*/

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getSectorName() {
        return getSector() != null ? getSector().getName() : null;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public List<JobCategory> getJobCategoryList() {
        return jobCategoryService.getJobCategoriesBySector(getSector());
    }

    public List<KindOfSalary> getKindOfSalaryList() {
        return sector != null ? kindOfSalaryService.findBySector(getSector()) : new ArrayList<KindOfSalary>();
    }

    /*Finders*/

    public void assignEmployee(Employee employee) {
        if (employee != null) {
            try {
                employee = getService().findById(Employee.class, employee.getId());
            } catch (EntryNotFoundException e) {
                entryNotFoundLog();
            }
        }
        setEmployee(employee);
        updateShowExtension();
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    /*CRUD methods*/

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('JOBCONTRACT','VIEW')}")
    public String select(JobContract instance) {
        String outcome = super.select(instance);
        if (Outcome.SUCCESS.equals(outcome)) {
            try {
                setContract(getService().findById(Contract.class, getInstance().getContractId()));
                setEmployee(getService().findById(Employee.class, getContract().getEmployeeId()));
                setJob(getService().findById(Job.class, getInstance().getJobId()));
                setSalary(getService().findById(Salary.class, getJob().getSalaryId()));
                setCharge(getService().findById(Charge.class, getJob().getChargeId()));
                setOrganizationalUnit(getService().findById(OrganizationalUnit.class, getJob().getOrganizationalUnitId()));
                setJobCategory(getService().findById(JobCategory.class, getJob().getJobCategoryId()));
                setSector(getService().findById(Sector.class, getJobCategory().getSectorId()));
            } catch (EntryNotFoundException e) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            updateShowExtension();
        }
        return outcome;
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String selectJobContract(JobContract instance) {
        Contexts.getConversationContext().remove("contractAction");
        Manager.instance().endConversation(true);
        return select(instance);
    }
    /*createOrUpdateEmployee method*/

    public String createOrUpdateEmployee() {
        if (getEmployee().getId() == null) {
            try {
                genericService.create(getEmployee());
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
                return Outcome.REDISPLAY;
            }
        } else {
            Long currentVersion = getEmployee().getVersion();
            try {
                genericService.update(getEmployee());
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
                getEmployee().setVersion(currentVersion);
                return Outcome.REDISPLAY;
            } catch (ConcurrencyException e) {
                concurrencyLog();
                try {
                    setEmployee(genericService.findById(Employee.class, getEmployee().getId()));
                } catch (EntryNotFoundException e1) {
                    entryNotFoundLog();
                    addNotFoundMessage();
                    return Outcome.FAIL;
                }
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            }
        }

        return Outcome.SUCCESS;
    }

    public String createSalaryIfNecesary() {

        if (getSalary().getId() == null) {
            try {
                genericService.create(getSalary());
            } catch (EntryDuplicatedException e) {
                concurrencyLog();
                addDuplicatedMessage();
                return Outcome.REDISPLAY;
            }
        }
        return Outcome.SUCCESS;
    }

    public String createOrUpdateContract() {
        getContract().setEmployee(getEmployee());
        if (getContract().getId() == null) {
            try {
                genericService.create(getContract());
            } catch (EntryDuplicatedException e) {
                concurrencyLog();
                addDuplicatedMessage();
                return Outcome.REDISPLAY;
            }
        } else {
            Long currentVersion = getContract().getVersion();
            try {
                getContract().setContractModificationAuthorization(false);
                genericService.update(getContract());
            } catch (EntryDuplicatedException e) {
                concurrencyLog();
                addDuplicatedMessage();
                getContract().setVersion(currentVersion);
                return Outcome.REDISPLAY;
            } catch (ConcurrencyException e) {
                concurrencyLog();
                try {
                    setContract(genericService.findById(Contract.class, getContract().getId()));
                } catch (EntryNotFoundException e1) {
                    entryNotFoundLog();
                    addNotFoundMessage();
                    return Outcome.FAIL;
                }
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            }
        }
        return Outcome.SUCCESS;
    }

    public String createJob(OrganizationalUnit currentOrganizationalUnit, JobCategory currentJobCategory) {
        getJob().setSalary(getSalary());
        getJob().setOrganizationalUnit(currentOrganizationalUnit);
        getJob().setJobCategory(currentJobCategory);
        getJob().setCharge(getCharge());
        try {
            genericService.create(getJob());
        } catch (EntryDuplicatedException e) {
            concurrencyLog();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    public String createOrUpdateJob(OrganizationalUnit currentOrganizationalUnit, JobCategory currentJobCategory) {
        if (getJob().getId() == null) {
            return createJob(currentOrganizationalUnit, currentJobCategory);
        } else {
            if (!getJob().getSalary().equals(getSalary())
                    || !getJob().getCharge().equals(getCharge())
                    || !getJob().getOrganizationalUnit().equals(currentOrganizationalUnit)
                    || !getJob().getJobCategory().equals(currentJobCategory)) {
                setDeleteJobId(getJob().getId());
                setJob(new Job(getJob()));
                return createJob(currentOrganizationalUnit, currentJobCategory);
            } else {
                Long currentVersion = getJob().getVersion();
                try {
                    genericService.update(getJob());
                } catch (EntryDuplicatedException e) {
                    concurrencyLog();
                    getJob().setVersion(currentVersion);
                    return Outcome.REDISPLAY;
                } catch (ConcurrencyException e) {
                    concurrencyLog();
                    try {
                        setJob(genericService.findById(Job.class, getJob().getId()));
                    } catch (EntryNotFoundException e1) {
                        entryNotFoundLog();
                        addNotFoundMessage();
                        return Outcome.FAIL;
                    }
                    addUpdateConcurrencyMessage();
                    return Outcome.REDISPLAY;
                }
            }
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('JOBCONTRACT','CREATE')}")
    public String create() {

        if (getOrganizationalUnit() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "JobContract.error.organizationalUnitRequired");
            return Outcome.REDISPLAY;
        }

        if (getJobCategory() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "JobContract.error.jobCategoryRequired");
            return Outcome.REDISPLAY;
        }
        if (getEmployee() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "JobContract.error.employeeRequired");
            return Outcome.REDISPLAY;
        }
        //Create or update employee
        if (!Outcome.SUCCESS.equals(createOrUpdateEmployee())) {
            return Outcome.REDISPLAY;
        }

        //Create or update contract
        if (!Outcome.SUCCESS.equals(createOrUpdateContract())) {
            return Outcome.REDISPLAY;
        }

        //Create or update salary
        if (!Outcome.SUCCESS.equals(createSalaryIfNecesary())) {
            return Outcome.REDISPLAY;
        }

        //Create or update job
        if (!Outcome.SUCCESS.equals(createOrUpdateJob(getOrganizationalUnit(), getJobCategory()))) {
            return Outcome.REDISPLAY;
        }

        //Setting the current instance's values
        getInstance().setJob(getJob());
        getInstance().setContract(getContract());

        return super.create();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('JOBCONTRACT','UPDATE')}")
    public String update() {
        if (getModificationCodeUnlock()) {
            getContract().setModificationCode(null);
        }

        if (getContract().getContractModificationAuthorization() && !eventEm.find(Contract.class, getContract().getId()).getContractModificationAuthorization()) {
            addContractModificationDisabled();
            try {
                getInstance().setContract(getService().findById(Contract.class, getContract().getId(), true));
            } catch (EntryNotFoundException e) {
                log.error(e, "the contract couldn't be found");
            }
            return Outcome.REDISPLAY;
        }
        if (getOrganizationalUnit() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "JobContract.error.organizationalUnitRequired");
            return Outcome.REDISPLAY;
        }

        if (getJobCategory() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "JobContract.error.jobCategoryRequired");
            return Outcome.REDISPLAY;
        }

        //Create or update employee
        if (!Outcome.SUCCESS.equals(createOrUpdateEmployee())) {
            return Outcome.REDISPLAY;
        }

        //Create or update contract
        if (!Outcome.SUCCESS.equals(createOrUpdateContract())) {
            return Outcome.REDISPLAY;
        }

        //Create or update salary
        if (!Outcome.SUCCESS.equals(createSalaryIfNecesary())) {
            return Outcome.REDISPLAY;
        }

        //Create or update job
        if (!Outcome.SUCCESS.equals(createOrUpdateJob(getOrganizationalUnit(), getJobCategory()))) {
            return Outcome.REDISPLAY;
        }

        //Setting the current instance's values
        getInstance().setJob(getJob());
        getInstance().setContract(getContract());
        getInstance().getContract().setEmployee(getEmployee());

        String outcome = super.update();

        if (getDeleteJobId() != null) {
            try {
                genericService.delete(genericService.findById(Job.class, getDeleteJobId()));

            } catch (ConcurrencyException e) {
                entryNotFoundLog();
                addDeleteConcurrencyMessage();
            } catch (ReferentialIntegrityException e) {
                referentialIntegrityLog();
                addDeleteReferentialIntegrityMessage();
            } catch (EntryNotFoundException e) {
                entryNotFoundLog();
            }
        }

        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('JOBCONTRACT','DELETE')}")
    public String delete() {
        return super.delete();
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getEmployee().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getEmployee().setExtensionSite(null);
        }
    }

    public void generateContractModificationCode() {
        String code = RandomStringUtils.randomAlphanumeric(BLOCK_CODE_LENGTH).toUpperCase();
        getContract().setModificationCode(code);
    }

    public void unlockContractModificationCode() {
        if (null != getContract().getModificationCode() && getInputModificationCode().compareToIgnoreCase(getContract().getModificationCode()) == 0) {
            setModificationCodeUnlock(true);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "JobContract.info.unlockCode");
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "JobContract.error.invalidBlockCode");
        }
    }

    @Restrict("#{s:hasPermission('CONTRACTMODIFICATIONAUTHORIZATION','VIEW')}")
    public void enableContractModification() {
        Contract contract = getContract();
        contract.setContractModificationAuthorization(true);
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            getService().update(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return;
        } catch (ConcurrencyException e) {
            try {
                getInstance().setContract(getService().findById(Contract.class, contract.getId(), true));
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return;
            }
            addUpdateConcurrencyMessage();
            return;
        }
        addUpdatedMessage();
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void setShowExtension(boolean showExtension) {
        this.showExtension = showExtension;
    }

    public boolean isContractEditable() {
        boolean hasModificationAuthorizationByContract = getContract().getContractModificationAuthorization();
        boolean hasModificationAuthorizationByCompany;
        try {
            hasModificationAuthorizationByCompany = companyConfigurationService.findCompanyConfiguration().getContractModificationAuthorization();
        } catch (CompanyConfigurationNotFoundException e) {
            return false;
        }
        return (hasModificationAuthorizationByCompany && hasModificationAuthorizationByContract) || !hasModificationAuthorizationByCompany;
    }

    /**
     * Checks if there user have input the block code to enable edit of Employee's flagRetention, flagControl and Salary's info
     * or if the company configuration will consider this code
     *
     * @return true if there user have input the block code and if the company config is set to take into account this block code
     */
    public boolean isContractBlocked() {
        boolean result = !getModificationCodeUnlock() && isManaged();
        if (result) {
            return result;
        } else {
            try {
                result = !companyConfigurationService.findCompanyConfiguration().getContractModificationCode();
                return result;
            } catch (CompanyConfigurationNotFoundException e) {
                return result;
            }
        }
    }

    public String getInputModificationCode() {
        return null == inputModificationCode ? "" : inputModificationCode;
    }

    public void setInputModificationCode(String inputModificationCode) {
        this.inputModificationCode = inputModificationCode;
    }

    public Boolean getModificationCodeUnlock() {
        return modificationCodeUnlock;
    }

    public void setModificationCodeUnlock(Boolean modificationCodeUnlock) {
        this.modificationCodeUnlock = modificationCodeUnlock;
    }

/*MESSAGES*/

    protected void addContractModificationEnabled() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Contract.message.contractModificationEnabled");
    }

    protected void addContractModificationDisabled() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Contract.error.contractModificationDisabled");
    }

}
