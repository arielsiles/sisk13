package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("warehousePurchaseOrderPrintAction")
@Scope(ScopeType.PAGE)
public class WarehousePurchaseOrderPrintAction extends GenericReportAction {
    private Long purchaseOrderId;

    @In
    private User currentUser;
    @In
    private CompanyConfigurationService companyConfigurationService;

    @Create
    public void init() {
        restrictions = new String[]{
                "purchaseOrder.id=#{warehousePurchaseOrderPrintAction.purchaseOrderId}"
        };

        sortProperty = "purchaseOrder.id, purchaseOrderDetail.detailNumber";
    }


    protected String getEjbql() {
        return "SELECT purchaseOrder, " +
                "      purchaseOrderDetail.detailNumber, " +
                "      productItem.name, " +
                "      purchaseOrderDetail.requestedQuantity, " +
                "      measureUnit.name, " +
                "      purchaseOrderDetail.totalAmount, " +
                "      purchaseOrderDetail.unitCost, " +
                "      productItem.productItemCode, " +
                "      purchaseOrderDetail.warning " +
                "FROM  PurchaseOrder as purchaseOrder " +
                "      LEFT JOIN purchaseOrder.purchaseOrderDetailList purchaseOrderDetail" +
                "      LEFT JOIN purchaseOrderDetail.productItem productItem" +
                "      LEFT JOIN purchaseOrderDetail.purchaseMeasureUnit measureUnit";
    }

    public void generateReport(PurchaseOrder warehousePurchaseOrder) {

        purchaseOrderId = warehousePurchaseOrder.getId();
        log.debug("generating purchaseOrderReport......................................id: " + purchaseOrderId);

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();
        }

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("currentUser.username", currentUser.getEmployee().getFullName());
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());

        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "purchaseOrderReport",
                "/warehouse/reports/warehousePurchaseOrderReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Warehouse.purchaseOrder.report.title"),
                reportParameters);
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }
}
