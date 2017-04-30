package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action for fixedAssets
 *
 * @author
 * @version 2.4.2
 */
@Name("fixedAssetPurchaseOrderPrintAction")
@Scope(ScopeType.PAGE)
public class FixedAssetPurchaseOrderPrintAction extends GenericReportAction {
    private PurchaseOrder purchaseOrder;

    @In
    private User currentUser;

    @Create
    public void init() {
        restrictions = new String[]{
                "purchaseOrder=#{fixedAssetPurchaseOrderPrintAction.purchaseOrder}"
        };

        sortProperty = "purchaseOrder.id";
    }


    protected String getEjbql() {
        return "SELECT purchaseOrder,purchaseOrder.id FROM  PurchaseOrder as purchaseOrder ";
    }

    public void generateReport(PurchaseOrder fixedAssetPurchaseOrder) {
        setPurchaseOrder(getEntityManager().find(PurchaseOrder.class, fixedAssetPurchaseOrder.getId()));
        log.debug("generating fixedAssetPurchaseOrderReport......................................id: " + purchaseOrder.getId());

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("currentUser.username", currentUser.getEmployee().getFullName());

        setReportFormat(ReportFormat.PDF);

        addFixedAssetPurchaseOrderDetailSubReport(reportParameters);
        addPurchaseOrderFixedAssetPartSubReport(reportParameters);

        super.generateReport(
                "fixedAssetPurchaseOrderReport",
                "/fixedassets/reports/fixedAssetPurchaseOrderReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("FixedAsset.purchaseOrder.report.title"),
                reportParameters);
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    private void addFixedAssetPurchaseOrderDetailSubReport(Map<String, Object> mainReportParams) {
        log.debug("Generating addFixedAssetPurchaseOrderDetailSubReport.............................!!!!!!!");
        Map<String, Object> params = new HashMap<String, Object>();

        // execute fixedassetMovementCommonSubReport
        String subReportKey = "FIXEDASSETPURCHASEORDERDETAILSUBREPORT";
        String showSubReportKey = "SHOW_FIXEDASSETPURCHASEORDERDETAILSUBREPORT";

        String ejbql =
                "SELECT  fixedAssetPurchaseOrderDetail.id," +
                        "      fixedAssetPurchaseOrderDetail.detailNumber, " +
                        "      fixedAssetPurchaseOrderDetail.requestedQuantity, " +
                        "      fixedAssetGroup.groupCode, " +
                        "      fixedAssetGroup.description, " +
                        "      fixedAssetSubGroup.fixedAssetSubGroupCode, " +
                        "      fixedAssetSubGroup.description, " +
                        "      fixedAssetPurchaseOrderDetail.detail, " +
                        "      fixedAssetPurchaseOrderDetail.bsUnitPriceValue, " +
                        "      fixedAssetPurchaseOrderDetail.ufvUnitPriceValue, " +
                        "      fixedAssetPurchaseOrderDetail.bsTotalAmount, " +
                        "      fixedAssetPurchaseOrderDetail.ufvTotalAmount, " +
                        "      orderDetailPart.id, " +
                        "      orderDetailPart.number, " +
                        "      orderDetailPart.description, " +
                        "      measureUnit.measureUnitCode, " +
                        "      measureUnit.name, " +
                        "      orderDetailPart.unitPrice, " +
                        "      orderDetailPart.totalPrice " +
                        "FROM  FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail " +
                        "      LEFT JOIN fixedAssetPurchaseOrderDetail.orderDetailPartList orderDetailPart " +
                        "      LEFT JOIN orderDetailPart.measureUnit measureUnit " +
                        "      LEFT JOIN fixedAssetPurchaseOrderDetail.fixedAssetSubGroup fixedAssetSubGroup " +
                        "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                        "      LEFT JOIN fixedAssetPurchaseOrderDetail.purchaseOrder purchaseOrder";

        String[] restrictions = new String[]{
                "purchaseOrder = #{fixedAssetPurchaseOrderPrintAction.purchaseOrder}"};

        String fixedassetMovementCommonSubReportOrderBy = "fixedAssetPurchaseOrderDetail.id, fixedAssetPurchaseOrderDetail.detailNumber, orderDetailPart.id, orderDetailPart.number ";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetPurchaseOrderDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), fixedassetMovementCommonSubReportOrderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
        mainReportParams.put(showSubReportKey, getPurchaseOrder().getPurchaseOrderCause().isFixedassetPurchase());

    }

    private void addPurchaseOrderFixedAssetPartSubReport(Map<String, Object> mainReportParams) {
        log.debug("Generating addPurchaseOrderFixedAssetPartSubReport.............................!!!!!!!");
        Map<String, Object> params = new HashMap<String, Object>();

        // execute purchaseOrderFixedAssetPartSubReport
        String subReportKey = "PURCHASEORDERFIXEDASSETPARTSUBREPORT";
        String showSubReportKey = "SHOW_PURCHASEORDERFIXEDASSETPARTSUBREPORT";

        String ejbql = "SELECT " +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAssetPart.description, " +
                "measureUnit.measureUnitCode, " +
                "measureUnit.name, " +
                "fixedAssetPart.serialNumber, " +
                "fixedAssetPart.unitPrice " +
                " FROM PurchaseOrderFixedAssetPart fixedAssetPart" +
                " LEFT JOIN fixedAssetPart.measureUnit measureUnit" +
                " LEFT JOIN fixedAssetPart.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAssetPart.purchaseOrder purchaseOrder";

        String[] restrictions = new String[]{
                "purchaseOrder = #{fixedAssetPurchaseOrderPrintAction.purchaseOrder}"};

        String orderBy = "fixedAsset.barCode, fixedAssetPart.description";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/purchaseOrderFixedAssetPartSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
        mainReportParams.put(showSubReportKey, getPurchaseOrder().getPurchaseOrderCause().isFixedassetPartsPurchase());
    }
}
