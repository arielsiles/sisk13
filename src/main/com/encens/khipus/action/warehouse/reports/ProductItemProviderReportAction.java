package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

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

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    public void generateReport() {
        log.debug("Generate ProductItemProviderReportAction......");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        parameters.putAll(paramMap);
        super.generateReport("productItemProviderReport", "/warehouse/reports/productItemProviderReport.jrxml", MessageUtils.getMessage("Reports.productItemProvider.title").toUpperCase(), parameters);
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