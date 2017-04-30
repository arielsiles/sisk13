package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * ProductItemByProviderHistoryReportAction
 *
 * @author
 * @version 2.27
 */
@Name("productItemByProviderHistoryReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTITEMBYPROVIDERHISTORYREPORT','VIEW')}")
public class ProductItemByProviderHistoryReportAction extends GenericReportAction {

    private ProductItem productItem;
    private Provider provider;

    public void generateReport() {
        log.debug("Generate productItemByProviderHistoryReportAction......");

        Map params = new HashMap();
        super.generateReport("productItemByProviderHistoryReport", "/warehouse/reports/productItemByProviderHistoryReport.jrxml", MessageUtils.getMessage("Reports.productItemByProviderHistory.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "productItem.productItemCode," +
                "productItem.name," +
                "groupMeasureUnit.name," +
                "financesEntity.id," +
                "financesEntity.acronym," +
                "productItemByProviderHistory.date," +
                "productItemByProviderHistory.unitCost" +
                " FROM Provide provide" +
                " LEFT JOIN provide.provider provider" +
                " LEFT JOIN provider.entity financesEntity" +
                " LEFT JOIN provide.productItem productItem" +
                " LEFT JOIN provide.groupMeasureUnit groupMeasureUnit" +
                " LEFT JOIN provide.productItemByProviderHistoryList productItemByProviderHistory";

    }

    @Create
    public void init() {
        restrictions = new String[]{
                "productItem=#{productItemByProviderHistoryReportAction.productItem}",
                "provider=#{productItemByProviderHistoryReportAction.provider}"
        };

        sortProperty = "productItem.productItemCode, productItem.name, financesEntity.id, financesEntity.acronym, productItemByProviderHistory.date";
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

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }


    public void assignProvider(Provider provider) {
        this.provider = provider;
    }

    public void cleanProvider() {
        this.provider = null;
    }
}