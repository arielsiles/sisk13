package com.encens.khipus.model.customers;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.products.Product;
import com.encens.khipus.model.products.ProductDiscountRule;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version $Id: InvoiceDetail.java 2008-9-10 10:56:08 $
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "InvoiceDetail.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "facturadetalle",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "facturadetalle")
public class InvoiceDetail {

    @Id
    @Column(name = "idfacturadetalle", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InvoiceDetail.tableGenerator")
    private Long id;

    /*@ManyToOne(optional = false)
    @JoinColumn(name = "ID_FACTURA", referencedColumnName = "ID_FACTURA", nullable = false)
    private Invoice invoice;*/

    @ManyToOne(optional = false)
    @JoinColumn(name = "idproducto", referencedColumnName = "idproducto", nullable = false)
    private Product product;

    @Column(name = "cantidad", nullable = false)
    private int quantity;

    @Column(name = "preciounitario", precision = 13, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "importe", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "descuentofacturadetalle",
            joinColumns = @JoinColumn(name = "idfacturadetalle"),
            inverseJoinColumns = @JoinColumn(name = "iddescuentoproducto"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA
    )
    @OrderBy("name asc")
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<ProductDiscountRule> discountRules = new ArrayList<ProductDiscountRule>(0);

    @Column(name = "descuento", precision = 13, scale = 2)
    private BigDecimal discount;

    @Column(name = "porcentajedescuento", precision = 13, scale = 2)
    private BigDecimal discountPercentage;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public InvoiceDetail(Product product, int quantity) {
        this.product = product;
        this.price = product.getSellPrice();
        this.quantity = quantity;
        this.totalAmount = price.multiply(BigDecimal.valueOf(quantity));
    }

    public InvoiceDetail(Product product, int quantity, List<ProductDiscountRule> rules, BigDecimal percentage, BigDecimal discount) {
        this.product = product;
        this.price = product.getSellPrice();
        this.quantity = quantity;
        this.discountRules = rules;
        this.discountPercentage = percentage;
        this.discount = discount;

        this.totalAmount = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalDiscount = new BigDecimal(0.0);
        if (discountPercentage != null) {
            totalDiscount = (totalAmount.abs()).multiply(discountPercentage).divide(new BigDecimal(100.0));
        }
        if (discount != null) {
            totalDiscount = totalDiscount.add(discount);
        }
        if (quantity > 0) {
            this.totalAmount = totalAmount.subtract(totalDiscount);
        } else {
            this.totalAmount = totalAmount.add(totalDiscount);
        }
    }

    public InvoiceDetail() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /*public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }*/

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (price != null) {
            totalAmount = price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        totalAmount = price.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ProductDiscountRule> getDiscountRules() {
        return discountRules;
    }

    public void setDiscountRules(List<ProductDiscountRule> discountRules) {
        this.discountRules = discountRules;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
