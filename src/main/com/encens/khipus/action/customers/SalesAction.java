package com.encens.khipus.action.customers;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.*;
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

import java.math.BigDecimal;
import java.util.*;

@Name("salesAction")
@Scope(ScopeType.PAGE)
public class SalesAction {

    private ProductItem productItem;
    private String productItemFullName;
    private BigDecimal totalAmount = BigDecimal.ZERO;

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

        if (!isThereInventory(productItem)){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No existe inventario suficiente...");
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
        if (articleOrder.getQuantity() >  getUnitaryBalance(articleOrder).intValue()){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Inventario Insuficiente...");
            articleOrder.setQuantity(0);
        }

        Double amount = articleOrder.getQuantity() * articleOrder.getPrice();
        articleOrder.setAmount(BigDecimalUtil.toBigDecimal(amount).doubleValue());
        calculateTotalUnits(articleOrder);
    }

    public BigDecimal calculateTotalAmount(){
        BigDecimal result = BigDecimal.ZERO;
        setZeroProduct(Boolean.FALSE);
        for (ArticleOrder articleOrder:articleOrderList){
            result = BigDecimalUtil.sum(result, BigDecimalUtil.toBigDecimal(articleOrder.getAmount()));
            if (articleOrder.getQuantity() == 0) setZeroProduct(Boolean.TRUE); /** Verifica productos con cantidad CERO **/
        }
        setTotalAmount(result);
        return result;
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
        //Voucher voucher = accountingCashSale(customerOrder, movement);
        //Voucher voucher = accountingCreditSale(customerOrder, movement);
        //customerOrder.setVoucher(voucher);
        //customerOrder.setAccounted(Boolean.TRUE);
        //customerOrder.setState(SaleStatus.CONTABILIZADO);
        saleService.updateCustomerOrder(customerOrder);

        inventoryService.updateInventoryForSales(customerOrder);

        clearAll();
        assignCustomerOrderTypeDefault();
    }


    public void registerCashSale(){
        System.out.println("......Registrando Venta al Contado...");
        checkMinimumValues();

        CustomerOrder customerOrder = createSale();
        if (customerOrder!= null) {
            Movement movement = createInvoice(customerOrder);
            customerOrder.setMovement(movement);
            Voucher voucher = accountingCashSale(customerOrder, movement);
            customerOrder.setVoucher(voucher);
            customerOrder.setAccounted(Boolean.TRUE);
            saleService.updateCustomerOrder(customerOrder);

            inventoryService.updateInventoryForSales(customerOrder);

            clearAll();
            assignCustomerOrderTypeDefault();
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
        for (ArticleOrder articleOrder : articleOrderList){
            if (!isThereInventory(articleOrder.getProductItem())){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Inventario Insuficiente...");
                return null;
            }
        }

        observation = (observation == null) ? "" : observation;
        CustomerOrder customerOrder = new CustomerOrder();
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
        customerOrder.setCommissionPercentage(0.0);
        customerOrder.setCommissionValue(0.0);
        BigDecimal tax = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), Constants.VAT);
        customerOrder.setTax(tax.doubleValue());

        /** For commissions **/
        if (client.getCommission() > 0){
            BigDecimal percentage = BigDecimalUtil.divide(BigDecimalUtil.toBigDecimal(client.getCommission()), BigDecimalUtil.ONE_HUNDRED);
            BigDecimal commissionValue = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), percentage);
            BigDecimal totalVar = BigDecimalUtil.subtract(totalAmount, commissionValue);
            customerOrder.setTax(BigDecimalUtil.multiply(totalVar, Constants.VAT).doubleValue());
            customerOrder.setCommissionValue(commissionValue.doubleValue());
            customerOrder.setCommissionPercentage(client.getCommission());
        }

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

        String outcome = saleService.createSale(customerOrder);

        /*if (customerOrder.getSaleType().equals(SaleTypeEnum.CASH)){
            Movement movement = createInvoice(customerOrder);
            movementService.createMovement(movement);
            customerOrder.setMovement(movement);
            saleService.updateCustomerOrder(customerOrder);
        }*/

        return customerOrder;
    }

    private Movement createInvoice(CustomerOrder customerOrder){
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
        String controlCode = sfc.generateControlCode();

        Movement movement = new Movement();
        movement.setDate(sfc.getDate());
        movement.setState(Constants.MOVEMENT_STATE_V);
        movement.setNumber(dosage.getCurrentNumber().intValue());
        movement.setNit(sfc.getClientNit());
        movement.setName(sfc.getName());
        movement.setAmount(BigDecimalUtil.toBigDecimal(sfc.getAmount()));
        movement.setAmountIce(BigDecimal.ZERO);
        movement.setExemptExport(BigDecimal.ZERO);
        movement.setTaxedSalesZero(BigDecimal.ZERO);
        movement.setSubtotal(BigDecimalUtil.toBigDecimal(sfc.getAmount())); /** todo **/
        movement.setDiscount(BigDecimalUtil.toBigDecimal(customerOrder.getCommissionValue()));

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
        movement.setCustomerOrder(customerOrder);

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

        /*Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                MessageUtils.getMessage("Voucher.creditSale.gloss") + " " +
                        customerOrder.getCode() + " (F-" + movement.getNumber() + ") " + customerOrder.getClient().getFullName());*/
        Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                MessageUtils.getMessage("Voucher.creditSale.gloss") + " " +
                        customerOrder.getCode() + " " + customerOrder.getClient().getFullName());

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

        /*Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                                                            MessageUtils.getMessage("Voucher.cashSale.gloss") + " " +
                                                                  customerOrder.getCode() + " (F-" + movement.getNumber() + ") " + customerOrder.getClient().getFullName());*/

        Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                MessageUtils.getMessage("Voucher.cashSale.gloss") + " " +
                        customerOrder.getCode() + " " + customerOrder.getClient().getFullName());

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
            customerOrderBill.setState(SaleStatus.PREPARAR);
            customerOrderBill.setUser(currentUser);

            saleService.createSale(customerOrderBill);

            Movement movement = createInvoice(customerOrderBill);
            customerOrderBill.setMovement(movement);

            /*for(CustomerOrder customerOrder : customerOrderList){
                customerOrder.setMovement(movement);
                saleService.updateCustomerOrder(customerOrder);
            }*/

            // Para contabilizar el pedido para facturar fin de mes
            Voucher voucher = accountingCreditSale(customerOrderBill, movement);
            //voucher.setGloss(MessageUtils.getMessage("Voucher.creditSale.gloss") + " " + " (F-" + movement.getNumber() + ") " + customerOrderBill.getClient().getFullName());
            voucher.setGloss(MessageUtils.getMessage("Voucher.creditSale.gloss") + " " + customerOrderBill.getClient().getFullName());
            voucherAccoutingService.simpleUpdateVoucher(voucher);

            customerOrderBill.setVoucher(voucher);
            customerOrderBill.setAccounted(Boolean.TRUE);
            saleService.updateCustomerOrder(customerOrderBill);

        }
        clearSpecialBilling();
    }

    public void printInvoices(List<CustomerOrder> customerOrderList){


        for (CustomerOrder customerOrder : customerOrderList){

            System.out.println("==========> Venta seleccionada: " + customerOrder.getClient().getFullName() + " - Venta Nro: " + customerOrder.getCode() + " - Monto Bs: " + customerOrder.getTotalAmount());

        }



    }

    public boolean isAnnulled(CustomerOrder customerOrder){
        return customerOrder.getState().equals(SaleStatus.ANULADO);
    }

    public void assignClient(Client client){
        setClient(client);
        assignCustomerOrderTypeDefault();
        setCustomerCategoryTypeEnum(null);
        setPriceItemListMap(null);

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

        assignCustomerOrderTypeDefault();
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
}
