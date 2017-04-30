package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.GestionPayrollSchedule;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.ExchangeRate;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import com.encens.khipus.service.employees.GestionPayrollScheduleService;
import com.encens.khipus.service.employees.GestionPayrollService;
import com.encens.khipus.util.EntityValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for GestionPayrollSchedule
 *
 * @author
 * @version 2.26
 */
@Name("gestionPayrollScheduleAction")
@Scope(ScopeType.CONVERSATION)
public class GestionPayrollScheduleAction extends GenericAction<GestionPayrollSchedule> {
    /*elements managed by the instance*/
    private List<BusinessUnit> businessUnitList;
    @In
    private BusinessUnitService businessUnitService;

    @In
    private GestionPayrollScheduleService gestionPayrollScheduleService;
    @In
    private GestionPayrollService gestionPayrollService;
    @In
    private GeneratedPayrollService generatedPayrollService;

    /* the jobCategory to apply the action selected*/
    private JobCategory jobCategory;

    /* to track the active tab in view*/
    private String activeTabName;
    private BusinessUnit activeBusinessUnit;

    /*list to hold the data to edit */
    private List<GestionPayroll> gestionPayrollToEditList;
    /*list to hold information of the readOnly state of the gestionPayrrols at load data time*/
    private List<GestionPayroll> gestionPayrollToEditReadOnlyList = new ArrayList<GestionPayroll>();
    /*list to hold information of the items of the gestionPayrollToEditList */
    public List<GestionPayroll> gestionPayrollToDeleteList = new ArrayList<GestionPayroll>();

    /*to support error messages*/
    public List<Boolean> validateRowList = new ArrayList<Boolean>();
    public List<String> gestionNameMessageList = new ArrayList<String>();
    public List<String> initDateMessageList = new ArrayList<String>();
    public List<String> endDateMessageList = new ArrayList<String>();
    public List<String> generationDeadlineMessageList = new ArrayList<String>();
    public List<String> rateMessageList = new ArrayList<String>();
    public List<String> generationBeginningMessageList = new ArrayList<String>();
    public List<String> officialPayrollDeadlineMessageList = new ArrayList<String>();
    private static final String GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED = "GestionPayrollSchedule.error.required";
    private static final String GESTION_PAYROLL_SCHEDULE_ERROR_LESS_DATE = "GestionPayrollSchedule.error.lessDate";

    @Create
    public void atCreateTime() {
        resetErrorList();
    }

    @Factory(value = "gestionPayrollSchedule", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('GESTIONPAYROLLSCHEDULE','VIEW')}")
    public GestionPayrollSchedule initGestionPayrollSchedule() {
        return getInstance();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('GESTIONPAYROLLSCHEDULE','CREATE')}")
    public String create() {
        /*set the instance to create the new entity*/
        try {
            gestionPayrollScheduleService.create(getInstance());
            setOp(OP_UPDATE);
            select(getInstance());
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('GESTIONPAYROLLSCHEDULE','VIEW')}")
    public String select(GestionPayrollSchedule instance) {

        String outcome = super.select(instance);
        if (outcome.equals(Outcome.SUCCESS) && isManaged()) {
            businessUnitList = businessUnitService.findAll(null);
            if (!businessUnitList.isEmpty()) {
                activeTabName = businessUnitList.get(0).getFullName();
                activeBusinessUnit = businessUnitList.get(0);
            }
        }
        return outcome;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('GESTIONPAYROLLSCHEDULE','UPDATE')}")
    public String update() {
        return super.update();
    }

    /*methods declaration*/

    /**
     * This method saves all the changes made to gestionPayrollToEditList
     */
    @Restrict("#{s:hasPermission('GESTIONPAYROLLSCHEDULEGESTIONITEM','CREATE') or s:hasPermission('GESTIONPAYROLLSCHEDULEGESTIONITEM','UPDATE') or s:hasPermission('GESTIONPAYROLLSCHEDULEGESTIONITEM','DELETE')}")
    public void saveAll() {
        if (!validate()) {
            return;
        }
        List<GestionPayroll> invalidGestionPayrollList = validateGestionPayrollToEditList();
        /* there are gestion payrolls that can't be updated*/
        if (invalidGestionPayrollList.size() > 0) {
            addCannotUpdatedMessage(invalidGestionPayrollList);
            return;
        }
        /*Validate if any user have generated payrolls by the way for gestion payrolls to delete*/
        if (thereAreGeneratedPayrollsForGestionPayrollsToDelete()) {
            return;
        }
        try {
            gestionPayrollScheduleService.saveAll(gestionPayrollToEditList, gestionPayrollToDeleteList, activeBusinessUnit,
                    jobCategory, getInstance().getGestion());
        } catch (ConcurrencyException e) {
            concurrencyLog();
            addUpdateConcurrencyMessage();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    /**
     * Method to indicate which items of gestionPayrollToEditList should be delete
     * in case the instance is persistent.
     * Cleans the data entered by the user in case the instance is not persistent.
     *
     * @param gestionPayroll the item to delete
     */
    public void manageItemToDelete(GestionPayroll gestionPayroll) {
        if (null != gestionPayroll.getId()) {
            if (gestionPayrollToDeleteList.contains(gestionPayroll)) {
                gestionPayrollToDeleteList.remove(gestionPayroll);
            } else {
                /* if the gestion payroll has generated payrolls it can't be deleted*/
                if (generatedPayrollService.hasGeneratedPayrolls(gestionPayroll)) {
                    addDeleteReferentialIntegrityMessage(gestionPayroll.getGestionName());
                } else {
                    gestionPayrollToDeleteList.add(gestionPayroll);
                }
            }
        } else {
            cleanGestionPayroll(gestionPayroll);
        }
    }

    /**
     * This method cleans data that was entered by the user
     *
     * @param gestionPayroll the object to clean
     */
    public void cleanGestionPayroll(GestionPayroll gestionPayroll) {
        gestionPayroll.setGestionName(null);
        gestionPayroll.setInitDate(null);
        gestionPayroll.setEndDate(null);
        gestionPayroll.setGenerationBeginning(null);
        gestionPayroll.setGenerationDeadline(null);
        gestionPayroll.setOfficialPayrollDeadline(null);
        gestionPayroll.getExchangeRate().setRate(null);
    }

    /**
     * Function to ask if the gestionPayroll item has been mark to delete operation
     *
     * @param gestionPayroll the item to test
     * @return true if the item is an element of the gestionPayrollToDeleteList
     */
    public boolean gestionPayrollToDeleteListContainsItem(GestionPayroll gestionPayroll) {
        return gestionPayrollToDeleteList.contains(gestionPayroll);
    }

    public void enableTab(String tabName) {
        setActiveTabName(tabName);
    }

    public List<GestionPayroll> gestionPayrollList(BusinessUnit businessUnit, Month month) {
        return gestionPayrollService.findGestionPayrollByGestionAndBusinessUnitAndMonth(getInstance().getGestion(), month, businessUnit, null);
    }

    /**
     * Validate if any user have generated payrolls by the way for gestion payrolls to delete
     *
     * @return true if any user have generated payrolls by the way for gestion payrolls to delete
     */
    private boolean thereAreGeneratedPayrollsForGestionPayrollsToDelete() {
        boolean thereAreGeneratedPayrolls = false;
        for (GestionPayroll gestionPayroll : gestionPayrollToDeleteList) {
            /* if the gestion payroll has generated payrolls it can't be deleted*/
            if (generatedPayrollService.hasGeneratedPayrolls(gestionPayroll)) {
                addDeleteReferentialIntegrityMessage(gestionPayroll.getGestionName());
                thereAreGeneratedPayrolls = true;
            }
        }
        return thereAreGeneratedPayrolls;
    }

    /**
     * Validates gestionPayrollToEditList
     *
     * @return List<GestionPayroll> wich contain the gestionPayrolls that can't be updated
     */
    private List<GestionPayroll> validateGestionPayrollToEditList() {
        /*List to hold gestionPayrolls that can't be updated*/
        List<GestionPayroll> invalidGestionPayrollList = new ArrayList<GestionPayroll>();

        for (GestionPayroll gestionPayroll : gestionPayrollToEditList) {
            /*in case already created*/
            /*if at load time it didn't had generated payrolls but it actually has*/
            if (null != gestionPayroll.getId() && !gestionPayrollToEditReadOnlyList.contains(gestionPayroll) && generatedPayrollService.hasGeneratedPayrolls(gestionPayroll)) {
                invalidGestionPayrollList.add(gestionPayroll);
            }
        }
        return invalidGestionPayrollList;
    }

    /**
     * Loads the data to edit given the instance gestion, an active businessUnit, and a jobCategory specified.
     */
    public void loadGestionPayrollToEditList() {
        gestionPayrollToEditReadOnlyList.clear();
        gestionPayrollToDeleteList.clear();
        if (jobCategory != null) {
            List<GestionPayroll> gestionPayrollList = new ArrayList<GestionPayroll>();
            for (Month month : Month.values()) {
                GestionPayroll gestionPayroll;
                gestionPayroll = gestionPayrollService.findGestionPayrollByGestionAndBusinessUnitAndMonthAndJobCategory(
                        getInstance().getGestion(), month, activeBusinessUnit, jobCategory, null);
                if (generatedPayrollService.hasGeneratedPayrolls(gestionPayroll)) {
                    gestionPayrollToEditReadOnlyList.add(gestionPayroll);
                }
                if (gestionPayroll == null) {
                    gestionPayroll = new GestionPayroll();
                    gestionPayroll.setExchangeRate(new ExchangeRate());
                }
                gestionPayrollList.add(gestionPayroll);
            }
            setGestionPayrollToEditList(gestionPayrollList);
        } else {
            addEmptyJobCategoryError();
        }
    }

    /**
     * @return true if there is no error message throughout the data hold by gestionPayrollToEditList
     */
    public boolean validate() {
        /*execute the validation process*/
        if (null != gestionPayrollToEditList) {
            validateEditData();
        } else {
            resetErrorList();
        }
        /*if the list does not contains any row to validate*/
        for (int i = 0; i < 12; i++) {
            if (validateRowList.get(i).equals(Boolean.TRUE)) {
                log.debug("validar fila " + i);
                return false;
            }
            log.debug("no validar fila" + i);
        }
        return true;
    }

    /**
     * Validates the data of the modal panel associated to manage the data
     */
    public void validateEditData() {
        log.debug("tamano de lista de datos" + gestionPayrollToEditList.size());
        log.debug("validar cada item de la lista**********************");
        for (int i = 0; i < gestionPayrollToEditList.size(); i++) {
            GestionPayroll gestionPayroll = gestionPayrollToEditList.get(i);
            /*in case validation is required*/
            log.debug("validando item " + i);
            validateItem(gestionPayroll, i);
            if (validateRowList.get(i).equals(Boolean.TRUE)) {
                log.debug("el item debe ser validado");
                /*required fields validation*/
                fitPropertyMessage(EntityValidatorUtil.isNotNull(gestionPayroll, "gestionName"), i,
                        gestionNameMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED);
                fitPropertyMessage(EntityValidatorUtil.isNotNull(gestionPayroll, "initDate"), i,
                        initDateMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED);
                fitPropertyMessage(EntityValidatorUtil.isNotNull(gestionPayroll, "endDate"), i,
                        endDateMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED);
                fitPropertyMessage(EntityValidatorUtil.isNotNull(gestionPayroll, "generationDeadline"), i,
                        generationDeadlineMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED);
                log.debug("rate=" + gestionPayroll.getExchangeRate().getRate());
                log.debug("validacion de rate isnotnull " + EntityValidatorUtil.isNotNull(gestionPayroll.getExchangeRate(), "rate"));
                fitPropertyMessage(null != gestionPayroll.getExchangeRate().getRate(), i,
                        rateMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED);
                fitPropertyMessage(EntityValidatorUtil.isNotNull(gestionPayroll, "generationBeginning"), i,
                        generationBeginningMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED);
                fitPropertyMessage(EntityValidatorUtil.isNotNull(gestionPayroll, "officialPayrollDeadline"), i,
                        officialPayrollDeadlineMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_REQUIRED);

                /**
                 * Validation of init and end dates between actual item and last item if any
                 * the init date of actual month must be greater than end date of last month if any
                 * this validation is only made in case required validation didn't succeed
                 * */
                if (i > 0 && null == initDateMessageList.get(i)) {
                    fitPropertyMessage((null != gestionPayroll.getInitDate() && null != gestionPayrollToEditList.get(i - 1).getEndDate() && gestionPayroll.getInitDate().compareTo(gestionPayrollToEditList.get(i - 1).getEndDate()) > 0), i,
                            initDateMessageList, GESTION_PAYROLL_SCHEDULE_ERROR_LESS_DATE);
                }
            } else {
                log.debug("el item no debe ser validado");
            }
        }
    }

    /**
     * This method determines whether the gestionPayroll item needs to be validated
     *
     * @param gestionPayroll the item to determine whether it needs to be validated
     * @param index          the position in the list which belongs to the gestionPayroll item in the data set
     */
    public void validateItem(GestionPayroll gestionPayroll, int index) {
        Boolean value;
        log.debug(gestionPayroll.toString());
        if (null != gestionPayroll.getId()) {
            log.debug("existe instancia");
            if (null != gestionPayroll.getGestionName()
                    && null != gestionPayroll.getInitDate()
                    && null != gestionPayroll.getEndDate()
                    && null != gestionPayroll.getGenerationDeadline()
                    && null != gestionPayroll.getExchangeRate().getRate()
                    ) {
                if (index > 0 && null != gestionPayrollToEditList.get(index - 1).getEndDate()
                        && gestionPayroll.getInitDate().compareTo(gestionPayrollToEditList.get(index - 1).getEndDate()) <= 0) {
                    value = Boolean.TRUE;
                } else {
                    value = Boolean.FALSE;
                }
                log.debug("a");
            } else {
                value = Boolean.TRUE;
                log.debug("b");
            }
        } else {
            log.debug("instancia no encontrada");
            if (null == gestionPayroll.getGestionName()
                    && null == gestionPayroll.getInitDate()
                    && null == gestionPayroll.getEndDate()
                    && null == gestionPayroll.getGenerationDeadline()
                    && null == gestionPayroll.getExchangeRate().getRate()
                    ) {
                value = Boolean.FALSE;
                log.debug("c");
            } else {
                /*if at least one filed is not null*/
                if (null != gestionPayroll.getGestionName()
                        && null != gestionPayroll.getInitDate()
                        && null != gestionPayroll.getEndDate()
                        && null != gestionPayroll.getGenerationDeadline()
                        && null != gestionPayroll.getExchangeRate().getRate()
                        ) {
                    if (index > 0 && null != gestionPayrollToEditList.get(index - 1).getEndDate()
                            && gestionPayroll.getInitDate().compareTo(gestionPayrollToEditList.get(index - 1).getEndDate()) <= 0) {
                        value = Boolean.TRUE;
                    } else {
                        value = Boolean.FALSE;
                    }
                } else {
                    value = Boolean.TRUE;
                }
                log.debug("d");
            }
        }
        validateRowList.remove(index);
        validateRowList.add(index, value);
    }

    /**
     * This method fit the message (the error message to show in view) in case the value send is false (something is wrong)
     * but if the value send is true (everything is ok) set a null message.
     *
     * @param value       the boolean value that indicates if everything is ok or not in field validation process
     * @param index       the position in the error message list in which the message should be added
     * @param messageList the list that holds and to which add the error messages if any
     * @param message     the error message to add to the error message list in case it is necessary
     */

    public void fitPropertyMessage(Boolean value, int index, List<String> messageList, String message) {
        messageList.remove(index);
        if (value) {
            messageList.add(index, null);
        } else {
            messageList.add(index, message);
        }
    }


    /**
     * @param i The number that indicates the number of the month beginning from zero
     * @return Month the enumeration value of the corresponding month
     */
    public Month decodeMonth(int i) {
        return Month.getMonth(i + 1);
    }

    private void resetErrorList() {
        validateRowList.clear();
        gestionNameMessageList.clear();
        initDateMessageList.clear();
        endDateMessageList.clear();
        generationDeadlineMessageList.clear();
        rateMessageList.clear();
        generationBeginningMessageList.clear();
        officialPayrollDeadlineMessageList.clear();

        /*charge default values*/
        for (int i = 0; i < 12; i++) {
            validateRowList.add(Boolean.FALSE);
            gestionNameMessageList.add(null);
            initDateMessageList.add(null);
            endDateMessageList.add(null);
            generationDeadlineMessageList.add(null);
            rateMessageList.add(null);
            generationBeginningMessageList.add(null);
            officialPayrollDeadlineMessageList.add(null);
        }
    }


    public boolean gestionPayrollReadOnly(GestionPayroll gestionPayroll) {
        return generatedPayrollService.hasGeneratedPayrolls(gestionPayroll);
    }
/* getters and setters*/

    public List<GestionPayroll> getGestionPayrollToEditList() {
        return gestionPayrollToEditList;
    }

    public void setGestionPayrollToEditList(List<GestionPayroll> gestionPayrollToEditList) {
        this.gestionPayrollToEditList = gestionPayrollToEditList;
    }

    public List<BusinessUnit> getBusinessUnitList() {
        return businessUnitList;
    }

    public void setBusinessUnitList(List<BusinessUnit> businessUnitList) {
        this.businessUnitList = businessUnitList;
    }

    public String getActiveTabName() {
        return activeTabName;
    }

    public void setActiveTabName(String activeTabName) {
        this.activeTabName = activeTabName;
        for (BusinessUnit businessUnit : businessUnitList) {
            if (businessUnit.getFullName().equals(activeTabName)) {
                activeBusinessUnit = businessUnit;
            }
        }
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public List<Boolean> getValidateRowList() {
        return validateRowList;
    }

    public void setValidateRowList(List<Boolean> validateRowList) {
        this.validateRowList = validateRowList;
    }

    public List<String> getGestionNameMessageList() {
        return gestionNameMessageList;
    }

    public void setGestionNameMessageList(List<String> gestionNameMessageList) {
        this.gestionNameMessageList = gestionNameMessageList;
    }

    public List<String> getInitDateMessageList() {
        return initDateMessageList;
    }

    public void setInitDateMessageList(List<String> initDateMessageList) {
        this.initDateMessageList = initDateMessageList;
    }

    public List<String> getEndDateMessageList() {
        return endDateMessageList;
    }

    public void setEndDateMessageList(List<String> endDateMessageList) {
        this.endDateMessageList = endDateMessageList;
    }

    public List<String> getGenerationDeadlineMessageList() {
        return generationDeadlineMessageList;
    }

    public void setGenerationDeadlineMessageList(List<String> generationDeadlineMessageList) {
        this.generationDeadlineMessageList = generationDeadlineMessageList;
    }

    public List<String> getRateMessageList() {
        return rateMessageList;
    }

    public void setRateMessageList(List<String> rateMessageList) {
        this.rateMessageList = rateMessageList;
    }

    public List<String> getGenerationBeginningMessageList() {
        return generationBeginningMessageList;
    }

    public void setGenerationBeginningMessageList(List<String> generationBeginningMessageList) {
        this.generationBeginningMessageList = generationBeginningMessageList;
    }

    public List<String> getOfficialPayrollDeadlineMessageList() {
        return officialPayrollDeadlineMessageList;
    }

    public void setOfficialPayrollDeadlineMessageList(List<String> officialPayrollDeadlineMessageList) {
        this.officialPayrollDeadlineMessageList = officialPayrollDeadlineMessageList;
    }

    public List<GestionPayroll> getGestionPayrollToDeleteList() {
        return gestionPayrollToDeleteList;
    }

    public void setGestionPayrollToDeleteList(List<GestionPayroll> gestionPayrollToDeleteList) {
        this.gestionPayrollToDeleteList = gestionPayrollToDeleteList;
    }
    /*messages*/

    protected void addEmptyJobCategoryError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "GestionPayrollSchedule.error.emptyJobCategory");
    }

    protected void addCannotUpdatedMessage(List<GestionPayroll> gestionPayrollList) {
        for (GestionPayroll gestionPayroll : gestionPayrollList) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "GestionPayrollSchedule.error.cannotUpdate", gestionPayroll.getGestionName());
        }
    }

    protected void addDeleteReferentialIntegrityMessage(String name) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.message.referentialIntegrity.delete", name);
    }
}
