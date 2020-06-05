package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: AS
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */
@NamedQueries(
        {
                @NamedQuery(name = "CustomerOrder.findByDatesForCosts",
                        query = "select c from CustomerOrder c " +
                                "where c.orderDate between :startDate and :endDate " +
                                "and c.state = 'CONTABILIZADO' " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderType.id = 1 "),

                /** NO USED **/
                @NamedQuery(name = "CustomerOrder.findByDatesForCostsLac",
                        query = "select c from CustomerOrder c " +
                                "where c.orderDate between :startDate and :endDate " +
                                "and c.state <> 'ANULADO' " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderType.id in (1,5) " +
                                "and c.user.id <> 5 "), /** breque y comercial **/

                @NamedQuery(name = "CustomerOrder.findByDatesForCostsVet",
                        query = "select c from CustomerOrder c " +
                                "where c.orderDate between :startDate and :endDate " +
                                "and c.state <> 'ANULADO' " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderType.id in (1,6) " +
                                "and c.user.id = 5 ") /** cisc **/
        }
)

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "CustomerOrder.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pedidos",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "pedidos")
public class CustomerOrder implements BaseModel  {

    @Id
    @Column(name = "idpedidos", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CustomerOrder.tableGenerator")
    private Long id;
    /* -- */
    @Column(name = "flagstock", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean stockFlag = Boolean.FALSE;

    @Column(name = "CV", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean cvFlag = Boolean.FALSE;

    /* -- */

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "FECHA_PEDIDO")
    @Temporal(TemporalType.DATE)
    private Date currentDate = new Date();

    @Basic(optional = false)
    @Column(name = "FECHA_ENTREGA")
    @Temporal(TemporalType.DATE)
    private Date orderDate;

    @Column(name = "OBSERVACION")
    private String observation;

    @Column(name = "PORCENTAJECOMISION")
    private Double commissionPercentage = 0.0;

    @Column(name = "PORCENTAJEGARANTIA")
    private Double guaranteePercentage = 0.0;

    @Column(name = "VALORCOMISION")
    private Double commissionValue = 0.0;

    @Column(name = "VALORGARANTIA")
    private Double guaranteeValue = 0.0;

    @Column(name="codigo")
    private Long code;

    @Column(name = "ESTADO")
    @Enumerated(EnumType.STRING)
    private SaleStatus state;

    @Basic(optional = false)
    @Column(name = "totalimporte")
    private Double totalAmount = 0.0;

    /*@Column(name = "IDTIPOPEDIDO")
    private Long customerOrderTypeId;*/

    @Column(name = "tipoventa")
    @Enumerated(EnumType.STRING)
    private SaleTypeEnum saleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipopedido", referencedColumnName = "idtipopedido")
    private CustomerOrderType customerOrderType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    private User user;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "customerOrder")
    private Collection<ArticleOrder> articleOrderList;

    @JoinColumn(name = "IDCLIENTE", referencedColumnName = "IDPERSONACLIENTE")
    @ManyToOne(optional = false)
    private Client client;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
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

    public Double getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(Double commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public Double getGuaranteePercentage() {
        return guaranteePercentage;
    }

    public void setGuaranteePercentage(Double guaranteePercentage) {
        this.guaranteePercentage = guaranteePercentage;
    }

    public Double getCommissionValue() {
        return commissionValue;
    }

    public void setCommissionValue(Double commissionValue) {
        this.commissionValue = commissionValue;
    }

    public Double getGuaranteeValue() {
        return guaranteeValue;
    }

    public void setGuaranteeValue(Double guaranteeValue) {
        this.guaranteeValue = guaranteeValue;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long codigo) {
        this.code = codigo;
    }

    public SaleStatus getState() {
        return state;
    }

    public void setState(SaleStatus state) {
        this.state = state;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Collection<ArticleOrder> getArticleOrderList() {
        return articleOrderList;
    }

    public void setArticleOrderList(Collection<ArticleOrder> articleOrderList) {
        this.articleOrderList = articleOrderList;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Boolean getStockFlag() {
        return stockFlag;
    }

    public void setStockFlag(Boolean stockFlag) {
        this.stockFlag = stockFlag;
    }

    public Boolean getCvFlag() {
        return cvFlag;
    }

    public void setCvFlag(Boolean cvFlag) {
        this.cvFlag = cvFlag;
    }
}
