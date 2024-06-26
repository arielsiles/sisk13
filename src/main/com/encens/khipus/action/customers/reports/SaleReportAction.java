package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MoneyUtil;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("saleReportAction")
@Scope(ScopeType.PAGE)
public class SaleReportAction extends GenericReportAction {

    @In
    private User currentUser;
    @In
    private SessionUser sessionUser;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private SaleService saleService;

    /*@In
    private SalesAction salesAction;*/

    private Long customerOrderId;
    private CustomerOrder lastCustomerOrder;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {
        System.out.println("-----------> user: " + currentUser.getUsername());
        return "SELECT " +
                " customerOrder.client, " +
                " customerOrder.orderDate, " +
                " articleOrder.codArt as productItemCode, " +
                " articleOrder.productItem, " +
                " articleOrder.price, " +
                " articleOrder.quantity, " +
                " articleOrder.amount, " +
                " articleOrder.promotion, " +
                " articleOrder.reposicion as replacement, " +
                " customerOrder.code, " +
                " articleOrder.discount " +
                " FROM  CustomerOrder as customerOrder " +
                " JOIN customerOrder.articleOrderList articleOrder " +
                " WHERE customerOrder.id = " + this.lastCustomerOrder.getId();
    }

    /*
    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();
        }

        setCustomerOrderId(saleService.findLastSaleId(currentUser));
        setLastCustomerOrder(saleService.findSaleById(getCustomerOrderId()));

        BigDecimal additionalDiscount = lastCustomerOrder.getAdditionalDiscountValue();
        BigDecimal subtotal    = BigDecimalUtil.sum(BigDecimalUtil.toBigDecimal(lastCustomerOrder.getTotalAmount()), additionalDiscount);
        BigDecimal totalAmount = BigDecimalUtil.toBigDecimal(lastCustomerOrder.getTotalAmount());


        MoneyUtil money = new MoneyUtil();
        String documentTitle = messages.get("Sale.report.deliveryNote.title");
        Long saleNumber =  lastCustomerOrder.getCode();
        if (lastCustomerOrder.getSaleType().equals(SaleTypeEnum.CASH)) {
            documentTitle = messages.get("Sale.report.entryNote.title");
            saleNumber = BigDecimalUtil.toBigDecimal(lastCustomerOrder.getVoucher().getDocumentNumber()).longValue();
        }

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("currentUser.username", currentUser.getUsername());
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("saleNumber", saleNumber);

        reportParameters.put("subtotal", subtotal.doubleValue());
        reportParameters.put("discount", additionalDiscount.doubleValue());
        reportParameters.put("totalAmount", totalAmount.doubleValue());

        String literalAmount = money.Convertir(totalAmount.toString(), true, messages.get("Reports.cashAvailable.bs"));
        reportParameters.put("literalAmount", literalAmount);

        String observation = "";
        if (lastCustomerOrder.getMovement() != null)
            observation = "F." + lastCustomerOrder.getMovement().getNumber();

        observation = observation + ((lastCustomerOrder.getObservation() == null) ? "" : lastCustomerOrder.getObservation());
        reportParameters.put("observation", observation);

        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "saleReport",
                "/customers/reports/saleReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                messages.get("Sale.report.title"),
                reportParameters);
    }
    */

    public void generateReport() {

        setCustomerOrderId(saleService.findLastSaleId(currentUser));
        setLastCustomerOrder(saleService.findSaleById(getCustomerOrderId()));

        Map<String, Object> reportParameters = getReportParams(lastCustomerOrder);

        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "saleReport",
                "/customers/reports/saleReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                messages.get("Sale.report.title"),
                reportParameters);
    }

    public void generateNotesReport(List<CustomerOrder> customerOrderList){
        setReportFormat(ReportFormat.PDF);
        List<TypedReportData> reportDataList = new ArrayList<TypedReportData>();

        /** Ordenar **/
        Collections.sort(customerOrderList, new Comparator<CustomerOrder>() {
            @Override
            public int compare(CustomerOrder o1, CustomerOrder o2) {
                return o1.getCode().compareTo(o2.getCode());
            }
        });

        for (CustomerOrder order : customerOrderList){
            Map params = new HashMap();
            params.putAll(getReportParams(order));
            reportDataList.add(addDetailReport(params, order));
        }

        TypedReportData result = reportDataList.get(0);
        for (int i=1 ; i < reportDataList.size() ; i++){
            result.getJasperPrint().getPages().addAll(reportDataList.get(i).getJasperPrint().getPages());
        }

        try {
            GenerationReportData generationReportData = new GenerationReportData(result);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Map<String, Object> getReportParams(CustomerOrder customerOrder) {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();
        }
        MoneyUtil money = new MoneyUtil();
        String documentTitle = messages.get("Sale.report.deliveryNote.title");
        //Long saleNumber =  customerOrder.getCode();
        String saleNumber =  customerOrder.getCode().toString();

        if (customerOrder.getSaleType().equals(SaleTypeEnum.CASH)){
            documentTitle = messages.get("Sale.report.entryNote.title");
            saleNumber = customerOrder.getVoucher().getDocumentNumber();
        }


        BigDecimal additionalDiscount = customerOrder.getAdditionalDiscountValue();
        BigDecimal subtotal    = BigDecimalUtil.sum(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), additionalDiscount);
        BigDecimal totalAmount = BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount());

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("currentUser.username", currentUser.getUsername());
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("saleNumber", saleNumber);

        reportParameters.put("subtotal", subtotal.doubleValue());
        reportParameters.put("discount", additionalDiscount.doubleValue());
        reportParameters.put("totalAmount", totalAmount.doubleValue());

        String literalAmount = money.Convertir(totalAmount.toString(), true, messages.get("Reports.cashAvailable.bs"));
        reportParameters.put("literalAmount", literalAmount);

        String observation = "";
        if (customerOrder.getMovement() != null)
            observation = "F." + customerOrder.getMovement().getNumber();

        observation = observation + ((customerOrder.getObservation() == null) ? "" : customerOrder.getObservation());
        reportParameters.put("observation", observation);

        return reportParameters;
    }

    private TypedReportData addDetailReport(Map<String, Object> params, CustomerOrder order) {
        log.debug("Generating addVoucherMovementDetailSubReport.............................");

        this.lastCustomerOrder = order;

        String ejbql = "SELECT " +
                " customerOrder.client, " +
                " customerOrder.orderDate, " +
                " articleOrder.codArt as productItemCode, " +
                " articleOrder.productItem, " +
                " articleOrder.price, " +
                " articleOrder.quantity, " +
                " articleOrder.amount, " +
                " articleOrder.promotion, " +
                " articleOrder.reposicion as replacement, " +
                " customerOrder.code, " +
                " articleOrder.discount " +
                " FROM  CustomerOrder as customerOrder " +
                " JOIN customerOrder.articleOrderList articleOrder ";
                //" WHERE customerOrder.id = " + this.lastCustomerOrder.getId();

        String[] restrictions = new String[]{"articleOrder.customerOrder.id = #{saleReportAction.lastCustomerOrder.getId()}"};
        String orderBy = "articleOrder.productItem.name";

        //generate the sub report
        String subReportKey = "saleReport";
        return super.getReport(
                subReportKey,
                "/customers/reports/saleReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                "NOTAS",
                params);

    }

    public Long getCustomerOrderId() {
        return customerOrderId;
    }

    public void setCustomerOrderId(Long customerOrderId) {
        this.customerOrderId = customerOrderId;
    }

    public CustomerOrder getLastCustomerOrder() {
        return lastCustomerOrder;
    }

    public void setLastCustomerOrder(CustomerOrder lastCustomerOrder) {
        this.lastCustomerOrder = lastCustomerOrder;
    }
}