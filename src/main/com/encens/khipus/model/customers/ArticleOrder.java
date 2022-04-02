package com.encens.khipus.model.customers;

import com.encens.khipus.model.warehouse.ProductItem;
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
                        "and cashSale.estado <> 'ANULADO' " +
                        "and cashSale.fechaPedido between :startDate and :endDate "),
        @NamedQuery(name  = "ArticleOrder.findOrderDetailByCodeAndDate",
                query = "select articleOrder " +
                        "from ArticleOrder articleOrder " +
                        "left join articleOrder.customerOrder customerOrder " +
                        "where articleOrder.codArt =:productItemCode " +
                        "and customerOrder.state <> :annulledState " +
                        "and customerOrder.orderDate between :startDate and :endDate "),
        @NamedQuery(name  = "ArticleOrder.findCashSaleDetailListGroupBy",
                query = "select articleOrder.codArt, sum(articleOrder.total) as total " +
                        "from ArticleOrder articleOrder " +
                        "left join articleOrder.ventaDirecta cashSale " +
                        "where cashSale.estado <> 'ANULADO' " +
                        "and cashSale.fechaPedido between :startDate and :endDate " +
                        "group by articleOrder.codArt "),
        @NamedQuery(name  = "ArticleOrder.findCustomerOrderDetailListGroupBy",
                query = "select articleOrder.codArt, sum(articleOrder.total) as total " +
                        "from ArticleOrder articleOrder " +
                        "left join articleOrder.customerOrder customerOrder " +
                        "where customerOrder.state <> :annulledState " +
                        "and customerOrder.orderDate between :startDate and :endDate " +
                        "group by articleOrder.codArt "),
        @NamedQuery(name  = "ArticleOrder.findCashSaleDetailList",
                query = "select articleOrder " +
                        "from ArticleOrder articleOrder " +
                        "where articleOrder.ventaDirecta.estado <> 'ANULADO' " +
                        "and articleOrder.ventaDirecta.fechaPedido between :startDate and :endDate "),
        @NamedQuery(name  = "ArticleOrder.findCustomerOrderDetailList",
                query = "select articleOrder " +
                        "from ArticleOrder articleOrder " +
                        "where articleOrder.customerOrder.state <> :annulledState " +
                        "and articleOrder.customerOrder.orderDate between :startDate and :endDate ")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ArticleOrder.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "articulos_pedido",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "articulos_pedido", schema = Constants.KHIPUS_SCHEMA)
public class ArticleOrder {

    @Id
    @Column(name = "idarticulospedido")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ArticleOrder.tableGenerator")
    private Long id;

    @Column(name = "cod_art", nullable = false, length = 6,insertable=false,updatable=false)
    @Length(max = 6)
    private String codArt;

    @Column(name = "cu", precision = 16, scale = 6)
    private BigDecimal cu;

    @Column(name = "costo_uni", precision = 16, scale = 6)
    private BigDecimal unitCost;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia"),
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art")
    })
    private ProductItem productItem;

    @JoinColumn(name = "idpedidos",referencedColumnName = "idpedidos")
    @ManyToOne
    private CustomerOrder customerOrder;

    @JoinColumn(name = "idventadirecta",referencedColumnName = "idventadirecta")
    @ManyToOne
    private VentaDirecta ventaDirecta;

    @Column(name = "cantidad", nullable = true)
    private Integer quantity;

    /*@ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA",nullable = true,updatable = false,insertable = false),
            @JoinColumn(name = "COD_ALM", referencedColumnName = "COD_ALM",nullable = true,updatable = false,insertable = false)
    })
    private Warehouse warehouse;*/

    @Column(name = "precio",nullable = true )
    private Double price;

    @Column(name = "reposicion",nullable = true)
    private Integer reposicion;

    @Column(name = "total",nullable = true)
    private Integer total;

    @Column(name = "promocion",nullable = true)
    private Integer promotion;

    @Column(name="tipo")
    private String tipo;

    @Column(name = "descuento",nullable = true )
    private Double discount;

    @Column(name = "importe",nullable = true )
    private Double amount;

    @Column(name = "no_cia", updatable = false, insertable = false, length = 2)
    @Length(max = 2)
    private String companyNumber = "01";

    @Transient
    private BigDecimal unitaryBalance;

    public String getCodArt() {
        return codArt;
    }

    public void setCodArt(String codArt) {
        this.codArt = codArt;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer amount) {
        this.quantity = amount;
    }

    /*public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }*/

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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public BigDecimal getUnitaryBalance() {
        return unitaryBalance;
    }

    public void setUnitaryBalance(BigDecimal unitaryBalance) {
        this.unitaryBalance = unitaryBalance;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }
}
