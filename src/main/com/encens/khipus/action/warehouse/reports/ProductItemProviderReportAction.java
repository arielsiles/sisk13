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
 * Encens S.R.L.
 * Action to generate product item provider report
 *
 * @author
 * @version $Id: ProductItemProviderReport.java  26-abr-2010 16:54:53$
 */
@Name("productItemProviderReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTITEMPROVIDERREPORT','VIEW')}")
public class ProductItemProviderReportAction extends GenericReportAction {

    private ProductItem productItem;
    private Provider provider;

    public void generateReport() {
        log.debug("Generate ProductItemProviderReportAction......");

        Map params = new HashMap();
        super.generateReport("productItemProviderReport", "/warehouse/reports/productItemProviderReport.jrxml", MessageUtils.getMessage("Reports.productItemProvider.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "financesEntity.acronym," +
                "productItem.productItemCode," +
                "productItem.name," +
                "provide.groupAmount," +
                "provide.delivery," +
                "groupMeasureUnit.name" +
                " FROM Provide provide" +
                " LEFT JOIN provide.provider provider" +
                " LEFT JOIN provider.entity financesEntity" +
                " LEFT JOIN provide.productItem productItem" +
                " LEFT JOIN provide.groupMeasureUnit groupMeasureUnit";

    }

    @Create
    public void init() {
        restrictions = new String[]{
                "productItem=#{productItemProviderReportAction.productItem}",
                "provider=#{productItemProviderReportAction.provider}"
        };

        sortProperty = "financesEntity.acronym,productItem.productItemCode,productItem.name";
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