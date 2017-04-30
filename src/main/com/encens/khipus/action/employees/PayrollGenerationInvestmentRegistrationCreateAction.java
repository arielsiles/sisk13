package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.SocialWelfareEntity;
import com.encens.khipus.model.finances.PayableDocumentType;
import com.encens.khipus.service.employees.PayrollGenerationCycleService;
import com.encens.khipus.service.employees.PayrollGenerationInvestmentRegistrationService;
import com.encens.khipus.service.employees.TributaryPayrollService;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.employees.TributaryPayrollCalculateResult;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.5
 */
@Name("payrollGenerationInvestmentRegistrationCreateAction")
@Scope(ScopeType.CONVERSATION)
public class PayrollGenerationInvestmentRegistrationCreateAction extends GenericAction<PayrollGenerationInvestmentRegistrationCreateAction> {
    @In
    private PayrollGenerationInvestmentRegistrationService payrollGenerationInvestmentRegistrationService;
    @In(required = false)
    private PayrollGenerationCycleAction payrollGenerationCycleAction;
    @In
    private TributaryPayrollService tributaryPayrollService;
    @In
    private PayrollGenerationCycleService payrollGenerationCycleService;
    @In(create = true)
    private EntityQuery socialWelfareEntityQuery;
    private List<SocialWelfareEntity> socialWelfareEntityList;
    private Map<Long, BigDecimal> socialWelfareEntityValues = null;
    private PayableDocumentType payableDocumentType;

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("PayrollGenerationInvestmentRegistration.list");
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String newInstance() {
        if (hasPayrollGenerationInvestmentRegistration() || hasUnregisteredSocialWelfareEntities()) {
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    public String generateRegistration() {
        if (hasPayrollGenerationInvestmentRegistration() || hasUnregisteredSocialWelfareEntities()) {
            return Outcome.FAIL;
        }
        try {

            payrollGenerationInvestmentRegistrationService.createInvestmentRegistrations(
                    payrollGenerationCycleAction.getInstance(),
                    getSocialWelfareEntityValues(),
                    getPayableDocumentType()
            );
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public Map<Long, BigDecimal> getSocialWelfareEntityValues() {
        if (socialWelfareEntityValues == null) {
            socialWelfareEntityValues = new HashMap<Long, BigDecimal>();
            addSocialWelfareEntityValues(tributaryPayrollService.sumGlobalPensionFundRetentionGroupingBySocialWelfareEntity(payrollGenerationCycleAction.getInstance()));
            addSocialWelfareEntityValues(tributaryPayrollService.sumGlobalSocialSecurityRetentionGroupingBySocialWelfareEntity(payrollGenerationCycleAction.getInstance()));
        }

        return socialWelfareEntityValues;
    }

    public void setSocialWelfareEntityValues(Map<Long, BigDecimal> socialWelfareEntityValues) {
        this.socialWelfareEntityValues = socialWelfareEntityValues;
    }

    public PayableDocumentType getPayableDocumentType() {
        return payableDocumentType;
    }

    public void setPayableDocumentType(PayableDocumentType payableDocumentType) {
        this.payableDocumentType = payableDocumentType;
    }

    private void addSocialWelfareEntityValues(List<TributaryPayrollCalculateResult> calculateResultList) {
        for (TributaryPayrollCalculateResult result : calculateResultList) {
            socialWelfareEntityValues.put(result.getSocialWelfareEntityId(), result.getAmount());
        }
    }

    public List<SocialWelfareEntity> getSocialWelfareEntityList() {
        if (socialWelfareEntityList == null) {
            socialWelfareEntityList = socialWelfareEntityQuery.getResultList();
        }
        return socialWelfareEntityList;
    }

    public void setSocialWelfareEntityList(List<SocialWelfareEntity> socialWelfareEntityList) {
        this.socialWelfareEntityList = socialWelfareEntityList;
    }

    public Integer getSocialWelfareEntityRowCounter() {
        if (socialWelfareEntityList == null) {
            socialWelfareEntityList = socialWelfareEntityQuery.getResultList();
        }
        return socialWelfareEntityList.size() + 1;
    }

    private Boolean hasPayrollGenerationInvestmentRegistration() {
        if (payrollGenerationCycleService.hasPayrollGenerationInvestmentRegistration(payrollGenerationCycleAction.getInstance())) {
            addHasInvestmentRegistrationErrorMessage();
            return true;
        }
        return false;
    }

    private void addHasInvestmentRegistrationErrorMessage() {
        String businessUnitName = payrollGenerationCycleAction.getInstance().getBusinessUnit().getOrganization().getName();
        String monthString = MessageUtils.getMessage(payrollGenerationCycleAction.getInstance().getMonth().getResourceKey());
        String yearString = String.valueOf(payrollGenerationCycleAction.getInstance().getGestion().getYear()).replace(".", "");
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PayrollGenerationInvestmentRegistration.error.hasInvestmentRegistration",
                businessUnitName, monthString, yearString);
    }

    private Boolean hasUnregisteredSocialWelfareEntities() {
        Boolean hasEUnregisteredElements = false;
        if (tributaryPayrollService.hasUnregisteredPensionFundOrganizations(payrollGenerationCycleAction.getInstance())) {
            addUnregisteredSocialWelfareEntitiesErrorMessage("PayrollGenerationInvestmentRegistration.error.hasUnregisteredPensionFundOrganizations");
            hasEUnregisteredElements = true;
        }
        if (tributaryPayrollService.hasUnregisteredSocialSecurityOrganizations(payrollGenerationCycleAction.getInstance())) {
            addUnregisteredSocialWelfareEntitiesErrorMessage("PayrollGenerationInvestmentRegistration.error.hasUnregisteredSocialSecurityOrganizations");
            hasEUnregisteredElements = true;
        }
        return hasEUnregisteredElements;
    }

    private void addUnregisteredSocialWelfareEntitiesErrorMessage(String resourceKey) {
        String businessUnitName = payrollGenerationCycleAction.getInstance().getBusinessUnit().getOrganization().getName();
        String monthString = MessageUtils.getMessage(payrollGenerationCycleAction.getInstance().getMonth().getResourceKey());
        String yearString = String.valueOf(payrollGenerationCycleAction.getInstance().getGestion().getYear()).replace(".", "");
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                resourceKey,
                businessUnitName, monthString, yearString);
    }
}
