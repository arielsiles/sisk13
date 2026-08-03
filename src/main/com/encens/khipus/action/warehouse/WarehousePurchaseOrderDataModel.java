package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.purchases.PayConditions;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("warehousePurchaseOrderDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDER','VIEW')}")
public class WarehousePurchaseOrderDataModel extends QueryDataModel<Long, PurchaseOrder> {
    private static final String[] RESTRICTIONS = {
            "lower(warehousePurchaseOrder.gloss) like concat('%', concat(lower(#{warehousePurchaseOrderDataModel.criteria.gloss}), '%'))",
            "warehousePurchaseOrder.state = #{warehousePurchaseOrderDataModel.criteria.state}",
            "lower(warehousePurchaseOrder.orderNumber) like concat(lower(#{warehousePurchaseOrderDataModel.criteria.orderNumber}), '%')",
            "lower(warehousePurchaseOrder.invoiceNumber) like concat('%', concat(lower(#{warehousePurchaseOrderDataModel.criteria.invoiceNumber}), '%'))",
            "warehousePurchaseOrder.responsible = #{warehousePurchaseOrderDataModel.responsible}",
            "warehousePurchaseOrder.costCenter = #{warehousePurchaseOrderDataModel.costCenter}",
            "warehousePurchaseOrder.provider = #{warehousePurchaseOrderDataModel.provider}",
            "warehousePurchaseOrder.warehouse = #{warehousePurchaseOrderDataModel.warehouse}",
            "warehousePurchaseOrder.payConditions = #{warehousePurchaseOrderDataModel.payConditions}",
            "warehousePurchaseOrder.date >= #{warehousePurchaseOrderDataModel.startDate}",
            "warehousePurchaseOrder.date <= #{warehousePurchaseOrderDataModel.endDate}",
            "warehousePurchaseOrder.executorUnit = #{warehousePurchaseOrderDataModel.criteria.executorUnit}",
            "warehousePurchaseOrder.orderType = #{enumerationUtil.getEnumValue('com.encens.khipus.model.purchases.PurchaseOrderType', 'WAREHOUSE')}",
            "warehousePurchaseOrder.documentType = #{warehousePurchaseOrderDataModel.documentType}"
    };

    private Date startDate;
    private Date endDate;
    private Employee responsible;
    private CostCenter costCenter;
    private Provider provider;
    /**
     * Texto a mostrar en el filtro de proveedor. Se resuelve al asignar el proveedor
     * -con el entity manager abierto- y no en el render: Provider.entity es LAZY y este
     * dataModel es de PAGE, por lo que en cualquier request posterior la entidad ya esta
     * desacoplada y navegar la asociacion lanza LazyInitializationException.
     */
    private String providerAcronym;
    private Warehouse warehouse;
    private CollectionDocumentType documentType;
    private PayConditions payConditions;

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "warehousePurchaseOrder.date, warehousePurchaseOrder.orderNumber";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select warehousePurchaseOrder" +
                " from PurchaseOrder warehousePurchaseOrder" +
                " left join fetch warehousePurchaseOrder.responsible responsible" +
                " left join fetch warehousePurchaseOrder.costCenter costCenter" +
                " left join fetch warehousePurchaseOrder.provider provider" +
                /* La columna Proveedor de la grilla muestra provider.fullName, que navega
                   provider.entity (LAZY). Sin este fetch las filas quedan con un proxy sin
                   inicializar y, al reutilizarse desde la cache del QueryDataModel en un
                   request posterior, revientan con LazyInitializationException. */
                " left join fetch provider.entity providerEntity" +
                " left join fetch warehousePurchaseOrder.warehouse";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
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

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void cleanCostCenter() {
        setCostCenter(null);
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public void cleanResponsible() {
        setResponsible(null);
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        this.providerAcronym = resolveProviderAcronym(provider);
    }

    public String getProviderAcronym() {
        return providerAcronym;
    }

    private String resolveProviderAcronym(Provider provider) {
        if (provider == null) {
            return null;
        }
        FinancesEntity financesEntity = provider.getEntity();
        return financesEntity != null ? financesEntity.getAcronym() : provider.getProviderCode();
    }

    public void cleanProvider() {
        setProvider(null);
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

    public PayConditions getPayConditions() {
        return payConditions;
    }

    public void setPayConditions(PayConditions payConditions) {
        this.payConditions = payConditions;
    }

    @Override
    public void clear() {
        setStartDate(null);
        setEndDate(null);
        setResponsible(null);
        setCostCenter(null);
        setProvider(null);
        setWarehouse(null);
        setDocumentType(null);
        setPayConditions(null);
        super.clear();
        update();
        search();
    }
}
