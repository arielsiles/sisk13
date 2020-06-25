package com.encens.khipus.action.customers;

import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.customers.CustomerOrderTypeService;
import com.encens.khipus.service.customers.PriceItemService;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    //private CustomerOrderTypeEnum customerOrderTypeEnum = CustomerOrderTypeEnum.NORMAL;

    private CustomerOrderType customerOrderType;
    private SaleTypeEnum saleType;
    private Client client;
    private Distributor distributor;
    private Date orderDate = new Date();
    private String observation;

    private SubsidyEnun subsidyEnun;


    //private List<ProductItem> productsSelected = new ArrayList<ProductItem>();
    private List<String> productItemCodesSelected = new ArrayList<String>();

    private List<ArticleOrder> articleOrderList = new ArrayList<ArticleOrder>();

    //private Map<String, BigDecimal> priceItemListMap = new HashMap<String, BigDecimal>();
    private Map<String, BigDecimal> priceItemListMap;

    @In(required = false)
    private User currentUser;

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

    /*@Create
    public void initialize() {
        System.out.println("------------------> Inicializando..............");
        setOrderDate(new Date());
    }*/

    @Factory(value = "subsidyEnumList")
    public SubsidyEnun[] getExperienceType() {
        return SubsidyEnun.values();
    }

    public void openSale(){
        setCustomerOrderType(customerOrderTypeService.findCustomerOrderTypeDefault());
    }

    public void addProduct(ProductItem productItem){
        setProductItem(productItem);
        addProduct();
    }

    public void addProduct(){
        System.out.println("--------> productItemFullName: " + productItemFullName);
        System.out.println("--------> Producto: " + productItem);

        if (client == null) return;

        if (productItemCodesSelected.contains(productItem.getProductItemCode())){
            System.out.println("------>>> contains(productItemCode): " + productItemCodesSelected.contains(productItem.getProductItemCode()));
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

        if (priceItemListMap != null){
            BigDecimal price = priceItemListMap.get(productItem.getProductItemCode());
            System.out.println("--------------> PRICE: " + price);
            if (price != null)
                articleOrder.setPrice(price.doubleValue());
        }

        articleOrderList.add(articleOrder);

        clearProduct();
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
        Double amount = articleOrder.getQuantity() * articleOrder.getPrice();
        articleOrder.setAmount(BigDecimalUtil.toBigDecimal(amount).doubleValue());
    }

    public BigDecimal calculateTotalAmount(){
        BigDecimal result = BigDecimal.ZERO;
        for (ArticleOrder articleOrder:articleOrderList){
            result = BigDecimalUtil.sum(result, BigDecimalUtil.toBigDecimal(articleOrder.getAmount()));
        }
        setTotalAmount(result);
        return result;
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
    }

    public void registerSale(){
        System.out.println("------------> Registrando venta Total: " + getTotalAmount());
        System.out.println("------------> Description: " + getObservation());
        System.out.println("------------> Fecha: " + DateUtils.format(getOrderDate(), "dd/MM/yyyy"));
        createSale();
        clearAll();
        assignCustomerOrderTypeDefault();
    }

    public void registerCashSale(){
        System.out.println("......Registrando Venta al Contado...");
        createSale();
        clearAll();
        assignCustomerOrderTypeDefault();
    }

    public void createSale(){

        System.out.println("------------> user: " + currentUser);
        System.out.println("------------> tipo pedido: " + customerOrderType);

        CustomerOrder customerOrder = new CustomerOrder();
        Long saleCode = new Long(financesPkGeneratorService.getNextNoTransByDocumentType(saleType.getSequenceName()));
        customerOrder.setCode(saleCode);
        customerOrder.setUser(currentUser);
        customerOrder.setOrderDate(orderDate);
        customerOrder.setObservation(observation);
        customerOrder.setState(SaleStatus.PREPARAR);
        customerOrder.setTotalAmount(totalAmount.doubleValue());
        customerOrder.setSaleType(saleType);
        customerOrder.setCustomerOrderType(customerOrderType);
        customerOrder.setClient(client);
        customerOrder.setDistributor(distributor);

        if (subsidyEnun != null)
            customerOrder.setDescripcion(subsidyEnun.getSubsidyType());

        customerOrder.setArticleOrderList(articleOrderList);

        String outcome = saleService.createSale(customerOrder);

    }

    public void assignClient(Client client){

        if (client.getCustomerCategory() != null)
            priceItemListMap = priceItemService.getPriceItemsMap(client.getCustomerCategory());

        setClient(client);
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

    public void initCashSale(){
        System.out.println("====>Cash Sale Fecha: " + this.orderDate);
        setSaleType(SaleTypeEnum.CASH);
        setMoneyReceived(calculateTotalAmount());
    }

    public List<ProductItem> getBestProductList(){
        return productItemService.findBestProductList();
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
        this.priceItemListMap = null;

        clearProduct();
        clearProductsSelected();
        clearTotalAmount();
        setObservation(null);
        setDistributor(null);
        setSubsidyEnun(null);

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
}
