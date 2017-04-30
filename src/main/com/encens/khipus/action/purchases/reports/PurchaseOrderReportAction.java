package com.encens.khipus.action.purchases.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.purchases.PayConditions;
import com.encens.khipus.model.purchases.PurchaseOrderDocumentRegisterState;
import com.encens.khipus.model.purchases.PurchaseOrderState;
import com.encens.khipus.model.purchases.PurchaseOrderType;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * Action to generate pending purchase order report
 *
 * @author
 * @version $Id: PurchaseOrderReportAction.java  06-oct-2010 18:13:43$
 */
@Name("purchaseOrderReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTPURCHASEORDER','VIEW')}")
public class PurchaseOrderReportAction extends GenericReportAction {

    private PurchaseOrderType purchaseOrderType;
    private String orderNumber;
    private String invoiceNumber;
    private Employee responsible;
    private Date startDate;
    private Date endDate;
    private String gloss;
    private PurchaseOrderState state;
    private List<PurchaseOrderState> inactiveStates;
    private BusinessUnit executorUnit;
    private CostCenter costCenter;
    private Provider provider;
    private Warehouse warehouse;
    private CollectionDocumentType documentType;
    private PurchaseOrderDocumentRegisterState registerState;
    private PayConditions payConditions;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private ModuleProviderType moduleProviderType;
    private Month consumeMonth;
    private Integer receptionYear;
    private Date initReceptionYearDate;
    private Date endReceptionYearDate;

    public void generateReport() {
        log.debug("Generate PurchaseOrderReportAction........");
        log.debug("año: " + initReceptionYearDate);
        log.debug("año: " + endReceptionYearDate);

        Map params = new HashMap();

        super.generateReport("purchaseOrderReport", "/purchases/reports/purchaseOrderReport.jrxml", MessageUtils.getMessage("Reports.purchaseOrder.title"), params);
    }

    @Override
    protected String getEjbql() {

        String query = "SELECT " +
                "purchaseOrder.date," +
                "purchaseOrder.orderType," +
                "purchaseOrder.orderNumber," +
                "purchaseOrder.invoiceNumber," +
                "executorUnit.executorUnitCode," +
                "organization.name," +
                "costCenter.code," +
                "costCenter.description," +
                "provider.providerCode," +
                "entity.acronym," +
                "entity.mainAddress," +
                "entity.phoneNumber," +
                "petitioner.lastName," +
                "petitioner.maidenName," +
                "petitioner.firstName," +
                "purchaseOrder.consumeMonth," +
                "warehouse.warehouseCode," +
                "warehouse.name," +
                "responsible.lastName," +
                "responsible.maidenName," +
                "responsible.firstName," +
                "purchaseOrder.state," +
                "purchaseOrder.documentType," +
                "payConditions.code," +
                "payConditions.name," +
                "purchaseOrder.totalAmount," +
                "purchaseOrder " +
                " FROM PurchaseOrder purchaseOrder" +
                " LEFT JOIN purchaseOrder.executorUnit executorUnit" +
                " LEFT JOIN executorUnit.organization organization" +
                " LEFT JOIN purchaseOrder.costCenter costCenter" +
                " LEFT JOIN purchaseOrder.provider provider" +
                " LEFT JOIN provider.entity entity" +
                //petitioner
                " LEFT JOIN purchaseOrder.petitionerJobContract petitionerJobContract" +
                " LEFT JOIN petitionerJobContract.contract petitionerContract" +
                " LEFT JOIN petitionerContract.employee petitioner" +
                " LEFT JOIN purchaseOrder.warehouse warehouse" +
                " LEFT JOIN purchaseOrder.responsible responsible" +
                " LEFT JOIN purchaseOrder.payConditions payConditions";
        if (getRegisterState() != null) {
            setInactiveStates(PurchaseOrderState.getInactiveStates());
            if (PurchaseOrderDocumentRegisterState.PENDING.equals(getRegisterState())) {
                query += " WHERE purchaseOrder.invoiceNumber IS NULL";
            } else if (PurchaseOrderDocumentRegisterState.COMPLETED.equals(getRegisterState())) {
                query += " WHERE purchaseOrder.invoiceNumber IS NOT NULL";
            }
        }

        return (query);
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "purchaseOrder.orderType = #{purchaseOrderReportAction.purchaseOrderType}",
                "purchaseOrder.documentType = #{purchaseOrderReportAction.documentType}",
                "lower(purchaseOrder.gloss) like concat('%', concat(lower(#{purchaseOrderReportAction.gloss}), '%'))",
                "purchaseOrder.state = #{purchaseOrderReportAction.state}",
                "lower(purchaseOrder.orderNumber) like concat(lower(#{purchaseOrderReportAction.orderNumber}), '%')",
                "lower(purchaseOrder.invoiceNumber) like concat('%', concat(lower(#{purchaseOrderReportAction.invoiceNumber}), '%'))",
                "responsible = #{purchaseOrderReportAction.responsible}",
                "costCenter = #{purchaseOrderReportAction.costCenter}",
                "provider = #{purchaseOrderReportAction.provider}",
                "warehouse = #{purchaseOrderReportAction.warehouse}",
                "purchaseOrder.date >= #{purchaseOrderReportAction.startDate}",
                "purchaseOrder.date <= #{purchaseOrderReportAction.endDate}",
                "purchaseOrder.receptionDate >= #{purchaseOrderReportAction.initReceptionYearDate}",
                "purchaseOrder.receptionDate <= #{purchaseOrderReportAction.endReceptionYearDate}",
                "executorUnit = #{purchaseOrderReportAction.executorUnit}",
                "purchaseOrder.state not in (#{purchaseOrderReportAction.inactiveStates})",
                "payConditions = #{purchaseOrderReportAction.payConditions}",
                "purchaseOrder.totalAmount >= #{purchaseOrderReportAction.startAmount}",
                "purchaseOrder.totalAmount <= #{purchaseOrderReportAction.endAmount}",
                "purchaseOrder.consumeMonth = #{purchaseOrderReportAction.consumeMonth}"
        };

        sortProperty = "purchaseOrder.date";
    }

    public void cleanResponsible() {
        setResponsible(null);
    }

    public void cleanCostCenter() {
        setCostCenter(null);
    }

    public void cleanProvider() {
        setProvider(null);
    }

    public PurchaseOrderType getPurchaseOrderType() {
        return purchaseOrderType;
    }

    public void setPurchaseOrderType(PurchaseOrderType purchaseOrderType) {
        this.purchaseOrderType = purchaseOrderType;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public PurchaseOrderState getState() {
        return state;
    }

    public void setState(PurchaseOrderState state) {
        this.state = state;
    }

    public List<PurchaseOrderState> getInactiveStates() {
        return inactiveStates;
    }

    public void setInactiveStates(List<PurchaseOrderState> inactiveStates) {
        this.inactiveStates = inactiveStates;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public CollectionDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(CollectionDocumentType documentType) {
        this.documentType = documentType;
    }

    public PurchaseOrderDocumentRegisterState getRegisterState() {
        return registerState;
    }

    public void setRegisterState(PurchaseOrderDocumentRegisterState registerState) {
        this.registerState = registerState;
    }

    public PayConditions getPayConditions() {
        return payConditions;
    }

    public void setPayConditions(PayConditions payConditions) {
        this.payConditions = payConditions;
    }

    public BigDecimal getStartAmount() {
        return startAmount;
    }

    public void setStartAmount(BigDecimal startAmount) {
        this.startAmount = startAmount;
    }

    public BigDecimal getEndAmount() {
        return endAmount;
    }

    public void setEndAmount(BigDecimal endAmount) {
        this.endAmount = endAmount;
    }

    public ModuleProviderType getModuleProviderType() {
        return moduleProviderType;
    }

    public void setModuleProviderType(ModuleProviderType moduleProviderType) {
        this.moduleProviderType = moduleProviderType;
    }

    public void setupModuleProviderType() {
        cleanProvider();
        if (purchaseOrderType != null) {
            switch (purchaseOrderType) {
                case WAREHOUSE:
                    setModuleProviderType(ModuleProviderType.WAREHOUSE);
                    break;
                case FIXEDASSET:
                    setModuleProviderType(ModuleProviderType.FIXEDASSET);
                    break;
            }
        } else {
            setModuleProviderType(null);
        }

    }

    public Month getConsumeMonth() {
        return consumeMonth;
    }

    public void setConsumeMonth(Month consumeMonth) {
        this.consumeMonth = consumeMonth;
    }

    public Integer getReceptionYear() {
        return receptionYear;
    }

    public void setReceptionYear(Integer receptionYear) {
        if (null != receptionYear) {

            this.receptionYear = receptionYear;
            Calendar initYearDate = Calendar.getInstance();
            initYearDate.set(receptionYear, Calendar.JANUARY, 1);
            initReceptionYearDate = new Date();
            initReceptionYearDate.setTime(initYearDate.getTime().getTime());
            Calendar endYearDate = Calendar.getInstance();
            endYearDate.set(receptionYear, Calendar.DECEMBER, 1);
            endYearDate.set(Calendar.DAY_OF_MONTH, endYearDate.getMaximum(Calendar.DAY_OF_MONTH));
            endReceptionYearDate = new Date();
            initReceptionYearDate.setTime(initYearDate.getTime().getTime());
            endReceptionYearDate.setTime(endYearDate.getTime().getTime());
        } else {
            initReceptionYearDate = null;
            endReceptionYearDate = null;
        }
    }

    public Date getInitReceptionYearDate() {
        return initReceptionYearDate;
    }

    public void setInitReceptionYearDate(Date initReceptionYearDate) {
        this.initReceptionYearDate = initReceptionYearDate;
    }

    public Date getEndReceptionYearDate() {
        return endReceptionYearDate;
    }

    public void setEndReceptionYearDate(Date endReceptionYearDate) {
        this.endReceptionYearDate = endReceptionYearDate;
    }
}
