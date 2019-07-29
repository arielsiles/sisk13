package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherCreateAction;
import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.MoneyUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("voucherReportAction")
@Scope(ScopeType.PAGE)
public class VoucherReportAction extends GenericReportAction {

    private Long voucherId;
    private Voucher voucher;

    private Date startDate = new Date();
    private Date endDate = new Date();

    @In(create = true)
    VoucherUpdateAction voucherUpdateAction;
    @In(create = true)
    VoucherCreateAction voucherCreateAction;
    @In
    private VoucherService voucherService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    @Create
    public void init() {
        restrictions = new String[]{
                "voucher.id=#{voucherReportAction.voucherId}"
        };
        //sortProperty = "name";
    }

    @Override
    protected String getEjbql() {
        //DateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //For Oracle
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        /*String start = df.format(startDate);
        String end = df.format(endDate);*/
        String start = "2015-09-01";
        String end = "2015-09-30";

        /*return  " SELECT distinct productionOrder.productComposition.processedProduct.productItem.productItemCode as productItemCode " +
                " ,productionOrder.productComposition.processedProduct.productItem.name as name" +
                " FROM  ProductionPlanning productionPlanning " +
                " inner join productionPlanning.productionOrderList productionOrder " +
                " where productionPlanning.date between '"+start+"' and '"+end+"'";*/

        /*return  " SELECT distinct productionOrder.productComposition.processedProduct.productItem.productItemCode as productItemCode, " +
                "        productionOrder.productComposition.processedProduct.productItem.name as name" +
                " FROM  ProductionPlanning productionPlanning " +
                " inner join productionPlanning.productionOrderList productionOrder ";*/


        /*return " SELECT productItem.productItemCode as productItemCode," +
               " productItem.name as name " +
               " FROM   ProductItem productItem " +
               " WHERE  productItem.productItemAccount = '4460114500'";*/

        return " SELECT voucher, " +
               "        voucherDetail.account as account, " +
               "        voucherDetail.cashAccount.description as description," +
               "        voucherDetail.debit as debit, " +
               "        voucherDetail.credit as credit, " +
               "        voucherDetail " +
               "  FROM  Voucher voucher " +
               "  LEFT JOIN voucher.voucherDetailList voucherDetail ";

    }

    public void generateReport(Voucher voucher) throws CompanyConfigurationNotFoundException {
        voucherId = voucher.getId();
        this.voucher = voucher;

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        String title = companyConfiguration.getTitle();
        String subTitle = companyConfiguration.getSubTitle();

        BigDecimal totalD = voucherCreateAction.getTotalsDebit();
        BigDecimal totalC = voucherCreateAction.getTotalsCredit();

        String documentTitle = (voucherService.getDocType(voucher.getDocumentType())).getDescription();

        MoneyUtil money = new MoneyUtil();
        String literalAmount = "";
        literalAmount = ( totalD.compareTo(totalC) == 0 ? money.Convertir(totalD.toString(), true) : "");

        log.debug("Generating products produced report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("title", title);
        reportParameters.put("subTitle", subTitle);
        reportParameters.put("startDate",startDate);
        reportParameters.put("endDate",endDate);
        reportParameters.put("totalD", totalD);
        reportParameters.put("totalC", totalC);
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("literalAmount", literalAmount);

        String fileReport = "/accounting/reports/voucherReport.jrxml";

        if (voucher.getDocumentType().equals("CP"))
            fileReport = "/accounting/reports/voucher5Report.jrxml";

        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "voucherReport",
                fileReport,
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                messages.get("Voucher.accountingEntry.titleReport"),
                reportParameters);
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

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
}
