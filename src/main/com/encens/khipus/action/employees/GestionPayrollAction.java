package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.ExchangeRate;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import com.encens.khipus.service.employees.GestionPayrollService;
import com.encens.khipus.service.employees.GestionService;
import com.encens.khipus.service.employees.PayrollGenerationCycleService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.employees.PayrollGenerationResult;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Actions for GestionPayroll
 *
 * @author
 * @version 3.2
 */

@Name("gestionPayrollAction")
@Scope(ScopeType.CONVERSATION)
public class GestionPayrollAction extends GenericAction<GestionPayroll> {

    @In
    private GeneratedPayrollService generatedPayrollService;
    @In
    private GestionPayrollService gestionPayrollService;
    @In
    private GestionService gestionService;

    @In(required = false)
    private PayrollGenerationCycleAction payrollGenerationCycleAction;

    @In
    private PayrollGenerationCycleService payrollGenerationCycleService;

    private Boolean gestionPayrollReadOnly = false;

    private GeneratedPayroll generatedPayroll = new GeneratedPayroll();

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    @Create
    public void atCreateTime() {
        getInstance().setGestionPayrollType(GestionPayrollType.SALARY);
        getInstance().setInitDate(new Date());
        getInstance().setEndDate(new Date());
        getInstance().setGenerationDeadline(new Date());
        getInstance().setGenerationBeginning(new Date());
        getInstance().setOfficialPayrollDeadline(new Date());
        if (getInstance().getExchangeRate() == null) {
            getInstance().setExchangeRate(new ExchangeRate());
        }
    }

    @Factory(value = "gestionPayroll", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('GESTIONPAYROLL','VIEW')}")
    public GestionPayroll initGestionPayroll() {
        /* here we set the exchangeRate value to a new value in order to avoid the null pointer exception*/
        if (getInstance().getExchangeRate() == null) {
            getInstance().setExchangeRate(new ExchangeRate());
        }
        return getInstance();
    }

    @Factory(value = "month", scope = ScopeType.STATELESS)
    public Month[] getMonth() {
        return Month.values();
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String newInstance() {
        //noinspection NullableProblems
        setInstance(null);
        atCreateTime();
        setOp(OP_CREATE);
        getInstance();
        apllyTemplate();
        return Outcome.SUCCESS;
    }

    private void apllyTemplate() {
        if (null != payrollGenerationCycleAction && null != payrollGenerationCycleAction.getInstance()) {
            payrollGenerationCycleService.applyTemplate(getInstance(), getInstance().getExchangeRate(), payrollGenerationCycleAction.getInstance());
            getInstance().setPayrollGenerationCycle(payrollGenerationCycleAction.getInstance());
        }
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('GESTIONPAYROLL','CREATE')}")
    public String create() {
        if (getInstance().getExchangeRate().getDate() == null) {
            getInstance().getExchangeRate().setDate((Calendar.getInstance().getTime()));
        }
        if (getInstance().getExchangeRate().getSale() == null) {
            getInstance().getExchangeRate().setSale(getInstance().getExchangeRate().getRate());
        }
        if (getInstance().getExchangeRate().getPurchase() == null) {
            getInstance().getExchangeRate().setPurchase(getInstance().getExchangeRate().getRate());
        }
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        /*set the instance to create the new entity*/
        return super.create();
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('GESTIONPAYROLL','VIEW')}")
    public String select(GestionPayroll instance) {
        String outcome = super.select(instance);
        setGestionPayrollReadOnly(generatedPayrollService.hasGeneratedPayrolls(getInstance()));
        return outcome;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "gestionName";
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('GESTIONPAYROLL','UPDATE')}")
    public String update() {
        /* if there is at least one generated payroll, it is not possible to upodate the payroll*/
        if (!getGestionPayrollReadOnly() && generatedPayrollService.hasGeneratedPayrolls(getInstance())) {
            addCannotUpdatedMessage();
            return Outcome.REDISPLAY;
        }

        if (getInstance().getExchangeRate().getPurchase().compareTo(getInstance().getExchangeRate().getRate()) != 0) {
            getInstance().getExchangeRate().setPurchase(getInstance().getExchangeRate().getRate());
            getInstance().getExchangeRate().setSale(getInstance().getExchangeRate().getRate());
        }
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('GESTIONPAYROLL','DELETE')}")
    public String delete() {
        return super.delete();
    }

    public void gestionChanged() {
        putStartAndEndDates();
        updateName();
    }

    public void monthChanged() {
        putStartAndEndDates();
        updateName();
    }

    public void jobCategoryChanged() {
        updateName();
    }

    @SuppressWarnings({"NullableProblems"})
    public void putStartAndEndDates() {
        if (getInstance().getMonth() != null && getInstance().getGestion() != null) {
            Date startDate = DateUtils.getFirstDayOfMonth(getInstance().getMonth().getValue() + 1, getInstance().getGestion().getYear());
            Date endDate = DateUtils.getLastDayOfMonth(startDate.getTime());

            getInstance().setInitDate(startDate);
            getInstance().setEndDate(endDate);
        } else {
            getInstance().setInitDate(null);
            getInstance().setEndDate(null);
        }
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
        if (null != getInstance().getJobCategory()) {
            nameStringList.add(getInstance().getJobCategory().getName());
        }
        if (null != getInstance().getGestion() && null != getInstance().getMonth()) {
            nameStringList.add(DateUtils.format(getInstance().getInitDate(), MessageUtils.getMessage("patterns.date")));
            nameStringList.add(DateUtils.format(getInstance().getEndDate(), MessageUtils.getMessage("patterns.date")));
        }
        getInstance().setGestionName(FormatUtils.concatLineSeparated(nameStringList));
    }

    protected void addCannotUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "GestionPayroll.error.cannotUpdate", getInstance().getGestionName());
    }

    public void addGenerationDateRangeMessage() {
        String beginningString = getInstance().getGenerationBeginning() != null ?
                DateUtils.format(getInstance().getGenerationBeginning(), MessageUtils.getMessage("patterns.date")) : "";
        String deadlineString = getInstance().getGenerationDeadline() != null ?
                DateUtils.format(getInstance().getGenerationDeadline(), MessageUtils.getMessage("patterns.date")) : "";
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "GestionPayroll.error.generationDateRange",
                getInstance().getGestionName(),
                beginningString,
                deadlineString);
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String generatePayroll(GestionPayroll instance) {
        String outcome = super.select(instance);
        /*in order to show thw exchangeRate value when retrieved from exchangeRateList*/
        if (!gestionPayrollService.hasValidGenerationDateRange(instance)) {
            addGenerationDateRangeMessage();
            return Outcome.REDISPLAY;
        }
        generatedPayroll = new GeneratedPayroll();
        generatedPayroll.setName(gestionPayrollService.getNextGeneratedPayrollName(instance));
        generatedPayroll.setExchangeRate(new ExchangeRate());
        generatedPayroll.getExchangeRate().setDate(new Date());
        generatedPayroll.getExchangeRate().setRate(instance.getExchangeRate().getRate());
        if (null != payrollGenerationCycleAction && null != payrollGenerationCycleAction.getInstance()) {
            generatedPayroll.setPayrollGenerationCycle(payrollGenerationCycleAction.getInstance());
        }

        return outcome;
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String viewPayroll(GestionPayroll instance) {
        return super.select(instance);
    }

    @End(beforeRedirect = true)
    public String generatePayroll() throws InterruptedException {
        GestionPayroll gestionPayroll = getInstance();
        generatedPayroll.setGeneratedPayrollType(GeneratedPayrollType.TEST);

        if (generatedPayrollService == null) {
            return null;
        }

        if (!gestionPayrollService.hasValidGenerationDateRange(gestionPayroll)) {
            addGenerationDateRangeMessage();
            return Outcome.REDISPLAY;
        }

        if (generatedPayrollService.countGeneratedPayrollByName(generatedPayroll) > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.nameAlreadyExist", generatedPayroll.getName());
            generatedPayroll.setName(gestionPayrollService.getNextGeneratedPayrollName(gestionPayroll));
            return Outcome.REDISPLAY;
        }

        if (generatedPayrollService.countGeneratedPayrollByGestionPayroll(gestionPayroll, GeneratedPayrollType.OFFICIAL) > 0) {
            /*return can not be more than one official for the gestion*/
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.officialAlreadyExist");
            return Outcome.REDISPLAY;
        }
        if (gestionPayroll.isChristmasBonusType()) {
            List<Month> lastThreeMonthList = new ArrayList<Month>();
            lastThreeMonthList.add(Month.SEPTEMBER);
            lastThreeMonthList.add(Month.OCTOBER);
            lastThreeMonthList.add(Month.NOVEMBER);
            boolean validator = true;
            //get the september, october and november official generated payrolls
            for (Month month : lastThreeMonthList) {
                Gestion gestion = gestionService.getGestion(gestionPayroll.getGestion().getYear());
                @SuppressWarnings({"NullableProblems"})
                GestionPayroll lastGestionPayroll = gestionPayrollService.findGestionPayrollByGestionAndBusinessUnitAndMonthAndJobCategory(
                        gestion, month, gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory(), null);
                GeneratedPayroll lastGeneratedPayroll = null;
                List<GeneratedPayroll> generatedPayrollList = generatedPayrollService.findOfficialGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType(lastGestionPayroll);
                if (!ValidatorUtil.isEmptyOrNull(generatedPayrollList)) {
                    lastGeneratedPayroll = generatedPayrollService.findOfficialGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType(lastGestionPayroll).get(0);
                }
                if (null == lastGeneratedPayroll) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.missingOfficialPayroll", messages.get(month.getResourceKey()));
                    validator = false;
                }
            }
            if (!validator) {
                return Outcome.REDISPLAY;
            }
        }

        try {
            generatedPayroll.setGestionPayroll(gestionPayroll);
            generatedPayroll.getExchangeRate().setPurchase(generatedPayroll.getExchangeRate().getRate());
            generatedPayroll.getExchangeRate().setSale(generatedPayroll.getExchangeRate().getRate());

            PayrollGenerationResult payrollGenerationResult;
            // in case it is a salary payroll generation either for employees of type (by Salary or by Time)
            if (gestionPayroll.isSalaryType()) {
                payrollGenerationResult = generatedPayrollService.fillPayroll(generatedPayroll);
            } else {
                // in case it is a christmas payroll
                payrollGenerationResult = generatedPayrollService.fillChristmasPayroll(generatedPayroll);
            }

            if (!PayrollGenerationResult.SUCCESS.equals(payrollGenerationResult)) {
                if (PayrollGenerationResult.WITHOUT_CONTRACTS.equals(payrollGenerationResult)) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.withoutContracts", payrollGenerationResult.getResultData()[0]);
                    return Outcome.FAIL;
                } else if (PayrollGenerationResult.WITHOUT_BANDS.equals(payrollGenerationResult)) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.withoutBands", payrollGenerationResult.getResultData()[0]);
                    return Outcome.FAIL;
                } else if (PayrollGenerationResult.FAIL.equals(payrollGenerationResult)) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.generationAborted");
                    return Outcome.FAIL;
                }
            }
        } catch (Exception e) {
            unexpectedErrorLog(e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.generationAborted");
            return Outcome.FAIL;
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "GeneratedPayroll.info.generationSucceed");
        return Outcome.SUCCESS;
    }

    /**
     * pre validate the create and update conditions
     *
     * @return true if success
     */
    private boolean validate() {
        boolean result = true;
        if (getInstance().isChristmasBonusType()) {
            // verify unique by Business Unit,Gestion, JobCategory and GestionPayrollType
            Long count;
            List<Long> idList = new ArrayList<Long>();
            if (isManaged()) {
                idList.add(getInstance().getId());
                count = gestionPayrollService.countByGestionAndBusinessUnitAndJobCategoryAndTypeNotInIdList(
                        getInstance().getGestion(), getInstance().getBusinessUnit(),
                        getInstance().getJobCategory(), getInstance().getGestionPayrollType(), idList, true);
            } else {
                count = gestionPayrollService.countByGestionAndBusinessUnitAndJobCategoryAndType(
                        getInstance().getGestion(), getInstance().getBusinessUnit(),
                        getInstance().getJobCategory(), getInstance().getGestionPayrollType(), true);
            }
            if (count > 0) {
                result = false;
                addGestionPayrollAlreadyEsxits();
            }
        } else {
            if (getInstance().getJobCategory().getPayrollGenerationType().equals(PayrollGenerationType.GENERATION_BY_SALARY)) {
                List<Long> idList = new ArrayList<Long>();
                Long count;
                if (isManaged()) {
                    idList.add(getInstance().getId());
                    count = gestionPayrollService.countByBusinessUnitAndGestionAndMonthAndJobCategoryAndTypeAndNotInList(
                            getInstance().getBusinessUnit(),
                            getInstance().getGestion(),
                            getInstance().getMonth(),
                            getInstance().getJobCategory(), getInstance().getGestionPayrollType(),
                            idList, true);
                } else {
                    count = gestionPayrollService.countByBusinessUnitAndGestionAndMonthAndJobCategoryAndType(
                            getInstance().getBusinessUnit(),
                            getInstance().getGestion(),
                            getInstance().getMonth(),
                            getInstance().getJobCategory(), getInstance().getGestionPayrollType(),
                            true);
                }

                if (count > 0) {
                    //show message indicating duplicity
                    addDuplicateGestionPayrollMessage();
                    result = false;
                }
            }
        }
        return result;
    }

    @SuppressWarnings({"NullableProblems"})
    public void gestionPayrollTypeChanged() {
        if (isShowSalaryFields()) {
            getInstance().setInitDate(new Date());
            getInstance().setEndDate(new Date());
        } else {
            getInstance().setInitDate(null);
            getInstance().setEndDate(null);
            getInstance().setMonth(null);
        }
    }

    public Boolean hasOfficialGeneration(GestionPayroll gestionPayroll) {
        return gestionPayrollService.hasOfficialGeneration(gestionPayroll);
    }

    public Boolean getGestionPayrollReadOnly() {
        return gestionPayrollReadOnly;
    }

    public void setGestionPayrollReadOnly(Boolean gestionPayrollReadOnly) {
        this.gestionPayrollReadOnly = gestionPayrollReadOnly;
    }

    public boolean isShowSalaryFields() {
        return null == getInstance().getGestionPayrollType() ||
                getInstance().getGestionPayrollType().equals(GestionPayrollType.SALARY);
    }


    private void addGestionPayrollAlreadyEsxits() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GestionPayroll.error.alreadyExists",
                messages.get(getInstance().getGestionPayrollType().getResourceKey()),
                getInstance().getBusinessUnit().getFullName(),
                getInstance().getJobCategory().getFullName(),
                getInstance().getGestion().getYear());
    }

    private void addDuplicateGestionPayrollMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GestionPayroll.error.duplicate",
                messages.get(getInstance().getGestionPayrollType().getResourceKey()),
                getInstance().getBusinessUnit().getFullName(),
                getInstance().getGestion().getYear(),
                messages.get(getInstance().getMonth().getResourceKey()),
                getInstance().getJobCategory().getFullName()
        );
    }

}