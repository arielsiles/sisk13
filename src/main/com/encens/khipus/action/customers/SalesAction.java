package com.encens.khipus.action.customers;

import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Name("salesAction")
@Scope(ScopeType.PAGE)
public class SalesAction {

    private ProductItem productItem;
    private String productItemFullName;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private List<ProductItem> productsSelected = new ArrayList<ProductItem>();
    private List<ArticleOrder> articleOrderList = new ArrayList<ArticleOrder>();

    @In
    private ProductItemService productItemService;



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
}
