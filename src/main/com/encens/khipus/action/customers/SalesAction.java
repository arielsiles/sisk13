package com.encens.khipus.action.customers;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.billing.BillControllerAction;
import com.encens.khipus.action.billing.SendMessageAction;
import com.encens.khipus.action.customers.reports.PrintBillReportAction;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.rest.SignificantEventCodePOJO;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.*;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.service.finances.UserCashBoxService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.util.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Name("salesAction")
@Scope(ScopeType.CONVERSATION)
public class SalesAction extends GenericAction {

    private ProductItem productItem;
    private String productItemFullName;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal additionalDiscountAmount = BigDecimal.ZERO;

    private Integer invoiceNumberCafc;

    private BigDecimal moneyReceived = BigDecimal.ZERO;
    private BigDecimal moneyReturned = BigDecimal.ZERO;

    private Boolean cashSaleCheck;
    private Boolean creditSaleCheck;
    private Boolean finalConsumer = Boolean.FALSE;
    private Boolean zeroProduct = Boolean.FALSE;

    private CustomerOrderType customerOrderType;
    private SaleTypeEnum saleType;
    private Client client;
    private Distributor distributor;
    private Date orderDate = new Date();
    private String observation;

    private SubsidyEnun subsidyEnun;
    private CustomerCategoryType customerCategoryTypeEnum;

    /** For special billing **/
    private Date billingSpecialDate = new Date();
    private String nameSpecialBill = "";
    private Double amountSpecialBill = 0.0;

    private String connectionStatus;
    private String billingMode;

    private Boolean validNitCi = Boolean.FALSE;
    private Boolean nitCiHasBeenValidated = Boolean.FALSE;

    private String cafcCode;
    private boolean showCAFC = false;

    private String nitValidationMessage;

    private List<SignificantEventCodePOJO> significantEventsCodes; // no usado
    private SignificantEventCodePOJO significantEventSelected; // no usado

    private SignificantEventSIN significantEventSIN;

    //private List<ProductItem> productsSelected = new ArrayList<ProductItem>();
    private List<String> productItemCodesSelected = new ArrayList<String>();

    private List<ArticleOrder> articleOrderList = new ArrayList<ArticleOrder>();

    //private Map<String, BigDecimal> priceItemListMap = new HashMap<String, BigDecimal>();
    private Map<String, BigDecimal> priceItemListMap;

    @In(required = false)
    private User currentUser;
    @In
    private SessionUser sessionUser;
    @In
    private UserService userService;

    @In
    private DosageService dosageService;

    @In
    private MovementService movementService;

    @In
    private ProductItemService productItemService;

    @In
    private SaleService saleService;

    @In
    private CustomerOrderTypeService customerOrderTypeService;

    @In
    private FinancesPkGeneratorService financesPkGeneratorService;

    @In
    private PriceItemService priceItemService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    protected FacesMessages facesMessages;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In
    private CashAccountService cashAccountService;

    @In
    private UserCashBoxService userCashBoxService;

    @In
    private InventoryService inventoryService;

    /*@Create
    public void initialize() {
        System.out.println("------------------> Inicializando..............");
        setOrderDate(new Date());
    }*/

    @In(create = true)
    private BillControllerAction billControllerAction;

    @In(create = true)
    private PrintBillReportAction printBillReportAction;

    @In(create = true)
    private SendMessageAction sendMessageAction;

    @Factory(value = "subsidyEnumList")
    public SubsidyEnun[] getExperienceType() {
        return SubsidyEnun.values();
    }

    @Factory(value = "customerCategoryTypes", scope = ScopeType.STATELESS)
    public CustomerCategoryType[] initProductDeliveryTypes() {
        return CustomerCategoryType.values();
    }

    @Factory(value = "fixedCustomerCategoryTypes", scope = ScopeType.STATELESS)
    public List<CustomerCategoryType> initFixedProductDeliveryTypes() {
        List<CustomerCategoryType> resulTypes = new ArrayList<CustomerCategoryType>(0);
        resulTypes.add(CustomerCategoryType.FACTORY);
        resulTypes.add(CustomerCategoryType.STORE);
        resulTypes.add(CustomerCategoryType.CONSUMER);
        resulTypes.add(CustomerCategoryType.AGENCY_LIST);

        return resulTypes;
    }

    public void openSale(){
        setCustomerOrderType(customerOrderTypeService.findCustomerOrderTypeDefault());

        /*CashBox cashBox = userCashBoxService.findByUser(currentUser);
        System.out.println("------------> cashBox: " + cashBox);
        if (cashBox == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "UserCashBox.error.unassignedCashBox", currentUser.getEmployee().getFullName());
        }*/

    }

    public void addProduct(ProductItem productItem){
        setProductItem(productItem);
        addProduct();
    }

    public Boolean isThereInventory(ProductItem productItem){
        Boolean result = Boolean.TRUE;

        BigDecimal unitaryBalance = inventoryService.findUnitaryBalanceByProductItemAndArticle(productItem.getWarehouse().getId(), productItem.getId());
        if (unitaryBalance.compareTo(BigDecimal.ZERO) <= 0){
            result = Boolean.FALSE;
        }

        return result;
    }

    public BigDecimal getUnitaryBalance(ArticleOrder articleOrder){
        BigDecimal result = inventoryService.findUnitaryBalanceByProductItemAndArticle(articleOrder.getProductItem().getWarehouse().getId(), articleOrder.getProductItem().getId());
        return  result;
    }

    public void addProduct(){

        if (client == null) return;

        /* Uncomment
        if (!isThereInventory(productItem)){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No existe inventario suficiente...");
            return;
        }*/

        if (!this.nitCiHasBeenValidated){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No es posible continuar con la venta, debe validar el NIT/CI.");
            return;
        }

        if (!this.validNitCi){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No es posible continuar con la venta, debe validar el NIT.");
            return;
        }

        if (productItemCodesSelected.contains(productItem.getProductItemCode())){
            clearProduct();
            return;
        }

        if (productItem == null) return;
        if (productItem.getSalePrice() == null) return;

        productItemCodesSelected.add(productItem.getProductItemCode());

        ArticleOrder articleOrder = new ArticleOrder();
        articleOrder.setCodArt(productItem.getProductItemCode());
        articleOrder.setProductItem(productItem);
        articleOrder.setQuantity(0);
        articleOrder.setPromotion(0);
        articleOrder.setReposicion(0);
        articleOrder.setTotal(0);
        articleOrder.setDiscount(0.0);
        articleOrder.setAmount(0.0);
        articleOrder.setCu(BigDecimal.ZERO);
        articleOrder.setUnitCost(BigDecimal.ZERO);
        articleOrder.setPrice(productItem.getSalePrice().doubleValue());
        articleOrder.setUnitaryBalance(inventoryService.findUnitaryBalanceByProductItemAndArticle(productItem.getWarehouse().getId(), productItem.getId()));

        if (getPriceItemListMap() != null){
            BigDecimal price = getPriceItemListMap().get(productItem.getProductItemCode());
            System.out.println("--------------> PRICE: " + price);
            if (price != null)
                articleOrder.setPrice(price.doubleValue());
        }

        articleOrderList.add(articleOrder);

        clearProduct();
    }

    public void addProductItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {

            System.out.println("- - - - - - - - > Productos: " + productItem.getFullName2());

            addProduct(productItem);

            /*if (selectedProductItems.contains(productItem.getId())) {
                continue;
            }

            selectedProductItems.add(productItem.getId());

            PriceItem item = new PriceItem();
            item.setProductItem(productItem);
            item.setProductItemCode(productItem.getProductItemCode());
            item.setCustomerCategory(getInstance());
            item.setCompanyNumber(Constants.COD_COMPANY_DEFAULT);
            getDetails().add(item);
            */
        }
    }

    public void calculateChange(){
        BigDecimal change = BigDecimal.ZERO;
        change = BigDecimalUtil.subtract(moneyReceived, totalAmount);
        setMoneyReturned(change);
    }

    public void removeProduct(ArticleOrder articleOrder){
        articleOrderList.remove(articleOrder);
        //productsSelected.remove(articleOrder.getProductItem());
        productItemCodesSelected.remove(articleOrder.getProductItem().getProductItemCode());
    }

    public void calculateAmount(ArticleOrder articleOrder){

        //if (articleOrder.getQuantity() >  articleOrder.getUnitaryBalance().intValue()){

        /* Uncomment
        if (articleOrder.getQuantity() >  getUnitaryBalance(articleOrder).intValue()){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Inventario Insuficiente...");
            articleOrder.setQuantity(0);
        }*/

        //Double amount = articleOrder.getQuantity() * articleOrder.getPrice();
        BigDecimal amountValue = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(articleOrder.getQuantity()), BigDecimalUtil.toBigDecimal(articleOrder.getPrice()));
        BigDecimal discount    = BigDecimalUtil.multiply(amountValue, BigDecimalUtil.divide(getClient().getProductDiscount(), BigDecimalUtil.ONE_HUNDRED, 4) );

        articleOrder.setDiscount(discount.doubleValue());

        amountValue = BigDecimalUtil.subtract(amountValue, discount);
        articleOrder.setAmount(amountValue.doubleValue());

        calculateTotalUnits(articleOrder);
    }

    public BigDecimal calculateTotalAmount(){
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalAmountResult = BigDecimal.ZERO;
        setZeroProduct(Boolean.FALSE);

        if (getClient() != null) {
            for (ArticleOrder articleOrder : articleOrderList) {
                totalAmount = BigDecimalUtil.sum(totalAmount, BigDecimalUtil.toBigDecimal(articleOrder.getAmount()));
                if (articleOrder.getQuantity() == 0)
                    setZeroProduct(Boolean.TRUE); /** Verifica productos con cantidad CERO **/
            }

            BigDecimal discount = BigDecimalUtil.multiply(totalAmount, BigDecimalUtil.divide(getClient().getAdditionalDiscount(), BigDecimalUtil.ONE_HUNDRED, 4));
            totalAmountResult = BigDecimalUtil.subtract(totalAmount, discount);
            this.additionalDiscountAmount = discount;
        }
        setTotalAmount(totalAmountResult);
        return totalAmountResult;
    }

    public void calculateTotalUnits(ArticleOrder articleOrder){
        System.out.println("--------------> Articulo: " + articleOrder.getProductItem().getFullName() + " C: " + articleOrder.getQuantity() + " P: " + articleOrder.getPromotion() + " R: " + articleOrder.getReposicion());
        articleOrder.setTotal(articleOrder.getQuantity() + articleOrder.getPromotion() + articleOrder.getReposicion());
        System.out.println("--------------> Total: " + articleOrder.getTotal());
    }

    public void clearProduct(){
        setProductItem(null);
        setProductItemFullName(null);
    }

    public void clearProductsSelected(){
        //setProductsSelected(new ArrayList<ProductItem>());
        setProductItemCodesSelected(new ArrayList<String>());
        setArticleOrderList(new ArrayList<ArticleOrder>());
    }

    public void clearTotalAmount(){
        setTotalAmount(BigDecimal.ZERO);
    }

    public void clearAll(){
        clearProduct();
        clearClient();
        clearProductsSelected();
        clearTotalAmount();
        setObservation(null);
        setDistributor(null);
        setSubsidyEnun(null);
        setCustomerCategoryTypeEnum(null);
        setFinalConsumer(Boolean.FALSE);
        setMoneyReturned(BigDecimal.ZERO);

        this.nitCiHasBeenValidated = Boolean.FALSE;
        this.validNitCi = Boolean.FALSE;
        setInvoiceNumberCafc(null);
        setAdditionalDiscountAmount(BigDecimal.ZERO);

        setNitValidationMessage(null);
    }

    public void clearSpecialBilling(){
        setAmountSpecialBill(0.0);
        setNameSpecialBill("");
    }

    public void checkMinimumValues(){
        if (client == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Seleccionar un cliente !");
            return;
        }

        if (articleOrderList.isEmpty()){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Seleccionar al menos un producto !");
            return;
        }

        if (zeroProduct){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Revisar productos con cantidad CERO !");
            return;
        }
    }

    /** Venta a credito **/
    public void registerSale(){
        System.out.println("------------> Registrando venta Total: " + getTotalAmount());
        System.out.println("------------> Description: " + getObservation());
        System.out.println("------------> Fecha: " + DateUtils.format(getOrderDate(), "dd/MM/yyyy"));

        checkMinimumValues();

        CustomerOrder customerOrder = createSale();

        inventoryService.updateInventoryForSales(customerOrder);

        clearAll();
        assignCustomerOrderTypeDefault();
    }

    /** Venta a credito y factura y asiento **/
    public void registerSaleAndInvoice(){
        checkMinimumValues();

        CustomerOrder customerOrder = createSale();

        Movement movement = createInvoice(customerOrder);
        customerOrder.setMovement(movement);
        saleService.updateCustomerOrder(customerOrder);
        inventoryService.updateInventoryForSales(customerOrder);

        generateInvoiceOnline(customerOrder);

        if ( customerOrder.getTotalAmount() > 0)
            generateFileXML(customerOrder);

        clearAll();
        assignCustomerOrderTypeDefault();
    }


    public void registerCashSale() throws IOException {
        System.out.println("......Registrando Venta al Contado...");
        checkMinimumValues();

        if (this.client == null || totalAmount.compareTo(BigDecimal.ZERO) == 0){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No se puede realizar la venta, monto incorrecto.");
            return;
        }

        CustomerOrder customerOrder = createSale();
        System.out.println("======> customerOrder???? " + customerOrder);
        if (customerOrder!= null) {
            Movement movement = createInvoice(customerOrder);
            customerOrder.setMovement(movement);
            Voucher voucher = accountingCashSale(customerOrder, movement);
            customerOrder.setVoucher(voucher);
            customerOrder.setAccounted(Boolean.TRUE);
            saleService.updateCustomerOrder(customerOrder);

            inventoryService.updateInventoryForSales(customerOrder);

            generateInvoiceOnline(customerOrder);

            if ( customerOrder.getTotalAmount() > 0)
                generateFileXML(customerOrder);

            clearAll();
            assignCustomerOrderTypeDefault();
        }
    }

    public void generateInvoiceOnline(CustomerOrder customerOrder){
        try {
            System.out.println("--->>> !hasInvoice ???" +  !billControllerAction.hasInvoice(customerOrder));
            if (!billControllerAction.hasInvoice(customerOrder)){
                /** todo Verifica que no se pueda emitir la factura con monto CERO o menor **/
                if ( customerOrder.getTotalAmount() > 0)
                    billControllerAction.createBill(customerOrder);
            }
        } catch (IOException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Error en facturacion...");
        }
    }

    public void generateFileXML(CustomerOrder customerOrder) {

        if (customerOrder.getMovement() != null) {
            try {
                String fileName = Constants.PREFIX_NAME_INVOICE + customerOrder.getMovement().getNumber() + ".xml";
                String pathFileName = Constants.PATH_FILE_INVOICE + fileName;

                /** decode the encoded data **/
                String input = customerOrder.getMovement().getFactura();
                Base64.Decoder decoder = Base64.getDecoder();
                String decoded = new String(decoder.decode(input), "UTF-8");

                /** todo Verificar con Javier **/
                String newDecoded = decoded.replace("Ley N?", "Ley N°");

                //System.out.println("Decoded Data: " + decoded);

                FileWriter archivo = null;
                PrintWriter escritor = null;

                //Writer out = null;

                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileName), "UTF-8"));
                out.write(newDecoded);
                out.close();

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            } /*finally {
                out.close();
            }*/
        }
    }

    public void registerCashSaleNoInvoice(){
        System.out.println("......Registrando Venta al Contado SF...");
        CustomerOrder customerOrder = createSale();
        Voucher voucher = accountingCashSaleNoInvoice(customerOrder);
        customerOrder.setVoucher(voucher);
        customerOrder.setAccounted(Boolean.TRUE);
        saleService.updateCustomerOrder(customerOrder);

        inventoryService.updateInventoryForSales(customerOrder);

        clearAll();
        assignCustomerOrderTypeDefault();
    }

    public CustomerOrder createSale(){

        System.out.println("------------> user: " + currentUser);
        System.out.println("------------> tipo pedido: " + customerOrderType);
        System.out.println("------------> Subsidio: " + this.subsidyEnun);

        /** Verifica existencia de inventario antes de registrar la venta **/
        /* Uncomment
        for (ArticleOrder articleOrder : articleOrderList){
            if (!isThereInventory(articleOrder.getProductItem())){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Inventario Insuficiente...");
                return null;
            }
        }*/

        observation = (observation == null) ? "" : observation;
        CustomerOrder customerOrder = new CustomerOrder();

        System.out.println("--------------------> Invoice Number CAFC: " + this.invoiceNumberCafc);
        if (this.invoiceNumberCafc != null)
            customerOrder.setInvoiceNumberCafc(this.invoiceNumberCafc.toString());

        Long saleCode = new Long(financesPkGeneratorService.getNextNoTransByDocumentType(saleType.getSequenceName()));
        customerOrder.setCode(saleCode);
        customerOrder.setUser(currentUser);
        customerOrder.setOrderDate(orderDate);
        customerOrder.setObservation(observation);
        customerOrder.setSaleType(saleType);
        customerOrder.setCustomerOrderType(customerOrderType);
        customerOrder.setClient(client);
        customerOrder.setDistributor(distributor);
        customerOrder.setState(SaleStatus.PENDIENTE);

        if (customerOrder.getSaleType().equals(SaleTypeEnum.CASH))
            customerOrder.setState(SaleStatus.CONTABILIZADO);

        customerOrder.setTotalAmount(totalAmount.doubleValue());
        customerOrder.setAdditionalDiscountValue(this.additionalDiscountAmount);

        customerOrder.setCommissionPercentage(0.0);
        customerOrder.setCommissionValue(0.0);
        //BigDecimal tax = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), Constants.VAT);
        //customerOrder.setTax(tax.doubleValue());

        /** For Product Discount **/
        System.out.println("----------------> client.getProductDiscount(): " + client.getProductDiscount().doubleValue());
        if (client.getProductDiscount().doubleValue() > 0){
            BigDecimal sumDiscount = BigDecimal.ZERO;

            for (ArticleOrder articleOrder : articleOrderList){
                sumDiscount = BigDecimalUtil.sum(sumDiscount, BigDecimalUtil.toBigDecimal(articleOrder.getDiscount()));
                System.out.println("........sumDiscount: " + sumDiscount);
            }
            customerOrder.setProductDiscountValue(sumDiscount);
        }

        System.out.println("==============> 1.TOTAL AMOUNT: " + customerOrder.getTotalAmount());
        System.out.println("==============> 1.PRODUCT DISCOUNT: " + customerOrder.getProductDiscountValue());

        /** For Additional Discount
         *  Se calcula el descuento al registrar los productos **/
        /*if (client.getAdditionalDiscount().doubleValue() > 0){
            BigDecimal percentage = BigDecimalUtil.divide(client.getAdditionalDiscount(), BigDecimalUtil.ONE_HUNDRED, 4);
            BigDecimal discountValue = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), percentage);
            customerOrder.setAdditionalDiscountValue(discountValue);
            customerOrder.setTotalAmount(BigDecimalUtil.subtract(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), discountValue).doubleValue());
        }*/
        System.out.println("==============> 2.TOTAL AMOUNT: " + customerOrder.getTotalAmount());

        customerOrder.setTax( BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), Constants.VAT).doubleValue() );

        if (subsidyEnun != null)
            customerOrder.setDescription(subsidyEnun.getSubsidyType());

        customerOrder.setArticleOrderList(articleOrderList);

        if (customerOrder.getDistributor() != null)
            customerOrder.setDealerAmount(calculateAmountForDealer(customerOrder));

        if (customerOrder.getDistributor() != null) {
            String amountDealer = FormatUtils.formatNumber(customerOrder.getDealerAmount(), MessageUtils.getMessage("patterns.decimalNumber"), sessionUser.getLocale());
            observation = observation + " Distribuidor: " + customerOrder.getDistributor().getFullName() + " - " + amountDealer;
            customerOrder.setObservation(observation);
        }

        if (customerOrder.getTotalAmount() > 0){
            String outcome = saleService.createSale(customerOrder);
        }else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Monto total incorrecto para facturar, revise el % descuento.");
            customerOrder = null;
        }

        return customerOrder;
    }

    public Movement createInvoice(CustomerOrder customerOrder){
        User user = getUser(currentUser.getId()); //
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId());

        Long invoiceNumber = dosage.getCurrentNumber();
        SFC sfc = new SFC(  dosage.getAuthorizationNumber().toString(),
                            invoiceNumber,
                            customerOrder.getClient().getNitNumber(),
                            customerOrder.getClient().getBusinessName(),
                            customerOrder.getOrderDate(),
                            customerOrder.getTotalAmount(),
                            dosage.getKey());
        /** Gen. Codigo de Control **/
        //String controlCode = sfc.generateControlCode(); Uncomment
        String controlCode = "0"; /** todo **/

        String nitCiValue = sfc.getClientNit();
        if (customerOrder.getClient().getComplement() != null)
            nitCiValue = nitCiValue + "-" + customerOrder.getClient().getComplement();

        Movement movement = new Movement();
        movement.setDate(sfc.getDate());
        movement.setState(Constants.MOVEMENT_STATE_V);

        if (customerOrder.getInvoiceNumberCafc() != null) {
            movement.setNumber(new Integer(customerOrder.getInvoiceNumberCafc()));
        }else{
            movement.setNumber(dosage.getCurrentNumber().intValue());
        }

        movement.setNit(nitCiValue);
        movement.setName(sfc.getName());
        movement.setAmount(BigDecimalUtil.toBigDecimal(sfc.getAmount()));
        movement.setAmountIce(BigDecimal.ZERO);
        movement.setExemptExport(BigDecimal.ZERO);
        movement.setTaxedSalesZero(BigDecimal.ZERO);
        movement.setSubtotal(BigDecimalUtil.toBigDecimal(sfc.getAmount())); /** todo **/
        //movement.setDiscount(BigDecimalUtil.toBigDecimal(customerOrder.getCommmissionValue()));
        movement.setDiscount(BigDecimalUtil.sum(customerOrder.getProductDiscountValue(), customerOrder.getAdditionalDiscountValue()));

        BigDecimal amountFiscalDebit = BigDecimalUtil.subtract(movement.getAmount(), movement.getDiscount());
        movement.setAmountFiscalDebit(amountFiscalDebit);
        movement.setFiscalDebit(BigDecimalUtil.toBigDecimal(customerOrder.getTax()));
        movement.setControlCode(controlCode);
        movement.setRegistrationDate(new Date());


        String discount = movement.getDiscount().doubleValue() > 0 ? movement.getDiscount().toString() : "0";
        String qrCode = dosage.getCompanyNit() + "|" +
                        movement.getNumber() + "|" +
                        dosage.getAuthorizationNumber() + "|" +
                        DateUtils.format(movement.getDate(), "dd/MM/yyyy") + "|" +
                        movement.getSubtotal() + "|" +
                        movement.getAmountFiscalDebit() + "|" +
                        movement.getControlCode() + "|" +
                        movement.getNit() + "|" +
                        "0|0|0|"  +
                        discount;

        movement.setQrCode(qrCode);
        movement.setAuthorizationNumber(dosage.getAuthorizationNumber().toString());
        /*movement.setCustomerOrder(customerOrder);*/

        if (customerOrder.getInvoiceNumberCafc() == null)
            dosageService.increaseInvoiceNumber(dosage);

        movementService.createMovement(movement);

        return movement;
    }

    private Voucher accountingCreditSale(CustomerOrder customerOrder, Movement movement){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        CashBox cashBox = userCashBoxService.findByUser(currentUser);

        Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                MessageUtils.getMessage("Voucher.creditSale.gloss") + " " +
                        customerOrder.getCode() + " (F-" + movement.getNumber() + ") " + customerOrder.getClient().getFullName());

        CashAccount debitAccount = cashBox.getType().getCashAccountReceivable();
        Client client = customerOrder.getClient();
        System.out.println("----------ZZZZz>>> Tipo Pedido: " + customerOrder.getCustomerOrderType().getType());
        if (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.SPECIAL)) {
            System.out.println("----->>> TRUE");
            debitAccount = cashBox.getType().getCashAccountIncome();
            client = null;
        }

        VoucherDetail debitClientAccount = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                debitAccount, BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()),
                FinancesCurrencyType.D, BigDecimal.ONE);

        debitClientAccount.setClient(client);

        VoucherDetail debitTransactionTax = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                companyConfiguration.getTransactionTaxExpense(),
                BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), Constants.IT_RETENTION_B),
                FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditTransactionTax = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getTransactionTaxPayable(),
                debitTransactionTax.getDebit(), FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditFiscalDebitIVA = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getFiscalDebitLiability(),
                BigDecimalUtil.toBigDecimal(customerOrder.getTax()), FinancesCurrencyType.D, BigDecimal.ONE);

        BigDecimal amount = BigDecimalUtil.sum(debitClientAccount.getDebit(), debitTransactionTax.getDebit());
        amount = BigDecimalUtil.subtract(amount, creditTransactionTax.getCredit(), creditFiscalDebitIVA.getCredit());

        VoucherDetail creditPrimarySaleProduct = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                //companyConfiguration.getPrimarySaleProduct(), amount, FinancesCurrencyType.D, BigDecimal.ONE);
                cashBox.getType().getCashAccountIncome(), amount, FinancesCurrencyType.D, BigDecimal.ONE);



        creditFiscalDebitIVA.setMovement(movement);
        voucher.setDocumentType(Constants.NE_VOUCHER_DOCTYPE);
        voucher.setMovement(movement);
        voucher.setInvoiceNumber(movement.getNumber());

        voucher.getDetails().add(debitClientAccount);
        voucher.getDetails().add(debitTransactionTax);
        voucher.getDetails().add(creditTransactionTax);
        voucher.getDetails().add(creditFiscalDebitIVA);
        voucher.getDetails().add(creditPrimarySaleProduct);

        voucherAccoutingService.saveVoucher(voucher);

        return voucher;
    }

    private Voucher accountingCashSale(CustomerOrder customerOrder, Movement movement){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        CashBox cashBox = userCashBoxService.findByUser(currentUser);

        Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                                                            MessageUtils.getMessage("Voucher.cashSale.gloss") + " " +
                                                                  customerOrder.getCode() + " (F-" + movement.getNumber() + ") " + customerOrder.getClient().getFullName());

        VoucherDetail debitGeneralBox = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                cashBox.getType().getCashAccountBox(), BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()),
                FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail debitTransactionTax = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                companyConfiguration.getTransactionTaxExpense(),
                BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), Constants.IT_RETENTION_B),
                FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditTransactionTax = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getTransactionTaxPayable(),
                debitTransactionTax.getDebit(), FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditFiscalDebitIVA = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getFiscalDebitLiability(),
                BigDecimalUtil.toBigDecimal(customerOrder.getTax()), FinancesCurrencyType.D, BigDecimal.ONE);

        BigDecimal amount = BigDecimalUtil.sum(debitGeneralBox.getDebit(), debitTransactionTax.getDebit());
        amount = BigDecimalUtil.subtract(amount, creditTransactionTax.getCredit(), creditFiscalDebitIVA.getCredit());

        VoucherDetail creditPrimarySaleProduct = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                //companyConfiguration.getPrimarySaleProduct(), amount, FinancesCurrencyType.D, BigDecimal.ONE);
                cashBox.getType().getCashAccountIncome(), amount, FinancesCurrencyType.D, BigDecimal.ONE);

        creditFiscalDebitIVA.setMovement(movement);
        voucher.setDocumentType(Constants.CI_VOUCHER_DOCTYPE);
        voucher.setMovement(movement);
        voucher.setInvoiceNumber(movement.getNumber());

        voucher.getDetails().add(debitGeneralBox);
        voucher.getDetails().add(debitTransactionTax);
        voucher.getDetails().add(creditTransactionTax);
        voucher.getDetails().add(creditFiscalDebitIVA);
        voucher.getDetails().add(creditPrimarySaleProduct);

        voucherAccoutingService.saveVoucher(voucher);

        return voucher;
    }

    private Voucher accountingCashSaleNoInvoice(CustomerOrder customerOrder){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        CashBox cashBox = userCashBoxService.findByUser(currentUser);

        Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                MessageUtils.getMessage("Voucher.cashSale.gloss") + " " + customerOrder.getCode() + " " + customerOrder.getClient().getFullName());

        VoucherDetail debitGeneralBox = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                cashBox.getType().getCashAccountBox(), BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()),
                FinancesCurrencyType.D, BigDecimal.ONE);


        /** 2570110000 - Prevision para contingencias **/
        VoucherDetail creditContingency = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                cashBox.getType().getCashAccountContingency() , debitGeneralBox.getDebit(), FinancesCurrencyType.D, BigDecimal.ONE);

        voucher.setDocumentType(Constants.CI_VOUCHER_DOCTYPE);
        voucher.getDetails().add(debitGeneralBox);
        voucher.getDetails().add(creditContingency);

        voucherAccoutingService.saveVoucher(voucher);

        return voucher;
    }

    public BigDecimal calculateAmountForDealer(CustomerOrder customerOrder){

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        BigDecimal dealerParameter = companyConfiguration.getDealerParameter();

        Map<String, BigDecimal> priceMap = priceItemService.getPricesByCategory(CustomerCategoryType.FACTORY);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ArticleOrder article:customerOrder.getArticleOrderList()){
            BigDecimal factoryPrice = priceMap.get(article.getCodArt());
            BigDecimal difference = BigDecimalUtil.subtract(BigDecimalUtil.toBigDecimal(article.getPrice()), factoryPrice);
            BigDecimal tax = BigDecimalUtil.multiply(difference, dealerParameter);
            BigDecimal dealerPrice = BigDecimalUtil.sum(factoryPrice, tax);
            BigDecimal amount = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(article.getQuantity()), dealerPrice);
            System.out.println("============> C:" + article.getQuantity() + " F:" + factoryPrice + " D:" + dealerPrice + " Dif:" + difference + " Tax:" + tax + " A:" + amount );
            totalAmount = BigDecimalUtil.sum(totalAmount, amount);
        }
        return totalAmount;
    }

    public void processBilling(List<CustomerOrder> customerOrderList){

        for (CustomerOrder customerOrder : customerOrderList){

            if (!customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.SPECIAL)) {

                /** Si: no tiene factura, Esta pendiente, Es Credito **/
                if (customerOrder.getMovement() == null && customerOrder.getState().equals(SaleStatus.PENDIENTE) && customerOrder.getSaleType().equals(SaleTypeEnum.CREDIT) &&
                        (!customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REFRESHMENT) && // F
                                !customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REPLACEMENT) && // T     F
                                !customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.TASTING))) {   // T

                    System.out.println("==========> Venta seleccionada: " + customerOrder.getClient().getFullName() + " - Venta Nro: " + customerOrder.getCode() + " - Monto Bs: " + customerOrder.getTotalAmount());

                    Movement movement = createInvoice(customerOrder);
                    customerOrder.setMovement(movement);
                    saleService.updateCustomerOrder(customerOrder);
                    System.out.println("==========> FACT: " + movement.getNumber());

                }
            }
        }
    }

    public void processBillingSpecial(List<CustomerOrder> customerOrderList){

        HashMap<String, Integer> products = new HashMap<String, Integer>();
        HashMap<String, ArticleOrder> productsArt = new HashMap<String, ArticleOrder>();

        CustomerOrder customerOrderBill = new CustomerOrder();
        List<ArticleOrder> articleOrderList = new ArrayList<ArticleOrder>();
        Double totalAmount = 0.0;
        //String glossCodes = "";

        for (CustomerOrder customerOrder : customerOrderList){
            if (customerOrder.getState().equals(SaleStatus.CONTABILIZADO)){
                System.out.println("==========> Venta seleccionada: " + customerOrder.getClient().getFullName() + " - Venta Nro: " + customerOrder.getCode() + " - Monto Bs: " + customerOrder.getTotalAmount());

                for (ArticleOrder articleOrder : customerOrder.getArticleOrderList()){
                    String articleOrderCode = articleOrder.getCodArt();

                    System.out.println("...Pedido: " + customerOrder.getCode() + " - " + articleOrder.getProductItem().getFullName() + " - " + articleOrder.getQuantity() + " - " + articleOrder.getAmount());

                    if (productsArt.containsKey(articleOrderCode)){

                        System.out.println("==> add: " + products.get(articleOrderCode) + " ... " + articleOrder.getQuantity());
                        Integer quantity = products.get(articleOrderCode);
                        quantity = quantity + articleOrder.getQuantity();
                        products.put(articleOrderCode, quantity);

                        ArticleOrder articleOrd = productsArt.get(articleOrderCode);
                        Integer quantityOrd     = articleOrd.getQuantity() + articleOrder.getQuantity();
                        articleOrd.setQuantity(quantityOrd);
                        Double amount = articleOrd.getAmount() + articleOrder.getAmount();
                        articleOrd.setAmount(amount);
                        Double price = articleOrd.getAmount() / articleOrd.getQuantity();
                        articleOrd.setPrice(price);
                        productsArt.put(articleOrderCode, articleOrd);

                    }else {
                        products.put(articleOrderCode, articleOrder.getQuantity());

                        ArticleOrder article = new ArticleOrder();
                        article.setQuantity(articleOrder.getQuantity());
                        article.setPrice(articleOrder.getPrice());
                        article.setProductItem(articleOrder.getProductItem());
                        article.setCodArt(articleOrderCode);
                        article.setCompanyNumber(Constants.defaultCompanyNumber);
                        article.setPromotion(0);
                        article.setReposicion(0);
                        article.setTotal(articleOrder.getQuantity());
                        article.setAmount(articleOrder.getAmount());

                        productsArt.put(articleOrderCode, article);
                        articleOrderList.add(article);

                    }
                }
            }
        }


        if (productsArt.size() > 0){

            for (String key : products.keySet()) {
                System.out.println(">>>>>>> Producto: " + key + " - Cant. " + products.get(key));
            }

            Long saleCode = new Long(financesPkGeneratorService.getNextNoTransByDocumentType(SaleTypeEnum.CREDIT.getSequenceName()));
            customerOrderBill.setCode(saleCode);
            customerOrderBill.setSaleType(SaleTypeEnum.CREDIT);
            customerOrderBill.setCustomerOrderType(customerOrderTypeService.findCustomerOrderTypeSpecial());
            customerOrderBill.setClient(customerOrderList.get(0).getClient());
            customerOrderBill.setArticleOrderList(articleOrderList);
            customerOrderBill.setOrderDate(billingSpecialDate);

            System.out.println("---------PARA FACTURACION----------" + billingSpecialDate);
            for (ArticleOrder articleOrder : customerOrderBill.getArticleOrderList()) {
                System.out.println("---> " + articleOrder.getCodArt() + " - " + articleOrder.getQuantity() + " - " + articleOrder.getPrice() + " - " + articleOrder.getAmount());
                totalAmount = totalAmount + articleOrder.getAmount();
            }

            customerOrderBill.setTotalAmount(totalAmount);
            customerOrderBill.setTax(BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(totalAmount), Constants.VAT).doubleValue());
            customerOrderBill.setState(SaleStatus.ANULADO);
            customerOrderBill.setUser(currentUser);
            /*saleService.createSale(customerOrderBill);*/

            Movement movement = createInvoice(customerOrderBill);
            customerOrderBill.setMovement(movement);

            for(CustomerOrder customerOrder : customerOrderList){
                customerOrder.setMovement(movement);
                saleService.updateCustomerOrder(customerOrder);
            }

            // Para contabilizar el pedido para facturar fin de mes
            Voucher voucher = accountingCreditSale(customerOrderBill, movement);
            voucher.setGloss(MessageUtils.getMessage("Voucher.creditSale.gloss") + " " + " (F-" + movement.getNumber() + ") " + customerOrderBill.getClient().getFullName());
            voucherAccoutingService.simpleUpdateVoucher(voucher);

            /*customerOrderBill.setVoucher(voucher);
            customerOrderBill.setAccounted(Boolean.TRUE);
            saleService.updateCustomerOrder(customerOrderBill);*/

        }
        clearSpecialBilling();
    }

    public void printInvoices(List<CustomerOrder> customerOrderList){


        for (CustomerOrder customerOrder : customerOrderList){

            System.out.println("==========> Venta seleccionada: " + customerOrder.getClient().getFullName() + " - Venta Nro: " + customerOrder.getCode() + " - Monto Bs: " + customerOrder.getTotalAmount());

        }



    }

    public boolean showActionsBilling(CustomerOrder customerOrder){
        boolean result = true;

        if (customerOrder.getState().equals(SaleStatus.ANULADO))
            result = false;
        if (customerOrder.getMovement() != null){
            if (customerOrder.getMovement().getDescri() != null)
                if (customerOrder.getMovement().getDescri().equals("RECHAZADA"))
                    result = false;
        }
        return result;
    }

    public boolean isAnnulled(CustomerOrder customerOrder){
        return customerOrder.getState().equals(SaleStatus.ANULADO);
    }

    public boolean isRejected(CustomerOrder customerOrder){
        boolean result = false;
        if (customerOrder.getMovement() != null){
            if (customerOrder.getMovement().getDescri() != null)
                result = customerOrder.getMovement().getDescri().equals("RECHAZADA");
        }

        return result;
    }

    public void assignClient(Client client){
        setClient(client);
        assignCustomerOrderTypeDefault();
        setCustomerCategoryTypeEnum(null);
        setPriceItemListMap(null);
        setNitValidationMessage(null);

        if (client.getCustomerCategory() != null)
            setPriceItemListMap(priceItemService.getPriceItemsMap(client.getCustomerCategory()));
    }

    public void loadConsumerPrices(){
        System.out.println("...............Cargando precios de consumidor...");
        setPriceItemListMap(priceItemService.getConsumerPrices());
        System.out.println("............... ejm: leche uht (118): " + getPriceItemListMap().get("118"));

    }

    public void loadPricesByCategory(){
        setPriceItemListMap(priceItemService.getPricesByCategory(this.customerCategoryTypeEnum));
    }

    public void assignCustomerOrderTypeDefault(){
        System.out.println("--------> findCustomerOrderTypeDefault: " + customerOrderTypeService.findCustomerOrderTypeDefault());
        setCustomerOrderType(customerOrderTypeService.findCustomerOrderTypeDefault());
    }

    public void initCreditSale(){
        setSaleType(SaleTypeEnum.CREDIT);
        calculateTotalAmount();
        System.out.println("====>Credit Sale Fecha: " + this.orderDate);
    }

    public void initSpecialBill(List<CustomerOrder> customerOrderList){
        if (customerOrderList.size() > 0){
            setNameSpecialBill(customerOrderList.get(0).getClient().getBusinessName());
        }

        Double amount = 0.0;
        for (CustomerOrder customerOrder : customerOrderList){
            if (!customerOrder.getState().equals(SaleStatus.ANULADO))
                amount = amount + customerOrder.getTotalAmount();
        }
        setAmountSpecialBill(amount);
    }

    public void initCashSale(){
        System.out.println("====>Cash Sale Fecha: " + this.orderDate);
        setSaleType(SaleTypeEnum.CASH);
        setMoneyReceived(calculateTotalAmount());
    }

    public List<ProductItem> getBestProductList(){
        return productItemService.findBestProductList();
    }

    public List<ProductItem> getBestProductsByCategory(){

        List<ProductItem> resultProductItems = new ArrayList<ProductItem>(0);

        CashBox cashBox = userCashBoxService.findByUser(currentUser);

        if (cashBox.getType().getCustomerCategory() != null){
            List<PriceItem> priceItemList = cashBox.getType().getCustomerCategory().getPriceItemList();
            for (PriceItem priceItem : priceItemList){
                resultProductItems.add(priceItem.getProductItem());
            }
        }
        return resultProductItems;
    }

    public boolean showInputCAFC(){
        System.out.println("********************>>>> >>> >>>" + getSignificantEventSIN());

        boolean result = false;
        if (getSignificantEventSIN() != null) { /** todo codes **/
            if (    getSignificantEventSIN().getCode() == 5 ||
                    getSignificantEventSIN().getCode() == 6 ||
                    getSignificantEventSIN().getCode() == 7) {
                result = true;
            } else
                result = false;
        }
        return result;
    }

    public String cancelBillingMode(){
        return Outcome.CANCEL;
    }

    public String changeToOfflineBillingMode() throws IOException {

        String cafc = null;
        if (showInputCAFC())
            cafc = getCafcCode();

        billControllerAction.changeToOfflineBillingMode(this.significantEventSIN, cafc);
        return Outcome.SUCCESS;
    }

    public String changeToOnlineBillingMode() throws IOException {
        billControllerAction.changeToOnlineBillingMode();
        return Outcome.SUCCESS;
    }

    public String prepareOfflineBillPackages() throws IOException {
        billControllerAction.prepareOfflineBillPackages();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"PREPARANDO: FACTURAS OFFLINE!!!");
        return Outcome.SUCCESS;
    }

    public String processOfflineBillPackages() throws IOException {
        billControllerAction.processOfflineBillPackages();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"PROCESANDO: FACTURAS OFFLINE!!!");
        return Outcome.SUCCESS;
    }

    public String validateOfflineBillPackages() throws IOException {
        billControllerAction.validateOfflineBillPackages();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"VALIDANDO: FACTURAS OFFLINE!!!");
        return Outcome.SUCCESS;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public List<ProductItem> getProductItemList(){
        return productItemService.findProductItemList();
    }

    public String getProductItemFullName() {
        return productItemFullName;
    }

    public void setProductItemFullName(String productItemFullName) {
        this.productItemFullName = productItemFullName;
    }

    /*public List<ProductItem> getProductsSelected() {
        return productsSelected;
    }

    public void setProductsSelected(List<ProductItem> productsSelected) {
        this.productsSelected = productsSelected;
    }*/

    public List<ArticleOrder> getArticleOrderList() {
        return articleOrderList;
    }

    public void setArticleOrderList(List<ArticleOrder> articleOrderList) {
        this.articleOrderList = articleOrderList;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getMoneyReceived() {
        return moneyReceived;
    }

    public void setMoneyReceived(BigDecimal moneyReceived) {
        this.moneyReceived = moneyReceived;
    }

    public BigDecimal getMoneyReturned() {
        return moneyReturned;
    }

    public void setMoneyReturned(BigDecimal moneyReturned) {
        this.moneyReturned = moneyReturned;
    }

    public Boolean getCashSaleCheck() {
        return cashSaleCheck;
    }

    public void setCashSaleCheck(Boolean cashSaleCheck) {
        this.cashSaleCheck = cashSaleCheck;
    }

    public Boolean getCreditSaleCheck() {
        return creditSaleCheck;
    }

    public void setCreditSaleCheck(Boolean creditSaleCheck) {
        this.creditSaleCheck = creditSaleCheck;
    }

    /*public CustomerOrderTypeEnum getCustomerOrderTypeEnum() {
        return customerOrderTypeEnum;
    }

    public void setCustomerOrderTypeEnum(CustomerOrderTypeEnum customerOrderTypeEnum) {
        this.customerOrderTypeEnum = customerOrderTypeEnum;
    }*/

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void clearClient(){
        setClient(null);
        this.setPriceItemListMap(null);

        clearProduct();
        clearProductsSelected();
        clearTotalAmount();
        setObservation(null);
        setDistributor(null);
        setSubsidyEnun(null);
        setCustomerCategoryTypeEnum(null);
        setNitValidationMessage(null);

        this.nitCiHasBeenValidated = Boolean.FALSE;
        this.validNitCi = Boolean.FALSE;
        setInvoiceNumberCafc(null);
        setAdditionalDiscountAmount(BigDecimal.ZERO);

        assignCustomerOrderTypeDefault();
    }

    public Boolean connectionTest() throws IOException {
        Boolean result = Boolean.FALSE;

        if (billControllerAction.connectionTest()){
            connectionStatus = "Conexion OK";
            result = Boolean.TRUE;
        } else {
            connectionStatus = "Sin conexion";
        }

        System.out.println("-----> Test Conexion: " + connectionStatus);

        return result;
    }

    public void validateNitCi(){
        String result = "";

        boolean connectionTest = billControllerAction.connectionTest();
        if (connectionTest){
            try {
                DocumentType docType = getClient().getInvoiceDocumentType();
                boolean isOnlineMode =  billControllerAction.checkBillingMode();

                if (docType.getSinCode() == 5 && isOnlineMode) {
                    result = billControllerAction.nitVerification(new Long(getClient().getNitNumber()));
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> RESULT NIT: " + result);
                    setNitValidationMessage(result);
                    this.nitCiHasBeenValidated = Boolean.TRUE;

                    if (result.equals("NIT INEXISTENTE")) {
                        if (getClient().getNitNumber().equals("99001") || getClient().getNitNumber().equals("99002") || getClient().getNitNumber().equals("99003")) {
                            validNitCi = Boolean.TRUE;
                        } else
                            validNitCi = Boolean.TRUE; /** FALSE Para controlar que no continue la venta en caso de un NIT inexistente **/
                    }
                    if (result.equals("NIT ACTIVO") || result.equals("NIT INACTIVO")) {
                        validNitCi = Boolean.TRUE;
                    }
                }else {
                    result = "CI/CEX/PAS/OD";
                    if (!isOnlineMode)
                        result = "Fuera de línea";

                    setNitValidationMessage(result);
                    validNitCi = Boolean.TRUE;
                    nitCiHasBeenValidated = Boolean.TRUE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                setNitValidationMessage("¡No se pudo validar!");
            }
        }else {

            Boolean isOnlineMode =  billControllerAction.checkBillingMode();
            if (isOnlineMode != null){ // ebilling conexion ok
                if (!isOnlineMode){
                    setNitValidationMessage("Modo Fuera de Línea, continuar.");
                    validNitCi = Boolean.TRUE;
                    nitCiHasBeenValidated = Boolean.TRUE;
                } else {
                    setNitValidationMessage("Intente otra vez o Cambie a modo Fuera de Línea.");
                    validNitCi = Boolean.FALSE;
                    nitCiHasBeenValidated = Boolean.FALSE;
                }
            } else { // ebilling sin conexion
                setNitValidationMessage("Sistema sin conexion, consulte con el administrador.");
                validNitCi = Boolean.FALSE;
                nitCiHasBeenValidated = Boolean.FALSE;
            }
        }
    }

    // No usado
    public void initBillingMode() throws IOException {
        chekBillingMode();
        //setSignificantEventsCodes(billControllerAction.querySignificantEvents());
    }

    public void chekBillingMode() throws IOException {

        if (billControllerAction.checkBillingMode())
            this.setBillingMode("En Linea");
        else
            this.setBillingMode("Fuera de Linea");

    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public SaleTypeEnum getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleTypeEnum saleType) {
        this.saleType = saleType;
    }

    public CustomerOrderType getCustomerOrderType() {
        return customerOrderType;
    }

    public void setCustomerOrderType(CustomerOrderType customerOrderType) {
        this.customerOrderType = customerOrderType;
    }

    public List<String> getProductItemCodesSelected() {
        return productItemCodesSelected;
    }

    public void setProductItemCodesSelected(List<String> productItemCodesSelected) {
        this.productItemCodesSelected = productItemCodesSelected;
    }

    public Distributor getDistributor() {
        return distributor;
    }

    public void setDistributor(Distributor distributor) {
        this.distributor = distributor;
    }

    public SubsidyEnun getSubsidyEnun() {
        return subsidyEnun;
    }

    public void setSubsidyEnun(SubsidyEnun subsidyEnun) {
        this.subsidyEnun = subsidyEnun;
    }

    public Boolean getFinalConsumer() {
        return finalConsumer;
    }

    public void setFinalConsumer(Boolean finalConsumer) {
        this.finalConsumer = finalConsumer;
    }

    public CustomerCategoryType getCustomerCategoryTypeEnum() {
        return customerCategoryTypeEnum;
    }

    public void setCustomerCategoryTypeEnum(CustomerCategoryType customerCategoryTypeEnum) {
        this.customerCategoryTypeEnum = customerCategoryTypeEnum;
    }

    public Map<String, BigDecimal> getPriceItemListMap() {
        return priceItemListMap;
    }

    public void setPriceItemListMap(Map<String, BigDecimal> priceItemListMap) {
        this.priceItemListMap = priceItemListMap;
    }

    private User getUser(Long id) {
        try {
            return userService.findById(User.class, id);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

    public Boolean getZeroProduct() {
        return zeroProduct;
    }

    public void setZeroProduct(Boolean zeroProduct) {
        this.zeroProduct = zeroProduct;
    }

    public Date getBillingSpecialDate() {
        return billingSpecialDate;
    }

    public void setBillingSpecialDate(Date billingSpecialDate) {
        this.billingSpecialDate = billingSpecialDate;
    }

    public String getNameSpecialBill() {
        return nameSpecialBill;
    }

    public void setNameSpecialBill(String nameSpecialBill) {
        this.nameSpecialBill = nameSpecialBill;
    }

    public Double getAmountSpecialBill() {
        return amountSpecialBill;
    }

    public void setAmountSpecialBill(Double amountSpecialBill) {
        this.amountSpecialBill = amountSpecialBill;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getBillingMode() {
        return billingMode;
    }

    public void setBillingMode(String billingMode) {
        this.billingMode = billingMode;
    }

    public List<SignificantEventCodePOJO> getSignificantEventsCodes() {
        return significantEventsCodes;
    }

    public void setSignificantEventsCodes(List<SignificantEventCodePOJO> significantEventsCodes) {
        this.significantEventsCodes = significantEventsCodes;
    }

    public SignificantEventCodePOJO getSignificantEventSelected() {
        return significantEventSelected;
    }

    public void setSignificantEventSelected(SignificantEventCodePOJO significantEventSelected) {
        this.significantEventSelected = significantEventSelected;
    }

    public String getCafcCode() {
        return cafcCode;
    }

    public void setCafcCode(String cafcCode) {
        this.cafcCode = cafcCode;
    }

    public boolean isShowCAFC() {
        return showCAFC;
    }

    public void setShowCAFC(boolean showCAFC) {
        this.showCAFC = showCAFC;
    }

    public SignificantEventSIN getSignificantEventSIN() {
        return significantEventSIN;
    }

    public void setSignificantEventSIN(SignificantEventSIN significantEventSIN) {
        this.significantEventSIN = significantEventSIN;
    }

    public String getNitValidationMessage() {
        return nitValidationMessage;
    }

    public void setNitValidationMessage(String nitValidationMessage) {
        this.nitValidationMessage = nitValidationMessage;
    }

    public Boolean getValidNitCi() {
        return validNitCi;
    }

    public void setValidNitCi(Boolean validNitCi) {
        this.validNitCi = validNitCi;
    }

    public Boolean getNitCiHasBeenValidated() {
        return nitCiHasBeenValidated;
    }

    public void setNitCiHasBeenValidated(Boolean nitCiHasBeenValidated) {
        this.nitCiHasBeenValidated = nitCiHasBeenValidated;
    }

    public Integer getInvoiceNumberCafc() {
        return invoiceNumberCafc;
    }

    public void setInvoiceNumberCafc(Integer invoiceNumberCafc) {
        this.invoiceNumberCafc = invoiceNumberCafc;
    }

    public BigDecimal getAdditionalDiscountAmount() {
        return additionalDiscountAmount;
    }

    public void setAdditionalDiscountAmount(BigDecimal additionalDiscountAmount) {
        this.additionalDiscountAmount = additionalDiscountAmount;
    }
}
