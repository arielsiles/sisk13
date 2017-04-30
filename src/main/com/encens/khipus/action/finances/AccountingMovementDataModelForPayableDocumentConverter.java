package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayrollType;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseOrderType;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.finances.PayableDocumentSourceType;
import com.encens.khipus.util.query.EntityQueryConditionOperator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import java.util.*;

/**
 * @author
 * @version 3.2.9
 */
@Name("accountingMovementDataModelForPayableDocumentConverter")
@Scope(ScopeType.CONVERSATION)
public class AccountingMovementDataModelForPayableDocumentConverter extends QueryDataModel<AccountingMovementPk, AccountingMovement> {
    private static final String[] RESTRICTIONS = {
            "accountingMovement.createDate >= #{accountingMovementDataModelForPayableDocumentConverter.createStartDate}",
            "accountingMovement.createDate <= #{accountingMovementDataModelForPayableDocumentConverter.createEndDate}",
            "accountingMovement.recordDate >= #{accountingMovementDataModelForPayableDocumentConverter.movementStartDate}",
            "accountingMovement.recordDate <= #{accountingMovementDataModelForPayableDocumentConverter.movementEndDate}",
            "detail.executorUnitCode=#{accountingMovementDataModelForPayableDocumentConverter.businessUnit.executorUnitCode}",
            "detail.costCenter=#{accountingMovementDataModelForPayableDocumentConverter.costCenter}",
            "detail.account=#{accountingMovementDataModelForPayableDocumentConverter.cashAccount}"
    };

    private Provider provider;
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private CashAccount cashAccount;
    private Date createStartDate;
    private Date createEndDate;
    private Date movementStartDate;
    private Date movementEndDate;
    private String gloss;
    //HHRR filsters
    private PayableDocumentSourceType converterType;
    private JobCategory jobCategory;
    private Gestion gestion;
    private GestionPayrollType gestionPayrollType;
    private Month month;
    private Date generatedDate;
    private String[] glossCompoundConditions;
    //PURCHASE filters
    private Month consumeMonth;
    private PurchaseOrderType purchaseOrderType;
    private String purchaseOrderNumber;

    private Boolean active = false;

    private Map<AccountingMovement, Boolean> selectedForPayableAccount = new HashMap<AccountingMovement, Boolean>();
    private Map<AccountingMovement, Boolean> selectedForPayments = new HashMap<AccountingMovement, Boolean>();

    @Logger
    protected Log log;

    @Create
    public void init() {
        sortProperty = "accountingMovement.gloss, accountingMovement.voucherType, accountingMovement.voucherNumber";
    }

    @Override
    public String getEjbql() {
        return "select distinct(accountingMovement) from AccountingMovementDetail detail " +
                " left join detail.accountingMovement accountingMovement " +
                " left join detail.voucher voucher ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    protected void postInitEntityQuery(EntityQuery entityQuery) {
        entityQuery.setEjbql(addConditions(getEjbql()));
    }

    private String addConditions(String ejbql) {
        String restrictionResult = null;

        if (!ValidatorUtil.isEmptyOrNull(getGlossCompoundConditions())) {
            restrictionResult = "";
            int length = getGlossCompoundConditions().length;
            boolean hasConditions = false;
            for (int i = 0; i < length; i++) {
                if (!ValidatorUtil.isBlankOrNull(getGlossCompoundConditions()[i])) {
                    hasConditions = true;
                    if (!ValidatorUtil.isBlankOrNull(restrictionResult)) {
                        restrictionResult += EntityQueryConditionOperator.OR;
                    }
                    String likeCondition = " like lower('" + getGlossCompoundConditions()[i] + "') ";
                    restrictionResult += " lower(accountingMovement.gloss)" + likeCondition;
                    restrictionResult += EntityQueryConditionOperator.OR;
                    restrictionResult += " lower(voucher.gloss)" + likeCondition;
                    restrictionResult += EntityQueryConditionOperator.OR;
                    restrictionResult += " lower(voucher.description)" + likeCondition;
                }
            }
            if (hasConditions) {
                restrictionResult = "(" + restrictionResult + ")";
            }
        }

        if (!ValidatorUtil.isBlankOrNull(restrictionResult)) {
            ejbql += " where ";
            ejbql += restrictionResult;
        }

        return ejbql;
    }


    @Override
    public void clear() {
        super.clear();
        setGlossCompoundConditions(null);
    }

    @Override
    public void search() {
        getSelectedForPayableAccount().clear();
        getSelectedForPayments().clear();
        if (getConverterType() != null) {
            setActive(true);
            setGlossCompoundConditions(null);
            if (PayableDocumentSourceType.HHRR.equals(getConverterType())) {
                String payrollTypeString = null;

                if (getGestionPayrollType() != null) {
                    payrollTypeString = MessageUtils.getMessage(getGestionPayrollType().getSingularResourceKey());
                }

                String jobCategoryName = null;
                if (getJobCategory() != null) {
                    jobCategoryName = FormatUtils.toAcronym(getJobCategory().getName(), getJobCategory().getAcronym());
                }

                String monthString = null;
                if (getMonth() != null) {
                    monthString = MessageUtils.getMessage(getMonth().getResourceKey());
                }

                String yearString = null;
                if (getGestion() != null) {
                    yearString = String.valueOf(getGestion().getYear()).replace(".", "");
                }

                String dateString = null;
                if (getGeneratedDate() != null) {
                    dateString = DateUtils.format(getGeneratedDate(), MessageUtils.getMessage("patterns.date"));
                }

                setGlossCompoundConditions(getCompoundValue(getGloss(), payrollTypeString, jobCategoryName, monthString, yearString, dateString));

            } else if (PayableDocumentSourceType.PURCHASE_ORDER.equals(getConverterType())) {

                String executorUnitName = null;
                if (getBusinessUnit() != null) {
                    executorUnitName = getBusinessUnit().getOrganization().getName();
                }

                String costCenterName = null;
                if (getCostCenter() != null) {
                    costCenterName = getCostCenter().getDescription();
                }

                String monthName = null;
                if (getConsumeMonth() != null) {
                    monthName = MessageUtils.getMessage(getConsumeMonth().getResourceKey());
                }

                String providerName = null;
                if (getProvider() != null) {
                    providerName = getProvider().getEntity().getAcronym();
                }

                String module = null;
                if (getPurchaseOrderType() != null) {
                    module = MessageUtils.getMessage(getPurchaseOrderType().getModule());
                }

                setGlossCompoundConditions(getCompoundValue(getGloss(), executorUnitName, costCenterName, providerName, monthName, getPurchaseOrderNumber(), module));

            }
        } else {
            setActive(false);
        }
        super.update();
        super.search();
    }

    public Date getCreateStartDate() {
        return createStartDate;
    }

    public void setCreateStartDate(Date createStartDate) {
        this.createStartDate = createStartDate;
    }

    public Date getCreateEndDate() {
        return createEndDate;
    }

    public void setCreateEndDate(Date createEndDate) {
        this.createEndDate = createEndDate;
    }

    public Date getMovementStartDate() {
        return movementStartDate;
    }

    public void setMovementStartDate(Date movementStartDate) {
        this.movementStartDate = movementStartDate;
    }

    public Date getMovementEndDate() {
        return movementEndDate;
    }

    public void setMovementEndDate(Date movementEndDate) {
        this.movementEndDate = movementEndDate;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public PayableDocumentSourceType getConverterType() {
        return converterType;
    }

    public void setConverterType(PayableDocumentSourceType converterType) {
        this.converterType = converterType;
    }

    public Boolean getIsHHRRConverterType() {
        return PayableDocumentSourceType.HHRR.equals(getConverterType());
    }

    public Boolean getIsPurchaseOrderConverterType() {
        return PayableDocumentSourceType.PURCHASE_ORDER.equals(getConverterType());
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public GestionPayrollType getGestionPayrollType() {
        return gestionPayrollType;
    }

    public void setGestionPayrollType(GestionPayrollType gestionPayrollType) {
        this.gestionPayrollType = gestionPayrollType;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public Boolean getGestionPayrollSalaryType() {
        return GestionPayrollType.SALARY.equals(getGestionPayrollType());
    }

    public Boolean getGestionPayrollChristmasBonusType() {
        return GestionPayrollType.CHRISTMAS_BONUS.equals(getGestionPayrollType());
    }

    public String[] getGlossCompoundConditions() {
        return glossCompoundConditions;
    }

    public void setGlossCompoundConditions(String[] glossCompoundConditions) {
        this.glossCompoundConditions = glossCompoundConditions;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Map<AccountingMovement, Boolean> getSelectedForPayableAccount() {
        return selectedForPayableAccount;
    }

    public void setSelectedForPayableAccount(Map<AccountingMovement, Boolean> selectedForPayableAccount) {
        this.selectedForPayableAccount = selectedForPayableAccount;
    }

    public Map<AccountingMovement, Boolean> getSelectedForPayments() {
        return selectedForPayments;
    }

    public void setSelectedForPayments(Map<AccountingMovement, Boolean> selectedForPayments) {
        this.selectedForPayments = selectedForPayments;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Month getConsumeMonth() {
        return consumeMonth;
    }

    public void setConsumeMonth(Month consumeMonth) {
        this.consumeMonth = consumeMonth;
    }

    public PurchaseOrderType getPurchaseOrderType() {
        return purchaseOrderType;
    }

    public void setPurchaseOrderType(PurchaseOrderType purchaseOrderType) {
        this.purchaseOrderType = purchaseOrderType;
    }

    public void assignCostCenter(CostCenter costCenterAssign) {
        setCostCenter(costCenterAssign);
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public void assignCashAccount(CashAccount assignedCashAccount) {
        setCashAccount(assignedCashAccount);
    }

    public void clearCashAccount() {
        setCashAccount(null);
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    private String[] getCompoundValue(String... valueConditions) {
        String compoundSimpleValue = "";
        String compoundReverseValue = "";
        if (!ValidatorUtil.isEmptyOrNull(valueConditions)) {
            for (String valueCondition : valueConditions) {
                if (!ValidatorUtil.isBlankOrNull(valueCondition)) {
                    valueCondition = valueCondition.replaceAll("'", "%");
                    compoundSimpleValue += "%" + valueCondition;
                    compoundReverseValue = valueCondition + "%" + compoundReverseValue;
                }
            }
            if (!ValidatorUtil.isBlankOrNull(compoundSimpleValue)) {
                compoundSimpleValue += "%";
                compoundReverseValue = "%" + compoundReverseValue;
            }
        }
        return new String[]{compoundSimpleValue, compoundReverseValue};
    }

}
