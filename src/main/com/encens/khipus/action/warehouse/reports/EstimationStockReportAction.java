package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.production.ProductProcessing;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.production.ProductionPlanningService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.warehouse.WarehouseService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("estimationStockReportAction")
@Scope(ScopeType.PAGE)
/*@Restrict("#{s:hasPermission('ESTIMATIONSTOCKREPORT','VIEW')}")*/
public class EstimationStockReportAction extends GenericReportAction {

    private ProductItem productItem;
    private Warehouse warehouse;
    private Date date;
    private Gestion gestion;

    @In
    private WarehouseService warehouseService;

    @In
    private ProductItemService productItemService;

    @Create
    public void init() {
        restrictions = new String[]{
                "warehouse = #{estimationStockReportAction.warehouse}",
                "productItem = #{estimationStockReportAction.productItem}"
        };
        this.warehouse = warehouseService.findWarehouseByCode("2");
        sortProperty = "productItem.name";
        sortAsc = true;
    }

    @Override
    protected String getEjbql() {
        return "SELECT inventory.articleCode, " +
                "      productItem.name , " +
                "      warehouse.name, " +
                "      inventory.unitaryBalance, " +
                "      warehouse.id " +
                "FROM  Inventory inventory " +
                " join inventory.warehouse warehouse " +
                " join inventory.productItem productItem" ;

    }

    public void generateReport() {
        log.debug("Generating valued Estimation Stock report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("dateDelivery",date);
        super.generateReport(
                "estimationStockReport",
                "/warehouse/reports/estimationStockReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("Reports.warehouse.EstimationStockReport"),
                reportParameters);
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void assignProductItem(ProcessedProduct productItem) {
        this.productItem = productItemService.findProductItemByCode(productItem.getProductItemCode());
    }

    public void cleanProductItem() {
        this.productItem = null;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void clearWarehouse() {
        setWarehouse(null);
    }

    public void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }
}
