package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemState;
import com.encens.khipus.model.warehouse.Warehouse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Encens S.R.L.
 * This class implements the articles report action
 *
 * @author
 * @version 2.3
 */

@Name("articlesReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ARTICLESREPORT','VIEW')}")
public class ArticlesReportAction extends GenericReportAction {
    private Warehouse warehouse;
    private ProductItem productItem;
    private ProductItemState productItemState;
    private List<ProductItemState> productItemStateList = Arrays.asList(ProductItemState.values());

    @Create
    public void init() {
        restrictions = new String[]{
                "warehouse=#{articlesReportAction.warehouse}",
                "productItem=#{articlesReportAction.productItem}",
                "productItem.state = #{articlesReportAction.productItemState}"
        };
        sortProperty = "warehouse.id.warehouseCode,productItem.productItemCode";
    }

    @Override
    protected String getEjbql() {
        return "SELECT warehouse.id.warehouseCode, " +
                "      warehouse.name, " +
                "      productItem.productItemCode, " +
                "      productItem.state, " +
                "      productItem.name, " +
                "      usageMeasureUnit.name, " +
                "      groupItem.groupCode, " +
                "      groupItem.name, " +
                "      cashAccount.accountCode, " +
                "      cashAccount.description, " +
                "      inventory.unitaryBalance, " +
                "      productItem.unitCost, " +
                "      productItem.id.companyNumber " +
                "FROM  ProductItem productItem " +
                "      LEFT JOIN productItem.usageMeasureUnit usageMeasureUnit" +
                "      LEFT JOIN productItem.inventories inventory " +
                "      LEFT JOIN inventory.warehouse warehouse " +
                "      LEFT JOIN productItem.subGroup subGroup " +
                "      LEFT JOIN subGroup.group groupItem " +
                "      LEFT JOIN productItem.cashAccount cashAccount " +
                "WHERE inventory is not null";

    }

    public void generateReport() {
        log.debug("Generating articles report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "articlesReport",
                "/warehouse/reports/articlesReport.jrxml",
                messages.get("ArticleReport.report.title"),
                reportParameters);
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

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void cleanProductItem() {
        this.productItem = null;
    }

    public ProductItemState getProductItemState() {
        return productItemState;
    }

    public void setProductItemState(ProductItemState productItemState) {
        this.productItemState = productItemState;
    }

    public List<ProductItemState> getProductItemStateList() {
        return productItemStateList;
    }
}
