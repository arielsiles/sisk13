package com.encens.khipus.model.customers;

import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */
@NamedQueries({
        @NamedQuery(name  = "ArticleOrder.findCashSaleDetailByCodeAndDate",
                    query = "select articleOrder " +
                            "from ArticleOrder articleOrder " +
                            "left join articleOrder.ventaDirecta cashSale " +
                            "where articleOrder.codArt =:productItemCode " +
                            "and cashSale.fechaPedido between :startDate and :endDate "),
        @NamedQuery(name  = "ArticleOrder.findOrderDetailByCodeAndDate",
                query = "select articleOrder " +
                        "from ArticleOrder articleOrder " +
                        "left join articleOrder.customerOrder customerOrder " +
                        "where articleOrder.codArt =:productItemCode " +
                        "and customerOrder.fechaEntrega between :startDate and :endDate ")
        })

@Entity
@Table(name = "ARTICULOS_PEDIDO",schema = Constants.CASHBOX_SCHEMA)
public class ArticleOrder {
    @Id
    @Column(name = "IDARTICULOSPEDIDO")
    private Long id;

    @Column(name = "COD_ART", nullable = false, length = 6,insertable=false,updatable=false)
    @Length(max = 6)
    private String codArt;

    @Column(name = "CU", precision = 16, scale = 6)
    private BigDecimal cu;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "COD_ART", referencedColumnName = "COD_ART")
    })
    private ProductItem productItem;

    @JoinColumn(name = "IDPEDIDOS",referencedColumnName = "IDPEDIDOS")
    @ManyToOne
    private CustomerOrder customerOrder;

    @JoinColumn(name = "IDVENTADIRECTA",referencedColumnName = "IDVENTADIRECTA")
    @ManyToOne
    private VentaDirecta ventaDirecta;

    @Column(name = "CANTIDAD", nullable = true)
    private Integer amount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    //@JoinColumn(name = "COD_ALM", nullable = true, updatable = false, insertable = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA",nullable = true,updatable = false,insertable = false),
            @JoinColumn(name = "COD_ALM", referencedColumnName = "COD_ALM",nullable = true,updatable = false,insertable = false)
    })
    private Warehouse warehouse;

    @Column(name = "PRECIO",nullable = true )
    private Double price;

    @Column(name = "REPOSICION",nullable = true)
    private Integer reposicion;

    @Column(name = "TOTAL",nullable = true)
    private Integer total;

    @Column(name = "PROMOCION",nullable = true)
    private Integer promotion;

    @Column(name="TIPO")
    private String tipo;


    public String getCodArt() {
        return codArt;
    }

    public void setCodArt(String codArt) {
        this.codArt = codArt;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getReposicion() {
        return reposicion;
    }

    public void setReposicion(Integer reposicion) {
        this.reposicion = reposicion;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPromotion() {
        return promotion;
    }

    public void setPromotion(Integer promotion) {
        this.promotion = promotion;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public VentaDirecta getVentaDirecta() {
        return ventaDirecta;
    }

    public void setVentaDirecta(VentaDirecta ventaDirecta) {
        this.ventaDirecta = ventaDirecta;
    }

    public BigDecimal getCu() {
        return cu;
    }

    public void setCu(BigDecimal cu) {
        this.cu = cu;
    }
}
