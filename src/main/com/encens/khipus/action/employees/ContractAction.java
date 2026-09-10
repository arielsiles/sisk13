package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.MainContractResult;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.customers.ExtensionService;
import com.encens.khipus.service.employees.ContractConditionService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

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

    @In
    private ContractConditionService contractConditionService;

    @Factory(value = "contract", scope = ScopeType.STATELESS)
    public Contract initContract() {
        return getInstance();
    }

    /**
     * Un contrato nuevo nace marcado como principal. Es el caso comun -la mayoria de la gente
     * tiene uno solo- y asi no depende de que alguien se acuerde de tildarlo: sin principal no
     * corresponden AFP ni vacaciones. Para un eventual, RRHH lo desmarca.
     */
    @Override
    public Contract createInstance() {
        Contract instance = super.createInstance();
        instance.setMainContract(Boolean.TRUE);
        return instance;
    }

    /* Alta, edicion y borrado de un contrato cambian la respuesta a "esta persona sigue en la
       empresa". El caso que obliga a esto es el reingreso: se da de baja, despues se le crea un
       contrato nuevo, y sin recalcular la fecha de salida queda puesta para siempre. La edicion
       tambien cuenta porque esta pantalla deja cambiar el estado del contrato a mano, sin pasar
       por Dar de baja. */

    @Override
    @End
    public String create() {
        /* Se mira ANTES de guardar: despues el contrato nuevo ya cuenta como abierto y las dos
           situaciones se vuelven indistinguibles. */
        boolean reentry = null != getInstance().getEmployee()
                && contractConditionService.findOpenContracts(getInstance().getEmployee(), null).isEmpty();

        String outcome = super.create();
        if (Outcome.SUCCESS.equals(outcome)) {
            contractConditionService.refreshRetireDate(getInstance());
            applyMainContract();
            announceEntry(reentry);
        }
        return outcome;
    }

    @Override
    @End
    public String update() {
        String outcome = super.update();
        if (Outcome.SUCCESS.equals(outcome)) {
            contractConditionService.refreshRetireDate(getInstance());
            applyMainContract();
        }
        return outcome;
    }

    /**
     * Deja la marca de principal consistente y lo dice. Nunca designa por su cuenta: si la
     * persona queda sin contrato principal avisa, porque sin principal no corresponden AFP ni
     * vacaciones y eso no puede pasar por olvido.
     */
    private void applyMainContract() {
        try {
            MainContractResult result = contractConditionService.applyMainContract(getInstance());
            String employeeName = null == getInstance().getEmployee()
                    ? "" : getInstance().getEmployee().getFullName();
            StatusMessage.Severity severity = MainContractResult.WITHOUT_MAIN.equals(result)
                    ? StatusMessage.Severity.WARN : StatusMessage.Severity.INFO;
            facesMessages.addFromResourceBundle(severity, result.getResourceKey(), employeeName);
        } catch (Exception e) {
            log.error("No se pudo aplicar la regla del contrato principal", e);
        }
    }

    /**
     * Crear un contrato para alguien sin contratos activos es un reingreso; para alguien que ya
     * tiene uno, un contrato adicional. Hoy las dos se hacen igual y ninguna avisa nada, que es
     * como se terminan cargando contratos duplicados creyendo que son altas nuevas.
     */
    private void announceEntry(boolean reentry) {
        Employee owner = getInstance().getEmployee();
        if (null == owner) {
            return;
        }
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                reentry ? "Contract.info.reentry" : "Contract.info.additional",
                owner.getFullName());
    }

    @Override
    @End
    public String delete() {
        Employee owner = getInstance().getEmployee();
        String outcome = super.delete();
        contractConditionService.refreshRetireDate(owner);
        return outcome;
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