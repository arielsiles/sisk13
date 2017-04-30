package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.customers.ExtensionService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Contract action class
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("contractAction")
@Scope(ScopeType.CONVERSATION)
public class ContractAction extends GenericAction<Contract> {

    public static final int BLOCK_CODE_LENGTH = 6;
    private Employee employee;
    private boolean showExtension = false;
    private Boolean modificationCodeUnlock = false;
    public List<Extension> extensionList;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private ExtensionService extensionService;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Factory(value = "contract", scope = ScopeType.STATELESS)
    public Contract initContract() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('CONTRACT','VIEW')}")
    public String select(Contract instance) {
        String outcome = super.select(instance);
        if (Outcome.SUCCESS.equals(outcome)) {
            try {
                setEmployee(getService().findById(Employee.class, getInstance().getEmployeeId()));
            } catch (EntryNotFoundException e) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        }
        updateShowExtension();
        return outcome;    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getDisplayNameProperty() {
        return "numberOfContract";
    }

    public void relation() {
        String c = "SELECT c " +
                " FROM Contract c ORDER BY c.employee.id ";

        String e = "SELECT e " +
                " FROM Employee e WHERE e.id=:employeeId";


        Query query = em.createQuery(c);
        List<Contract> resultsC = query.getResultList();

        for (Contract contract : resultsC) {

            Query employeeQuery = em.createQuery(e);
            employeeQuery.setParameter("employeeId", contract.getEmployee().getId());

            Employee employee = (Employee) employeeQuery.getSingleResult();
            contract.setEmployee(employee);
            employee.getContractList().add(contract);
            em.close();
        }
    }

    public void showRelation() {

        String c = "SELECT c " +
                " FROM Contract c ORDER BY c.employee.id";

        Query query2 = em.createQuery(c);
        query2.getResultList();
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void setShowExtension(boolean showExtension) {
        this.showExtension = showExtension;
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

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getEmployee().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getEmployee().setExtensionSite(null);
        }
    }

    public boolean isContractEditable() {
        boolean hasModificationAuthorizationByContract = getInstance().getContractModificationAuthorization();
        boolean hasModificationAuthorizationByCompany;
        try {
            hasModificationAuthorizationByCompany = companyConfigurationService.findCompanyConfiguration().getContractModificationAuthorization();
        } catch (CompanyConfigurationNotFoundException e) {
            return false;
        }
        return (hasModificationAuthorizationByCompany && hasModificationAuthorizationByContract) || !hasModificationAuthorizationByCompany;
    }

    public void clearPensionFundInfo() {
        clearPensionFundOrganization();
        getInstance().setPensionFundRegistrationCode(null);
    }

    public void generateContractModificationCode() {
        String code = RandomStringUtils.randomAlphanumeric(BLOCK_CODE_LENGTH).toUpperCase();
        getInstance().setModificationCode(code);
    }

    public void clearPensionFundOrganization() {
        getInstance().setPensionFundOrganization(null);
    }

    public Boolean getModificationCodeUnlock() {
        return modificationCodeUnlock;
    }

    public void setModificationCodeUnlock(Boolean modificationCodeUnlock) {
        this.modificationCodeUnlock = modificationCodeUnlock;
    }
}