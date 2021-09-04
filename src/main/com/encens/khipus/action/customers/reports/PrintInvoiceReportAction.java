package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.customers.PrintInvoiceDataModel;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.DosageService;
import com.encens.khipus.service.customers.MovementService;
import com.encens.khipus.service.customers.RePrintsService;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FileCacheLoader;
import com.encens.khipus.util.MoneyUtil;
import com.encens.khipus.util.barcode.BarcodeRenderer;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRPrintPage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

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
@Name("printInvoiceReportAction")
@Scope(ScopeType.PAGE)
public class PrintInvoiceReportAction extends GenericReportAction {

    @In
    private User currentUser;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private SaleService saleService;

    @In
    private DosageService dosageService;

    @In
    private UserService userService;

    private Long customerOrderId;
    private CustomerOrder lastCustomerOrder;



    @In
    private WarehouseService warehouseService;

    @In(create = true)
    private PrintInvoiceDataModel printInvoiceDataModel;

    @In
    private RePrintsService rePrintsService;

    @In
    private MovementService movementService;


    //private Dosage dosage;
    private CustomerOrder customerOrder;
    private MoneyUtil moneyUtil;
    private BarcodeRenderer barcodeRenderer;
    private InvoicePrintType invoicePrintType;
    private Boolean imprimirCopia = false;
    private List<Movement> movements = new ArrayList<Movement>();
    private List<CustomerOrder> customerOrders = new ArrayList<CustomerOrder>();
    private List<RePrints> rePrintsNews = new ArrayList<RePrints>();
    private List<Movement> movementsNews = new ArrayList<Movement>();
    private Date date;

    @Restrict("#{s:hasPermission('PRINTINVOICE','VIEW')}")
    public void generateReport() {
        log.debug("Generate PrintInvoiceReportAction......");

        User user = getUser(currentUser.getId());
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId()); /** Solo es impresion, revisar la dosificacion que obtiene??? **/
        this.customerOrderId = saleService.findLastSaleId(user);
        this.lastCustomerOrder = saleService.findSaleById(getCustomerOrderId());

        moneyUtil = new MoneyUtil();
        barcodeRenderer = new BarcodeRenderer();

        TypedReportData typedReportData;
        setReportFormat(ReportFormat.PDF);

        String name = lastCustomerOrder.getClient().getBusinessName();
        String clientNit = lastCustomerOrder.getMovement().getNit();
        Date invoiceDate = lastCustomerOrder.getMovement().getDate();
        Long invoiceNumber = lastCustomerOrder.getMovement().getNumber().longValue();
        String controlCode = lastCustomerOrder.getMovement().getControlCode();
        String qrCode = lastCustomerOrder.getMovement().getQrCode();
        Double totalAmount = lastCustomerOrder.getTotalAmount();
        Double discount = lastCustomerOrder.getCommissionValue();

        String labelType = "ORIGINAL";
        Map params = new HashMap();
        params.putAll(getReportParams(dosage, name, clientNit, invoiceDate, invoiceNumber, totalAmount, discount, controlCode, qrCode, labelType));
        TypedReportData reportData =   addDetailSubReport(params, lastCustomerOrder);

        String labelTypeCopy = "COPIA";
        Map paramsCopy = new HashMap();
        paramsCopy.putAll(getReportParams(dosage, name, clientNit, invoiceDate, invoiceNumber, totalAmount, discount, controlCode, qrCode, labelTypeCopy));
        TypedReportData reportDataCopy =   addDetailSubReport(paramsCopy, lastCustomerOrder);

        for (Object jrPrintPage : reportDataCopy.getJasperPrint().getPages()) {
            reportData.getJasperPrint().addPage((JRPrintPage) jrPrintPage);
        }

        /*for(JRPrintPage page:(List<JRPrintPage>)reportData.getJasperPrint().getPages())
            reportData.getJasperPrint().addPage(page);*/

        try {
            GenerationReportData generationReportData = new GenerationReportData(reportData);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printInvoicesOriginal(List<CustomerOrder> customerOrderList){
        //printInvoices(customerOrderList, "ORIGINAL");


        List<TypedReportData> reportDataList = new ArrayList<TypedReportData>();

        for (CustomerOrder order : customerOrderList){
            reportDataList.add(printInvoice(order, "original"));
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

    public void printInvoicesCopy(List<CustomerOrder> customerOrderList){
        printInvoices(customerOrderList, "COPIA");
    }

    public void printInvoices(List<CustomerOrder> customerOrderList, String labelType){

        log.debug("Generate PrintInvoiceReportAction......");

        User user = getUser(currentUser.getId());
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId()); /** Solo es impresion, revisar la dosificacion que obtiene??? **/

        moneyUtil = new MoneyUtil();
        barcodeRenderer = new BarcodeRenderer();
        setReportFormat(ReportFormat.PDF);

        Map params = new HashMap();
        CustomerOrder customerOrderOne = customerOrderList.get(0);
        Movement movement = customerOrderOne.getMovement();
        String name         = movement.getName();
        String clientNit    = movement.getNit();
        Date invoiceDate    = movement.getDate();
        Long invoiceNumber  = movement.getNumber().longValue();
        String controlCode  = movement.getControlCode();
        String qrCode       = movement.getQrCode();
        Double totalAmount  = movement.getAmount().doubleValue();
        Double discount     = movement.getDiscount().doubleValue();

        params.putAll(getReportParams(dosage, name, clientNit, invoiceDate, invoiceNumber, totalAmount, discount, controlCode, qrCode, labelType));
        TypedReportData reportData =   addDetailSubReport(params, customerOrderOne);

        for (int i = 1 ; i < customerOrderList.size() ; i++){
            CustomerOrder order = customerOrderList.get(i);
            Movement mov = order.getMovement();
            Map paramMap  = new HashMap();

            paramMap.putAll(getReportParams(dosage, mov.getName(), mov.getNit(), mov.getDate(), mov.getNumber().longValue(),
                                            mov.getAmount().doubleValue(), mov.getDiscount().doubleValue(), mov.getControlCode(), mov.getQrCode(), labelType));

            TypedReportData typedReportData =   addDetailSubReport(paramMap, order);
            reportData.getJasperPrint().getPages().addAll(typedReportData.getJasperPrint().getPages());
        }

        try {
            GenerationReportData generationReportData = new GenerationReportData(reportData);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public TypedReportData printInvoice(CustomerOrder order, String labelType){

        User user = getUser(currentUser.getId());
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId()); /** Solo es impresion, revisar la dosificacion que obtiene??? **/

        moneyUtil = new MoneyUtil();
        BarcodeRenderer barcodeRenderer = new BarcodeRenderer();
        setReportFormat(ReportFormat.PDF);

        //Map params = new HashMap();
        Movement movement = order.getMovement();
        String name         = movement.getName();
        String clientNit    = movement.getNit();
        Date invoiceDate    = movement.getDate();
        Long invoiceNumber  = movement.getNumber().longValue();
        String controlCode  = movement.getControlCode();
        String qrCode       = movement.getQrCode();
        Double totalAmount  = movement.getAmount().doubleValue();
        Double discount     = movement.getDiscount().doubleValue();

        //params.putAll(getReportParams(dosage, name, clientNit, invoiceDate, invoiceNumber, totalAmount, discount, controlCode, qrCode, labelType));

        Double subtotal = totalAmount;
        totalAmount = totalAmount - discount;

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("companyNit", dosage.getCompanyNit());
        paramMap.put("invoiceNumber", invoiceNumber);
        paramMap.put("authorizationNumber", dosage.getAuthorizationNumber());
        paramMap.put("clientNit", clientNit);
        paramMap.put("name", name);
        paramMap.put("invoiceDateLiteral", DateUtils.getLiteralDate("Cochabamba", invoiceDate));
        paramMap.put("expirationDate", dosage.getExpirationDate());
        paramMap.put("controlCode", controlCode);
        paramMap.put("companyLabel", dosage.getCompanyLabel());
        paramMap.put("lawLabel", dosage.getLawLabel());
        paramMap.put("labelType", labelType);
        paramMap.put("llaveQR", qrCode);
        paramMap.put("subtotal", subtotal);
        paramMap.put("discount", discount);
        paramMap.put("total", totalAmount);
        paramMap.put("literalTotal", moneyUtil.Convertir(totalAmount.toString(), true, messages.get("Reports.cashAvailable.bs")));

        String filePath = FileCacheLoader.i.getPath("/customers/reports/qr_inv.png");
        barcodeRenderer.generateQR(qrCode, filePath);

        TypedReportData reportData =   addDetailSubReport(paramMap, order);

        return reportData;

    }



    private void createNewReImprint()
    {
        for(RePrints rePrints:rePrintsNews)
        {
             try {
                rePrintsService.create(rePrints);
            } catch (EntryDuplicatedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    private  void createReImprint(CustomerOrder order, Dosage dosage, long numberInvoice, User currentUser) {
        RePrints rePrints = new RePrints();
        rePrints.setNumberReImprent(0);
        rePrints.setState("R");
        rePrints.setDosage(dosage);
        rePrints.setNumberInvoice(numberInvoice);
        rePrints.setCustomerOrder(order);
       /* rePrints.setDateEmission(order.getDateDelicery());
        rePrints.setDateRePrint(new Date());
        rePrints.setGloss("");//verificar
        rePrints.setNit(order.getClientOrder().getNumberDoc());*/
        rePrints.setIdUsrEmission(currentUser.getId());
        rePrints.setIdUsrRePint(currentUser.getId());//verificar
        rePrintsNews.add(rePrints);
       /* try {
            rePrintsService.create(rePrints);
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
    }

    private void updateReImprint(CustomerOrder order) {
       RePrints rePrints =  rePrintsService.findReprintByCustomerOrder(order);
       Integer aux = rePrints.getNumberReImprent();
       aux ++;
       rePrints.setNumberReImprent(aux);
        try {
            rePrintsService.update(rePrints);
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ConcurrencyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void createArticleOrders(CustomerOrder order,Long numberInvoice,String codControl)
    {
        List<ArticleOrder> articleOrders = movementService.findArticleOrdersByCustomerOrder(order);
        for(ArticleOrder articleOrder:articleOrders){
            if(!imprimirCopia)
            {
                createMovement(articleOrder,order,codControl,numberInvoice);
            }
        }
    }

    private void createMovement(ArticleOrder articleOrder,CustomerOrder order,String codControl,Long numberInvoice)
    {
        Movement movement = new Movement();
        movementsNews.add(movement);
    }

    private void createNesmovements()
    {
         for(Movement movement:movementsNews)
         {
              try {
                movementService.create(movement);
              } catch (EntryDuplicatedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
              }
         }
    }

    @Override
    protected String getEjbql() {
        return "";
    }

    @Create
    public void init() {
        restrictions = new String[]{};
        //sortProperty = "productDelivery.id";
    }


    /**
     * get report params
     *
     * @return Map
     */
    private Map<String, Object> getReportParams(Dosage dosage, String name, String clientNit, Date invoiceDate, long invoiceNumber,
                                                Double totalAmount, Double discount, String controlCode, String keyQR, String labelType) {
        String filePath = FileCacheLoader.i.getPath("/customers/reports/qr_inv.png");

        Double subtotal = totalAmount;
        totalAmount = totalAmount - discount;

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("companyNit", dosage.getCompanyNit());
        paramMap.put("invoiceNumber", invoiceNumber);
        paramMap.put("authorizationNumber", dosage.getAuthorizationNumber());
        paramMap.put("clientNit", clientNit);
        paramMap.put("name", name);
        paramMap.put("invoiceDateLiteral", DateUtils.getLiteralDate("Cochabamba", invoiceDate));
        paramMap.put("expirationDate", dosage.getExpirationDate());
        paramMap.put("controlCode", controlCode);
        paramMap.put("companyLabel", dosage.getCompanyLabel());
        paramMap.put("lawLabel", dosage.getLawLabel());
        paramMap.put("labelType", labelType);
        paramMap.put("llaveQR",keyQR);
        paramMap.put("subtotal", subtotal);
        paramMap.put("discount", discount);
        paramMap.put("total", totalAmount);
        paramMap.put("literalTotal", moneyUtil.Convertir(totalAmount.toString(), true, messages.get("Reports.cashAvailable.bs")));
        barcodeRenderer.generateQR(keyQR, filePath);
        return paramMap;
    }

    private ControlCode generateCodControl(CustomerOrder order, Integer numberInvoice, BigDecimal numberAutorization, String key) {
        //Double importeBaseCreditFisical = order.getTotalAmount().doubleValue() * 0.13;
        ControlCode controlCode = null;
        moneyUtil.getLlaveQR(controlCode, key);
        controlCode.generarCodigoQR();
        return controlCode;
    }

    /**
     * add voucher movement detail sub report in main report
     *
     * @param
     */
    private TypedReportData addDetailSubReport(Map<String, Object> params, CustomerOrder order) {
        log.debug("Generating addVoucherMovementDetailSubReport.............................");

         this.customerOrder = order;

        String ejbql = "SELECT " +
                " articleOrder.quantity as quantity, " +
                " articleOrder.productItem.name as name, " +
                " articleOrder.price as price, " +
                " articleOrder.total as total, "+
                " articleOrder.amount as amount, "+
                " articleOrder.customerOrder.description as description "+
                " FROM ArticleOrder articleOrder";

        String[] restrictions = new String[]{"articleOrder.customerOrder.id = #{printInvoiceReportAction.customerOrder.getId()}"};

        String orderBy = "articleOrder.productItem.name";

        //generate the sub report
        String subReportKey = "INVOICEDETAILSUBREPORT";
        return super.getReport(
                subReportKey,
                "/customers/reports/invoiceReceptionReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                "FACTURAS",
                params);

        //add in main report params
        /*mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());*/
    }

    @Factory(value = "invoicePrintTypes", scope = ScopeType.STATELESS)
    public InvoicePrintType[] initProductDeliveryTypes() {
        return InvoicePrintType.values();
    }

    public void search()
    {
        printInvoiceDataModel.search();
        movements = movementService.findMovementByDate(date);
        if(movements.size() > 0)
            imprimirCopia = true;
        else
            imprimirCopia = false;

        //customerOrders = printInvoiceDataModel.getList(0,printInvoiceDataModel.getCount().intValue());
        customerOrders = printInvoiceDataModel.getList(0,printInvoiceDataModel.getNumberOfRows());

    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }

    public InvoicePrintType getInvoicePrintType() {
        return invoicePrintType;
    }

    public void setInvoicePrintType(InvoicePrintType invoicePrintType) {
        this.invoicePrintType = invoicePrintType;
    }

    public Boolean getImprimirCopia() {
        return imprimirCopia;
    }

    public void setImprimirCopia(Boolean imprimirCopia) {
        this.imprimirCopia = imprimirCopia;
    }

    public PrintInvoiceDataModel getPrintInvoiceDataModel() {
        return printInvoiceDataModel;
    }

    public void setPrintInvoiceDataModel(PrintInvoiceDataModel printInvoiceDataModel) {
        this.printInvoiceDataModel = printInvoiceDataModel;
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
