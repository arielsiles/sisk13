package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.GestionPayrollOfficialPayrollDeadlineException;
import com.encens.khipus.exception.employees.PayrollSelectItemsEmptyException;
import com.encens.khipus.exception.employees.PayrollSelectItemsHasAccountingRecordException;
import com.encens.khipus.exception.employees.UpdateActivePaymentException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.employees.BankAccountService;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import com.encens.khipus.util.MapUtil;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.util.List;


/**
 * Actions for GeneratedPayroll
 *
 * @author
 * @version 2.19
 */

@Name("generatedPayrollAction")
@Scope(ScopeType.CONVERSATION)
public class GeneratedPayrollAction extends GenericAction<GeneratedPayroll> {

    private GeneratedPayrollType generatedPayrollTypeSelected;
    @In
    private GeneratedPayrollService generatedPayrollService;
    @Out(required = false)
    @In(required = false)
    private GestionPayroll gestionPayroll;
    @Out(required = false)
    @In(required = false)
    private GeneralPayrollDataModel generalPayrollDataModel;
    @Out(required = false)
    @In(required = false)
    private ManagersPayrollDataModel managersPayrollDataModel;
    @In(required = false)
    @Out(required = false)
    private FiscalProfessorPayrollDataModel fiscalProfessorPayrollDataModel;
    @In(required = false)
    @Out(required = false)
    private ChristmasPayrollDataModel christmasPayrollDataModel;
    @In
    private BankAccountService bankAccountService;

    @In(required = false)
    private PayrollGenerationCycleAction payrollGenerationCycleAction;

    @Factory(value = "generatedPayroll", scope = ScopeType.STATELESS)
    public GeneratedPayroll initGeneratedPayroll() {
        return getInstance();
    }

    @Factory(value = "generatedPayrollTypeEnum")
    public GeneratedPayrollType[] getGeneratedPayrollTypeEnum() {
        return GeneratedPayrollType.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @SuppressWarnings({"unchecked"})
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewProfessorsGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        List<Long> resultList = generatedPayrollService.getSelectIdList(GeneralPayroll.class, generatedPayroll);
        generalPayrollDataModel = new GeneralPayrollDataModel();
        generalPayrollDataModel.getSelectedList().put(1, MapUtil.createByDefaultValue(resultList, Boolean.TRUE));
        return select(generatedPayroll);
    }

    @SuppressWarnings({"unchecked"})
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewManagersGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        List<Long> resultList = generatedPayrollService.getSelectIdList(ManagersPayroll.class, generatedPayroll);
        managersPayrollDataModel = new ManagersPayrollDataModel();
        managersPayrollDataModel.getSelectedList().put(1, MapUtil.createByDefaultValue(resultList, Boolean.TRUE));
        return select(generatedPayroll);
    }

    @SuppressWarnings({"unchecked"})
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewFiscalProfessorGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        List<Long> resultList = generatedPayrollService.getSelectIdList(FiscalProfessorPayroll.class, generatedPayroll);
        fiscalProfessorPayrollDataModel = new FiscalProfessorPayrollDataModel();
        fiscalProfessorPayrollDataModel.getSelectedList().put(1, MapUtil.createByDefaultValue(resultList, Boolean.TRUE));
        return select(generatedPayroll);
    }

    @SuppressWarnings({"unchecked"})
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewChristmasGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        List<Long> resultList = generatedPayrollService.getSelectIdList(ChristmasPayroll.class, generatedPayroll);
        christmasPayrollDataModel = new ChristmasPayrollDataModel();
        christmasPayrollDataModel.getSelectedList().put(1, MapUtil.createByDefaultValue(resultList, Boolean.TRUE));
        return select(generatedPayroll);
    }

    /**
     * Redirect to category tributary payroll view
     *
     * @param generatedPayroll
     * @return String
     */
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewCategoryTributaryPayroll(GeneratedPayroll generatedPayroll) {

        CategoryTributaryPayrollDataModel categoryTributaryPayrollDataModel = (CategoryTributaryPayrollDataModel) Component.getInstance("categoryTributaryPayrollDataModel", true);
        categoryTributaryPayrollDataModel.setGeneratedPayroll(generatedPayroll);
        return Outcome.SUCCESS;
    }

    /**
     * Redirect to category fiscal payroll view
     *
     * @param generatedPayroll
     * @return String
     */
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String viewCategoryFiscalPayroll(GeneratedPayroll generatedPayroll) {

        CategoryFiscalPayrollDataModel categoryFiscalPayrollDataModel = (CategoryFiscalPayrollDataModel) Component.getInstance("categoryFiscalPayrollDataModel", true);
        categoryFiscalPayrollDataModel.setGeneratedPayroll(generatedPayroll);
        return Outcome.SUCCESS;
    }

    @SuppressWarnings({"unchecked"})
    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public void updateGeneralPayrollSelectItems() {
        try {
            List<Integer> successUpdateList = generatedPayrollService.updateActivePaymentToPayrollItems(
                    GeneralPayroll.class, getInstance(), generalPayrollDataModel.getSelectedIdList());
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GeneratedPayroll.info.updateActivePayment",
                    successUpdateList.get(0),
                    successUpdateList.get(1));
        } catch (PayrollSelectItemsEmptyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.selectItemsEmpty");
        } catch (PayrollSelectItemsHasAccountingRecordException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.hasAccountingRecord");
        } catch (UpdateActivePaymentException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.updateActivePayment");
        }
    }

    @SuppressWarnings({"unchecked"})
    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public void updateManagersPayrollSelectItems() {
        try {
            List<Integer> successUpdateList = generatedPayrollService.updateActivePaymentToPayrollItems(
                    ManagersPayroll.class, getInstance(), managersPayrollDataModel.getSelectedIdList());
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GeneratedPayroll.info.updateActivePayment",
                    successUpdateList.get(0),
                    successUpdateList.get(1));
        } catch (PayrollSelectItemsEmptyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.selectItemsEmpty");
        } catch (PayrollSelectItemsHasAccountingRecordException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.hasAccountingRecord");
        } catch (UpdateActivePaymentException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.updateActivePayment");
        }
    }

    @SuppressWarnings({"unchecked"})
    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public void updateFiscalProfessorPayrollSelectItems() {
        try {
            List<Integer> successUpdateList = generatedPayrollService.updateActivePaymentToPayrollItems(
                    FiscalProfessorPayroll.class, getInstance(), fiscalProfessorPayrollDataModel.getSelectedIdList());
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GeneratedPayroll.info.updateActivePayment",
                    successUpdateList.get(0),
                    successUpdateList.get(1));
        } catch (PayrollSelectItemsEmptyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.selectItemsEmpty");
        } catch (PayrollSelectItemsHasAccountingRecordException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.hasAccountingRecord");
        } catch (UpdateActivePaymentException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.updateActivePayment");
        }
    }

    @SuppressWarnings({"unchecked"})
    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public void updateChristmasPayrollSelectItems() {
        try {
            List<Integer> successUpdateList = generatedPayrollService.updateActivePaymentToPayrollItems(
                    ChristmasPayroll.class, getInstance(), christmasPayrollDataModel.getSelectedIdList());
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "GeneratedPayroll.info.updateActivePayment",
                    successUpdateList.get(0),
                    successUpdateList.get(1));
        } catch (PayrollSelectItemsEmptyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.selectItemsEmpty");
        } catch (PayrollSelectItemsHasAccountingRecordException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.hasAccountingRecord");
        } catch (UpdateActivePaymentException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GeneratedPayroll.error.updateActivePayment");
        }
    }

    @Override
    public String create() {
        long generatedPayrollCount = generatedPayrollService.countGeneratedPayrollByGestionPayroll(gestionPayroll, GeneratedPayrollType.OFFICIAL);
        if (generatedPayrollCount > 0) {
            /*return can not be more than one official for the gestion*/
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.officialAlreadyExist");
            return Outcome.REDISPLAY;
        } else {
            String outcome = super.create();
            closeConversation(outcome);
            return outcome;
        }
    }


    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String select(GeneratedPayroll instance) {
        generatedPayrollTypeSelected = instance.getGeneratedPayrollType();
        try {
            setOp(OP_UPDATE);
            //Ensure the instance exists in the database, find it
            setInstance(getService().findById(getEntityClass(), getId(instance), true));
            return Outcome.SUCCESS;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    public GeneratedPayrollType getGeneratedPayrollTypeSelected() {
        return generatedPayrollTypeSelected;
    }

    public void setGeneratedPayrollTypeSelected(GeneratedPayrollType generatedPayrollTypeSelected) {
        this.generatedPayrollTypeSelected = generatedPayrollTypeSelected;
    }

    @Override
    @End(beforeRedirect = true)
    public String update() {
        try {
            generatedPayrollService.update(getInstance());
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (GeneratedPayrollHasNegativeAmountException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.negativeAmount");
            return Outcome.REDISPLAY;
        } catch (EmployeeMissingBankAccountException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.bankAccounts");
            return Outcome.REDISPLAY;
        } catch (AlreadyExistsAnOfficialGeneratedPayrollException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.officialAlreadyExistForGestionParameters",
                    getInstance().getGestionPayroll().getBusinessUnit().getFullName(),
                    getInstance().getGestionPayroll().getGestion().getYear(),
                    messages.get(getInstance().getGestionPayroll().getMonth().getResourceKey()),
                    getInstance().getGestionPayroll().getJobCategory().getFullName());
            return Outcome.REDISPLAY;
        } catch (CannotChangeToOutdatedGeneratedPayrollTypeException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.anyToOutdated");
            return Outcome.REDISPLAY;
        } catch (CannotChangeFromOutdatedGeneratedPayrollTypeException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.outdatedToany");
            return Outcome.REDISPLAY;
        } catch (CannotChangeFromOfficialToTestGeneratedPayrollTypeException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.officialToTest");
            return Outcome.REDISPLAY;
        } catch (QuotaInfoOutdatedException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.quotaInfoHasChanged");
            return Outcome.REDISPLAY;
        } catch (GestionPayrollOfficialPayrollDeadlineException e) {
            refreshInstance();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GestionPayroll.error.officialPayrollDeadline");
            return Outcome.REDISPLAY;
        }
        String outcome = Outcome.SUCCESS;
        closeConversation(outcome);
        addUpdatedMessage();
        return outcome; //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @End(beforeRedirect = true)
    public String delete() {
        if (GeneratedPayrollType.OFFICIAL.equals(getInstance().getGeneratedPayrollType())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.cannotDeleteOfficalPayroll");
            return Outcome.REDISPLAY;
        } else {
            try {
                GeneratedPayroll currentGeneratedPayroll = getService().findById(GeneratedPayroll.class, getInstance().getId());
                if (GeneratedPayrollType.OFFICIAL.equals(currentGeneratedPayroll.getGeneratedPayrollType())) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.cannotDeleteOfficalPayroll");
                    return Outcome.REDISPLAY;
                }
            } catch (EntryNotFoundException e) {
                addNotFoundMessage();
                return Outcome.REDISPLAY;
            }
        }
        return super.delete();
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public PayrollGenerationCycleAction getPayrollGenerationCycleAction() {
        return payrollGenerationCycleAction;
    }

    public Boolean isBankAccountPaymentType(Employee employee) {
        return employee.getPaymentType().equals(PaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public BankAccount findDefaultBankAccount(Employee employee) {
        return bankAccountService.getDefaultAccount(employee);
    }

    public Boolean isOfficialGeneratedPayrollType(GeneratedPayroll generatedPayroll) {
        return GeneratedPayrollType.OFFICIAL.equals(generatedPayroll.getGeneratedPayrollType());
    }

    public Boolean isGenerationBySalary(GeneratedPayroll generatedPayroll) {
        return PayrollGenerationType.GENERATION_BY_SALARY.equals(generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType());
    }

    public Boolean isProfessorGenerationBySalary(GeneratedPayroll generatedPayroll) {
        return PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType());
    }

    public Boolean isGenerationByTime(GeneratedPayroll generatedPayroll) {
        return PayrollGenerationType.GENERATION_BY_TIME.equals(generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType());
    }

    public Boolean isChristmasBonusType(GeneratedPayroll generatedPayroll) {
        return GestionPayrollType.CHRISTMAS_BONUS.equals(generatedPayroll.getGestionPayroll().getGestionPayrollType());
    }
}