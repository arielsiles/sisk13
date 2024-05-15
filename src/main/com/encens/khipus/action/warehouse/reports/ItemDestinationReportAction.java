package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.customers.ArticleOrderService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.CollectMaterialService;
import com.encens.khipus.service.production.ProductionOrderService;
import com.encens.khipus.service.warehouse.InitialInventoryService;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.ProductInventoryService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("itemDestinationReportAction")
@Scope(ScopeType.PAGE)
public class ItemDestinationReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private String documentCode = "EGR";

    private ProductItem productItem;
    private Warehouse warehouse;
    private JobContract petitionerJobContract;
    private Boolean articlesWithMovement = true;

    @In
    private MovementDetailService movementDetailService;
    @In
    private ArticleOrderService articleOrderService;
    @In
    private ProductItemService productItemService;
    @In
    private ProductInventoryService productInventoryService;
    @In
    private ProductionOrderService productionOrderService;
    @In
    private CollectMaterialService collectMaterialService;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @In(create = true)
    KardexProductMovementAction kardexProductMovementAction;

    @In
    private InitialInventoryService initialInventoryService;

    @Create
    public void init() {
        restrictions = new String[]{
                "warehouseVoucher.date >= #{itemDestinationReportAction.startDate}",
                "warehouseVoucher.date <= #{itemDestinationReportAction.endDate}",
                "warehouseVoucher.warehouseCode = #{itemDestinationReportAction.warehouse.warehouseCode}",
                "warehouseVoucher.documentCode = #{itemDestinationReportAction.documentCode}",
                "warehouseVoucher.petitionerJobContract = #{itemDestinationReportAction.petitionerJobContract}"
        };
    }


    protected String getEjbql() {
        return "SELECT " +
                "   warehouseVoucher.date as date, " +
                "   warehouseVoucher.number as number, " +
                "   movementDetail.productItemCode as code, " +
                "   productItem.name as productName, " +
                "   movementDetail.measureCode as med, " +
                "   movementDetail.quantity as quantity, " +
                "   movementDetail.unitCost as unitCost, " +
                "   movementDetail.amount as amount, " +
                "   destination.code as destCode, " +
                "   destination.name as destName " +
                "   from MovementDetail movementDetail " +
                "   LEFT JOIN movementDetail.inventoryMovement.warehouseVoucher warehouseVoucher " +
                "   LEFT JOIN movementDetail.inventoryMovement.warehouseVoucher.destination destination" +
                "   LEFT JOIN movementDetail.productItem productItem ";
    }


    public void generateReport() {

        log.debug("generating Item Destination Report................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");
        String petitioner = "";
        if (petitionerJobContract != null) {
            petitioner = "Solicitante: " + petitionerJobContract.getContract().getEmployee().getFullName();
        }

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "DESTINO DE ARTICULOS DE ALMACEN");
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());

        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("warehouse", warehouse.getFullName());
        paramMap.put("period", period);
        paramMap.put("petitioner", petitioner);

        super.generateReport(
                "itemDestinationReport",
                "/warehouse/reports/itemDestinationReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("Reports.kardex.movement.itemDestination.titleReport"),
                paramMap);

    }


    public void cleanProductItem() {
        this.productItem = null;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
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

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void cleanWarehouseField() {
        warehouse = null;
    }

    public Boolean getArticlesWithMovement() {
        return articlesWithMovement;
    }

    public void setArticlesWithMovement(Boolean articlesWithMovement) {
        this.articlesWithMovement = articlesWithMovement;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public JobContract getPetitionerJobContract() {
        return petitionerJobContract;
    }

    public void setPetitionerJobContract(JobContract petitionerJobContract) {
        this.petitionerJobContract = petitionerJobContract;
    }

    public void clearPetitionerJobContract() {
        setPetitionerJobContract(null);
    }

    /**
     *
     */
    public class InitialArticle{

        private String code;
        private BigDecimal quantity;

        public InitialArticle(String code, BigDecimal quantity){
            this.code = code;
            this.quantity = quantity;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    /**
     * CollectionData class for Product Inventory Report
     */
    public class CollectionData{

        private String code;
        private String productName;
        private String unit;
        private BigDecimal initialAmount;
        private BigDecimal entryAmount;
        private BigDecimal outputAmount;
        private BigDecimal balance;
        private BigDecimal unitCost;
        private BigDecimal valuedBalance;

        public CollectionData(String code, String productName, String unit, BigDecimal initialAmount,  BigDecimal entryAmount, BigDecimal outputAmount, BigDecimal balance, BigDecimal unitCost){

            this.setCode(code);
            this.setProductName(productName);
            this.unit = unit;
            this.setInitialAmount(initialAmount);
            this.setEntryAmount(entryAmount);
            this.setOutputAmount(outputAmount);
            this.setBalance(balance);
            this.unitCost = unitCost;
            this.valuedBalance = BigDecimal.ZERO;
        }


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getInitialAmount() {
            return initialAmount;
        }

        public void setInitialAmount(BigDecimal initialAmount) {
            this.initialAmount = initialAmount;
        }

        public BigDecimal getEntryAmount() {
            return entryAmount;
        }

        public void setEntryAmount(BigDecimal entryAmount) {
            this.entryAmount = entryAmount;
        }

        public BigDecimal getOutputAmount() {
            return outputAmount;
        }

        public void setOutputAmount(BigDecimal outputAmount) {
            this.outputAmount = outputAmount;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public BigDecimal getValuedBalance() {
            return valuedBalance;
        }

        public void setValuedBalance(BigDecimal valuedBalance) {
            this.valuedBalance = valuedBalance;
        }
    }



    public class ArticleMovement{

        private Date date;
        private String productCode;
        private MovementDetailType movementType;
        private BigDecimal quantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;

        ArticleMovement(Date date, String productCode, MovementDetailType movementType, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost){
            this.setDate(date);
            this.setProductCode(productCode);
            this.setMovementType(movementType);
            this.setQuantity(quantity);
            this.setUnitCost(unitCost);
            this.setTotalCost(totalCost);
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
        }

        public MovementDetailType getMovementType() {
            return movementType;
        }

        public void setMovementType(MovementDetailType movementType) {
            this.movementType = movementType;
        }
    }

}