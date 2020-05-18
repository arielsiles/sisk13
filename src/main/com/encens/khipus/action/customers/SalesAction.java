package com.encens.khipus.action.customers;

import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.customers.CustomerOrderTypeEnum;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private CustomerOrderTypeEnum customerOrderTypeEnum = CustomerOrderTypeEnum.NORMAL;
    private SaleTypeEnum saleType;
    private Client client;
    private Date orderDate = new Date();
    private String description;

    private List<ProductItem> productsSelected = new ArrayList<ProductItem>();
    private List<ArticleOrder> articleOrderList = new ArrayList<ArticleOrder>();

    @In
    private ProductItemService productItemService;

    /*@Create
    public void initialize() {
        System.out.println("------------------> Inicializando..............");
        setOrderDate(new Date());
    }*/

    public void addProduct(ProductItem productItem){
        setProductItem(productItem);
        addProduct();
    }

    public void addProduct(){
        System.out.println("--------> productItemFullName: " + productItemFullName);
        System.out.println("--------> Producto: " + productItem);

        if (productsSelected.contains(productItem)){
            clearProduct();
            return;
        }
        if (productItem == null) return;
        if (productItem.getSalePrice() == null) return;

        productsSelected.add(productItem);

        ArticleOrder articleOrder = new ArticleOrder();
        articleOrder.setCodArt(productItem.getProductItemCode());
        articleOrder.setProductItem(productItem);
        articleOrder.setQuantity(0);
        articleOrder.setPrice(productItem.getSalePrice().doubleValue());
        articleOrder.setPromotion(0);
        articleOrder.setReposicion(0);
        articleOrder.setTotal(0);
        articleOrder.setAmount(0.0);
        articleOrder.setCu(BigDecimal.ZERO);
        articleOrder.setUnitCost(BigDecimal.ZERO);

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
        productsSelected.remove(articleOrder.getProductItem());
    }

    public void calculateAmount(ArticleOrder articleOrder){
        Double amount = articleOrder.getQuantity() * articleOrder.getPrice();
        articleOrder.setAmount(BigDecimalUtil.toBigDecimal(amount).doubleValue());
    }

    public void calculateTotalAmount(){
        BigDecimal amount = BigDecimal.ZERO;
        for (ArticleOrder articleOrder:articleOrderList){
            amount = BigDecimalUtil.sum(amount, BigDecimalUtil.toBigDecimal(articleOrder.getAmount()));
        }
        setTotalAmount(amount);
    }

    public void clearProduct(){
        setProductItem(null);
        setProductItemFullName(null);
    }

    public void clearProductsSelected(){
        setProductsSelected(new ArrayList<ProductItem>());
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
        setDescription(null);
    }

    public void registerSale(){
        System.out.println("------------> Registrando venta Total: " + getTotalAmount());
        System.out.println("------------> Description: " + getDescription());
        System.out.println("------------> Fecha: " + DateUtils.format(getOrderDate(), "dd/MM/yyyy"));
        clearAll();
    }

    public void registerCashSale(){
        System.out.println("......Registrando Venta al Contado...");
    }

    public void initCreditSale(){
        setSaleType(SaleTypeEnum.CREDIT);
        System.out.println("====>Credit Fecha: " + this.orderDate);
    }

    public void initCashSale(){
        setSaleType(SaleTypeEnum.CASH);
        System.out.println("====>Cash Fecha: " + this.orderDate);
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

    public List<ProductItem> getProductsSelected() {
        return productsSelected;
    }

    public void setProductsSelected(List<ProductItem> productsSelected) {
        this.productsSelected = productsSelected;
    }

    public List<ArticleOrder> getArticleOrderList() {
        return articleOrderList;
    }

    public void setArticleOrderList(List<ArticleOrder> articleOrderList) {
        this.articleOrderList = articleOrderList;
    }

    public BigDecimal getTotalAmount() {
        calculateTotalAmount();
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getMoneyReceived() {
        moneyReceived = getTotalAmount();
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

    public CustomerOrderTypeEnum getCustomerOrderTypeEnum() {
        return customerOrderTypeEnum;
    }

    public void setCustomerOrderTypeEnum(CustomerOrderTypeEnum customerOrderTypeEnum) {
        this.customerOrderTypeEnum = customerOrderTypeEnum;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void clearClient(){
        setClient(null);
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SaleTypeEnum getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleTypeEnum saleType) {
        this.saleType = saleType;
    }
}
