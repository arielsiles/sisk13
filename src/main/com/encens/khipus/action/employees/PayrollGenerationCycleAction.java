package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.AFPRateType;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.ExchangeRate;
import com.encens.khipus.service.employees.GestionPayrollService;
import com.encens.khipus.service.employees.PayrollGenerationCycleService;
import com.encens.khipus.service.employees.TaxPayrollUtilService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.4
 */

@Name("payrollGenerationCycleAction")
@Scope(ScopeType.CONVERSATION)
public class PayrollGenerationCycleAction extends GenericAction<PayrollGenerationCycle> {

    @In
    private PayrollGenerationCycleService payrollGenerationCycleService;
    @In
    private TaxPayrollUtilService taxPayrollUtilService;

    @In
    private GestionPayrollService gestionPayrollService;

    private GestionPayroll administrativeGestionPayroll;

    private String selectedTab;

    private boolean includeActiveJobCategories = true;

    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean payrollGenerationCycleHasAllPayrollsAsOfficial;

    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean payrollGenerationCycleHasAllManagersAndFiscalProfessorsPayrollsAsOfficial;

    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean payrollGenerationCycleReadOnly;

    @In(create = true)
    private TributaryPayrollDataModel tributaryPayrollDataModel;

    @In(create = true)
    private FiscalPayrollDataModel fiscalPayrollDataModel;

    @Factory(value = "payrollGenerationCycle", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
    public PayrollGenerationCycle initPayrollGenerationCycle() {
        return getInstance();
    }

    @Create
    public void initialize() {
        if (!isManaged()) {
            putDefaultRates();
            /* here we set the exchangeRate value to a new value in order to avoid the null pointer exception*/
            if (getInstance().getExchangeRate() == null) {
                getInstance().setExchangeRate(new ExchangeRate());
            }
        }
    }

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
    public String select(PayrollGenerationCycle instance) {
        setOp(OP_UPDATE);
        setInstance(payrollGenerationCycleService.read(instance));
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','CREATE')}")
    public String create() {
        String validationOutcome = validateDefaultValues();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return Outcome.REDISPLAY;
        }
        try {
            payrollGenerationCycleService.createPayrollGenerationCycle(getInstance(), includeActiveJobCategories);
            addCreatedMessage();
            select(getInstance());
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }


    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','UPDATE')}")
    public String update() {
        String validationOutcome = validateDefaultValues();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return Outcome.REDISPLAY;
        }

        if (!validateIsReadOnly()) {
            return Outcome.REDISPLAY;
        }

        try {
            payrollGenerationCycleService.updatePayrollGenerationCycle(getInstance());
            addUpdatedMessage();
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.FAIL;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','DELETE')}")
    public String delete() {
        if (!validateIsReadOnly()) {
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public void gestionChanged() {
        updateName();
        putStartAndEndDates();
    }

    public void monthChanged() {
        updateName();
        putStartAndEndDates();
    }

    public void businessUnitChanged() {
        updateName();
    }

    private void updateName() {
        List<String> nameStringList = new ArrayList<String>();
        if (null != getInstance().getBusinessUnit()) {
            nameStringList.add(getInstance().getBusinessUnit().getOrganization().getName());
        }
        if (null != getInstance().getGestion()) {
            nameStringList.add(getInstance().getGestion().getYear().toString());
        }
        if (null != getInstance().getMonth()) {
            nameStringList.add(messages.get(getInstance().getMonth().getResourceKey()));
        }
        getInstance().setName(FormatUtils.concatLineSeparated(nameStringList));
    }

    @SuppressWarnings({"UnusedDeclaration", "NullableProblems"})
    public void putStartAndEndDates() {
        if (getInstance().getMonth() != null && getInstance().getGestion() != null) {
            Date startDate = DateUtils.getFirstDayOfMonth(getInstance().getMonth().getValue() + 1, getInstance().getGestion().getYear());
            Date endDate = DateUtils.getLastDayOfMonth(startDate.getTime());

            getInstance().setStartDate(startDate);
            getInstance().setEndDate(endDate);
            getInstance().setGenerationInitDate(startDate);
            getInstance().setGenerationEndDate(endDate);
        } else {
            getInstance().setStartDate(null);
            getInstance().setEndDate(null);
            getInstance().setGenerationInitDate(null);
            getInstance().setGenerationEndDate(null);
        }
    }

    public GestionPayroll getAdministrativeGestionPayroll() {
        return administrativeGestionPayroll;
    }

    public void setAdministrativeGestionPayroll(GestionPayroll administrativeGestionPayroll) {
        this.administrativeGestionPayroll = administrativeGestionPayroll;
    }

    public void assignAdministrativeGestionPayRoll(GestionPayroll gestionPayroll) {
        setAdministrativeGestionPayroll(gestionPayroll);
    }

    public void cleanAdministrativeGestionPayRoll() {
        setAdministrativeGestionPayroll(null);
    }

    private void putDefaultRates() {
        getInstance().setAfpRate(taxPayrollUtilService.getActiveAfpRate(AFPRateType.LABOR_CONTRIBUTION));
        getInstance().setNationalSolidaryAfpDiscountRule(taxPayrollUtilService.findActiveNationalSolidaryAfpDiscountRule());
        getInstance().setProfessionalRiskAfpRate(taxPayrollUtilService.getActiveAfpRate(AFPRateType.PATRONAL_CONTRIBUTION_PROFESSIONAL_RISKS));
        getInstance().setProHousingAfpRate(taxPayrollUtilService.getActiveAfpRate(AFPRateType.PATRONAL_CONTRIBUTION_PRO_HOUSING));
        getInstance().setSolidaryAfpRate(taxPayrollUtilService.getActiveAfpRate(AFPRateType.PATRONAL_CONTRIBUTION_SOLIDARY));
        getInstance().setCnsRate(taxPayrollUtilService.getActiveCnsRate());
        getInstance().setIvaRate(taxPayrollUtilService.getActiveIvaRate());
        getInstance().setSmnRate(taxPayrollUtilService.getActiveSmnRate());
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    public boolean isActiveForTaxPayrollGeneration() {
        return true;
    }

    private String validateDefaultValues() {
        String outcome = Outcome.SUCCESS;
        if (null == getInstance().getAfpRate()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("PayrollGenerationCycle.afpRate"));
            outcome = Outcome.FAIL;
        }

        if (null == getInstance().getProfessionalRiskAfpRate()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("PayrollGenerationCycle.afpRateProfessionalRisk"));
            outcome = Outcome.FAIL;
        }

        if (null == getInstance().getProHousingAfpRate()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("PayrollGenerationCycle.afpRateProHousing"));
            outcome = Outcome.FAIL;
        }
        if (null == getInstance().getSolidaryAfpRate()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("PayrollGenerationCycle.solidaryAfpRate"));
            outcome = Outcome.FAIL;
        }

        if (null == getInstance().getCnsRate()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("PayrollGenerationCycle.cnsRate"));
            outcome = Outcome.FAIL;
        }

        if (!isManaged()) {
            if (payrollGenerationCycleService.countByName(getInstance().getName()) > 0) {
                addNameDuplicatedMessage();
                outcome = Outcome.FAIL;
            }
            if (payrollGenerationCycleService.countByBusinessUnitAndGestionAndMonth(getInstance()) > 0) {
                addBusinessUnitGestionMonthDuplicatedMessage();
                outcome = Outcome.FAIL;
            }
        } else {
            if (payrollGenerationCycleService.countByNameButThis(getInstance().getName(), getInstance().getId()) > 0) {
                addNameDuplicatedMessage();
                outcome = Outcome.FAIL;
            }
            if (payrollGenerationCycleService.countByBusinessUnitAndGestionAndMonthButThis(getInstance()) > 0) {
                addBusinessUnitGestionMonthDuplicatedMessage();
                outcome = Outcome.FAIL;
            }

        }
        return outcome;
    }

    private void addNameDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PayrollGenerationCycle.error.nameDuplicated", getInstance().getName());
    }

    private void addBusinessUnitGestionMonthDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Common.message.duplicated", FormatUtils.concatDashSeparated(getInstance().getBusinessUnit().getOrganization().getName(),
                getInstance().getGestion().getYear(), getInstance().getMonth()));
    }

    public boolean isIncludeActiveJobCategories() {
        return includeActiveJobCategories;
    }

    public void setIncludeActiveJobCategories(boolean includeActiveJobCategories) {
        this.includeActiveJobCategories = includeActiveJobCategories;
    }

    public Boolean getReadOnly() {
        if (payrollGenerationCycleReadOnly == null) {
            payrollGenerationCycleReadOnly = isManaged() && payrollGenerationCycleService.isReadOnly(getInstance());
        }
        return payrollGenerationCycleReadOnly;
    }

    public Boolean validateIsReadOnly() {
        Boolean isReadOnly = payrollGenerationCycleService.isReadOnly(getInstance());
        if (isReadOnly) {
            select(getInstance());
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PayrollGenerationCycle.error.readOnly", getInstance().getName());
        }
        return !isReadOnly;
    }

    public Boolean getHasAllPayrollsAsOfficial() {
        if (payrollGenerationCycleHasAllPayrollsAsOfficial == null) {
            payrollGenerationCycleHasAllPayrollsAsOfficial = isManaged() && payrollGenerationCycleService.hasAllPayrollsAsOfficial(getInstance());
        }
        return payrollGenerationCycleHasAllPayrollsAsOfficial;
    }

    public Boolean getHasAllManagersAndFiscalProfessorsPayrollsAsOfficial() {
        if (payrollGenerationCycleHasAllManagersAndFiscalProfessorsPayrollsAsOfficial == null) {
            payrollGenerationCycleHasAllManagersAndFiscalProfessorsPayrollsAsOfficial = isManaged() && payrollGenerationCycleService.hasAllPayrollsAsOfficialByGenerationType(getInstance(), PayrollGenerationType.getSalaryAndPeriodSalaryValues());
        }
        return payrollGenerationCycleHasAllManagersAndFiscalProfessorsPayrollsAsOfficial;
    }

    /**
     * Redirect to tributary payroll view
     *
     * @param payrollGenerationCycle
     * @return String
     */
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewTributaryPayroll(PayrollGenerationCycle payrollGenerationCycle) {
        tributaryPayrollDataModel.setPayrollGenerationCycle(payrollGenerationCycle);
        return Outcome.SUCCESS;
    }

    /**
     * Redirect to fiscal payroll view
     *
     * @param payrollGenerationCycle
     * @return String
     */
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewFiscalPayroll(PayrollGenerationCycle payrollGenerationCycle) {
        fiscalPayrollDataModel.setPayrollGenerationCycle(payrollGenerationCycle);
        return Outcome.SUCCESS;
    }

    /**
     * Verify if this cycle has generated tributary payroll
     *
     * @return true or false
     */
    public Boolean hasTributaryPayroll() {
        return payrollGenerationCycleService.hasTributaryPayroll(getInstance());
    }

    /**
     * Verify if this cycle has generated fiscal payroll
     *
     * @return true or false
     */
    public Boolean hasFiscalPayroll() {
        return payrollGenerationCycleService.hasFiscalPayroll(getInstance());
    }

}