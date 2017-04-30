package com.encens.khipus.action.accounting;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import net.sf.jasperreports.engine.JRException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.IOException;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("voucherPrintAction")
@Scope(ScopeType.PAGE)
public class VoucherPrintAction extends GenericReportAction {
    //private Long purchaseOrderId;
    private Long voucherId;
    private Voucher voucher;

    @In
    private User currentUser;

    @Create
    public void init() {
        restrictions = new String[]{
                //"purchaseOrder.id=#{warehousePurchaseOrderPrintAction.purchaseOrderId}"
                //"voucher.id=#{voucherPrintAction.voucherId}"
                //"voucherDetail.voucher=#{voucherPrintAction.voucher}"
        };

        //sortProperty = "purchaseOrder.id, purchaseOrderDetail.detailNumber";
        //sortProperty = "voucher.id";
    }


    protected String getEjbql() {
              /*"SELECT purchaseOrder, " +
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
                "      LEFT JOIN purchaseOrderDetail.purchaseMeasureUnit measureUnit";*/

        return "SELECT " +
                "      voucher.transactionNumber " +
                "FROM  Voucher as voucher ";
    }

    //public void generateReport(PurchaseOrder warehousePurchaseOrder) {
    public void generateReport(Voucher voucher) throws JRException, IOException {

        voucherId = voucher.getId();
        this.setVoucher(voucher);

        System.out.println("generating voucherReport......................................id: " + voucherId);
        log.debug("generating voucherReport......................................id: " + voucherId);

        System.out.println("____SIZE VOUCHER DETAIL LIST: " + voucher.getVoucherDetailList().size());

        for(VoucherDetail voucherDetail : voucher.getVoucherDetailList()){
            System.out.println("---------------------------");
            System.out.println("Account: " + voucherDetail.getAccount());
            System.out.println("Debe: " + voucherDetail.getDebit());
            System.out.println("Haber: " + voucherDetail.getCredit());
            System.out.println("---------------------------");
        }

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("currentUser.username", currentUser.getEmployee().getFullName());

        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "voucherReport6",
                "/accounting/reports/voucherReport6.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Voucher.report.title"),
                reportParameters);

        /*Collection beanCollection = voucher.getVoucherDetailList();
        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(beanCollection);
        JasperPrint jasperPrint = JasperFillManager.fillReport("D:/voucherReport5.jasper", new HashMap(), beanCollectionDataSource);

        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        //httpServletResponse.addHeader("Content-disposition", "attachment; file-name=voucher.pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();*/

    }

    public void generateVoucherReport(Voucher voucher){

        //File jasper = new File(FacesContext.getCurrentInstance().getExternalContext().get );

    }

    public Long getVoucherId() {
        return voucherId;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
}
