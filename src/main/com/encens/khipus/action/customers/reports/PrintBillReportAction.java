package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.billing.SendMessageAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Dosage;
import com.encens.khipus.model.customers.InvoicePrintType;
import com.encens.khipus.model.customers.Movement;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.DosageService;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.*;
import com.encens.khipus.util.barcode.BarcodeRenderer;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * Action to generate product delivery receipt report
 *
 * @author
 * @version $Id: PrintInvoiceReportAction.java  23-sep-2010 18:25:14$
 */
@Name("printBillReportAction")
@Scope(ScopeType.PAGE)
public class PrintBillReportAction extends GenericReportAction {

    @In
    private User currentUser;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private FacesMessages facesMessages;

    @In
    private SaleService saleService;

    @In
    private DosageService dosageService;

    @In
    private UserService userService;

    @In(create = true)
    private SendMessageAction sendMessageAction;

    private Long customerOrderId;
    private CustomerOrder lastCustomerOrder;


    //private Dosage dosage;
    private CustomerOrder customerOrder;
    private MoneyUtil moneyUtil;
    private BarcodeRenderer barcodeRenderer;
    private Date date;

    @Restrict("#{s:hasPermission('PRINTINVOICE','VIEW')}")
    public void generateReport() {
        log.debug("Generate BillInvoiceReportAction......");

        User user = getUser(currentUser.getId());
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId()); /** Solo es impresion, revisar la dosificacion que obtiene??? **/
        this.customerOrderId = saleService.findLastSaleId(user);
        this.lastCustomerOrder = saleService.findSaleById(getCustomerOrderId());
        setReportFormat(ReportFormat.PDF);


        if (!hasValidInvoice(lastCustomerOrder)){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FACTURACION INVALIDA, Consulte con el Administrador");
            return;
        }


        if (lastCustomerOrder.getMovement() != null){
            if (lastCustomerOrder.getMovement().getDescri() != null) {
                if (lastCustomerOrder.getMovement().getDescri().equals("RECHAZADA")) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FACTURA RECHAZADA, No se puede imprimir.");
                    return;
                }
            }
        }

        Map params = new HashMap();
        params.putAll(getReportParams(dosage, lastCustomerOrder));
        TypedReportData reportData = addDetailReport(params, lastCustomerOrder);

        try {
            GenerationReportData generationReportData = new GenerationReportData(reportData);
            generationReportData.exportReport();
            generatePdfFileReport(lastCustomerOrder, generationReportData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generatePdfFileReport(CustomerOrder customerOrder, GenerationReportData generationReportData) {

        String destFileName = Constants.PATH_FILE_INVOICE + Constants.PREFIX_NAME_INVOICE + customerOrder.getMovement().getNumber() + ".pdf";

        JasperPrint jasperPrint = generationReportData.getExportReport().getJasperPrint();
        try {
            JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    public void sendMailAttachments(){

        User user = getUser(currentUser.getId());
        this.customerOrderId = saleService.findLastSaleId(user);
        this.lastCustomerOrder = saleService.findSaleById(getCustomerOrderId());

        sendMessageAction.sendEmailAttachment(lastCustomerOrder);

    }

    private boolean hasValidInvoice(CustomerOrder customerOrder){
        boolean result = false;
        if (customerOrder.getMovement() != null) {
            /** todo **/
            if (customerOrder.getMovement().getCuf() != null) {
                result = true;
            }
        }

        return result;
    }

    public void generateInvoicesReport(List<CustomerOrder> customerOrderList){
        User user = getUser(currentUser.getId());
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId()); /** Obtiene la dosificacion activa de la sucursal del usuario ? **/
        setReportFormat(ReportFormat.PDF);

        System.out.println("---------> Pedido List 0: " + customerOrderList.get(0));
        System.out.println("---------> Pedido List 0: " + customerOrderList.get(0).getTotalAmount());

        /** Ordenar **/
        Collections.sort(customerOrderList, new Comparator<CustomerOrder>() {
            @Override
            public int compare(CustomerOrder o1, CustomerOrder o2) {
                return o1.getMovement().getNumber().compareTo(o2.getMovement().getNumber());
            }
        });

        List<TypedReportData> reportDataList = new ArrayList<TypedReportData>();
        for (CustomerOrder order : customerOrderList){

            if (!hasValidInvoice(order)){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"FACTURACION INVALIDA, Consulte con el Administrador");
                return;
            }

            dosage = dosageService.findDosageByOffice(order.getUser().getBranchOffice().getId());

            Map params = new HashMap();
            params.putAll(getReportParams(dosage, order));
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

    /**
     * get report params
     *
     * @return Map
     */
    private Map<String, Object> getReportParams(Dosage dosage, CustomerOrder lastCustomerOrder) {
        String filePath = FileCacheLoader.i.getPath("/customers/reports/qr_inv.png");

        moneyUtil = new MoneyUtil();
        barcodeRenderer = new BarcodeRenderer();

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        BigDecimal subtotal    = BigDecimalUtil.sum(BigDecimalUtil.toBigDecimal(lastCustomerOrder.getTotalAmount()), lastCustomerOrder.getAdditionalDiscountValue());
        BigDecimal totalAmount = BigDecimalUtil.toBigDecimal(lastCustomerOrder.getTotalAmount());


        String legend1 = companyConfiguration.getOneLegend();
        String legend3 = companyConfiguration.getOnlineLegend();
        if (lastCustomerOrder.getMovement().getEmissionType().equals("OFFLINE"))
            legend3 = companyConfiguration.getOfflineLegend();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("companyNit", dosage.getCompanyNit());
        paramMap.put("invoiceNumber", lastCustomerOrder.getMovement().getNumber().longValue());
        paramMap.put("authorizationNumber", dosage.getAuthorizationNumber());
        paramMap.put("clientNit", lastCustomerOrder.getMovement().getNit());
        paramMap.put("clientCode", lastCustomerOrder.getClient().getId().toString());
        paramMap.put("name", lastCustomerOrder.getMovement().getName());
        paramMap.put("invoiceDateLiteral", DateUtils.getLiteralDate("Cochabamba", lastCustomerOrder.getMovement().getDate()));
        paramMap.put("expirationDate", dosage.getExpirationDate());
        paramMap.put("controlCode", "");
        paramMap.put("companyLabel", dosage.getCompanyLabel());
        paramMap.put("lawLabel", dosage.getLawLabel());
        paramMap.put("labelType", "");
        paramMap.put("subtotal", subtotal.doubleValue());
        paramMap.put("discount", lastCustomerOrder.getAdditionalDiscountValue().doubleValue());
        paramMap.put("total", totalAmount.doubleValue());
        paramMap.put("literalTotal", moneyUtil.Convertir(totalAmount.toString(), true, messages.get("Reports.cashAvailable.bs")));

        paramMap.put("legend1", legend1);
        paramMap.put("legend3", legend3);

        paramMap.put("activity", dosage.getBranchOffice().getActivity());
        paramMap.put("companyName", dosage.getBranchOffice().getCompanyName());
        paramMap.put("branchName", dosage.getBranchOffice().getBranchName());
        paramMap.put("posName", dosage.getBranchOffice().getPosName());
        paramMap.put("phone", dosage.getBranchOffice().getPhone());
        paramMap.put("place", dosage.getBranchOffice().getPlace());
        paramMap.put("address", dosage.getBranchOffice().getAddress());

        Movement movement = lastCustomerOrder.getMovement();
        paramMap.put("cuf", movement.getCuf());
        paramMap.put("fechasin", new Date(new Long(movement.getFechaSin())));
        paramMap.put("leyenda", movement.getLeyenda());

        // https://pilotosiat.impuestos.gob.bo/facturacionv2/public/Qr.xhtml?nit=valorNit&cuf=valorCuf&numero=valorNroFactura&t=valorTamaño
        String urlcode = companyConfiguration.getQrcodeURL();
        String paramNit = "valorNit";
        String paramCuf = "valorCuf";
        String paramNroFactura = "valorNroFactura";

        urlcode = urlcode.replaceAll(paramNit, dosage.getCompanyNit());
        urlcode = urlcode.replaceAll(paramCuf, movement.getCuf());
        urlcode = urlcode.replaceAll(paramNroFactura, lastCustomerOrder.getMovement().getNumber().toString());
        paramMap.put("llaveQR", urlcode);

        System.out.println("URL CODE: " + urlcode);

        barcodeRenderer.generateQR(lastCustomerOrder.getMovement().getQrCode(), filePath);
        //barcodeRenderer.generateQR(newUrlCode, filePath);

        return paramMap;
    }

    /**
     * add voucher movement detail sub report in main report
     *
     * @param
     */
    private TypedReportData addDetailReport(Map<String, Object> params, CustomerOrder order) {
        log.debug("Generating addVoucherMovementDetailSubReport.............................");

        this.customerOrder = order;

        String ejbql = "SELECT " +
                " articleOrder.quantity as quantity, " +
                " articleOrder.productItem.name as name, " +
                " articleOrder.price as price, " +
                " articleOrder.total as total, "+
                " articleOrder.amount as amount, "+
                " articleOrder.customerOrder.description as description," +
                " articleOrder.codArt," +
                " articleOrder.discount, "+
                " articleOrder.productItem.measureUnitDescription as uniMed "+
                " FROM ArticleOrder articleOrder";

        String[] restrictions = new String[]{"articleOrder.customerOrder.id = #{printBillReportAction.customerOrder.getId()}"};

        String orderBy = "articleOrder.productItem.name";

        //generate the sub report
        String subReportKey = "INVOICEDETAILSUBREPORT";
        return super.getReport(
                subReportKey,
                "/customers/reports/billReceptionReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                "FACTURAS",
                params);

    }
    /** ----------------------------- **/
    /** ----------------------------- **/

    @Override
    protected String getEjbql() {
        return "";
    }

    @Create
    public void init() {
        restrictions = new String[]{};
        //sortProperty = "productDelivery.id";
    }

    @Factory(value = "billPrintTypes", scope = ScopeType.STATELESS)
    public InvoicePrintType[] initProductDeliveryTypes() {
        return InvoicePrintType.values();
    }


    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    private User getUser(Long id) {
        try {
            return userService.findById(User.class, id);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

}
