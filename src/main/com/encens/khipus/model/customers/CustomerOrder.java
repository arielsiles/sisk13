package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
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
                                "and c.state = :accounted " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderType.id = 1 "),

                /** NO USED **/
                @NamedQuery(name = "CustomerOrder.findByDatesForCostsLac",
                        query = "select c from CustomerOrder c " +
                                "where c.orderDate between :startDate and :endDate " +
                                "and c.state <> :anulledState " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderType.id in (1,5) " +
                                "and c.user.id <> 5 "), /** breque y comercial **/

                @NamedQuery(name = "CustomerOrder.findByDatesForCostsVet",
                        query = "select c from CustomerOrder c " +
                                "where c.orderDate between :startDate and :endDate " +
                                "and c.state <> :anulledState " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderType.id in (1,6) " +
                                "and c.user.id = 5 "), /** cisc **/

                @NamedQuery(name = "CustomerOrder.findBetweenDates",
                        query = "select c from CustomerOrder c " +
                                "where c.orderDate between :startDate and :endDate " +
                                "and c.state <> 'ANULADO' " +
                                "and c.user.id = 408 " +
                                "and c.flagct = :flagct ")
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
    private String description;

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

    @Column(name = "montodist")
    private BigDecimal dealerAmount;

    /*@Column(name = "IDTIPOPEDIDO")
    private Long customerOrderTypeId;*/

    @Column(name = "tipoventa")
    @Enumerated(EnumType.STRING)
    private SaleTypeEnum saleType;

    @Basic(optional = true)
    @Column(name = "impuesto")
    private Double tax = 0.0;

    @Basic
    @Column(name = "contabilizado")
    private Boolean accounted;

    @Column(name = "flagct", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean flagct = Boolean.FALSE;

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

    @JoinColumn(name = "idmovimiento", referencedColumnName = "idmovimiento")
    @ManyToOne(optional = true)
    private Movement movement;

    @JoinColumn(name = "IDDISTRIBUIDOR", referencedColumnName = "IDDISTRIBUIDOR")
    @ManyToOne(optional = true)
    private Distributor distributor;

    @JoinColumn(name = "id_tmpenc", referencedColumnName = "id_tmpenc")
    @ManyToOne(optional = true)
    private Voucher voucher;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descripcion) {
        this.description = descripcion;
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

    public Distributor getDistributor() {
        return distributor;
    }

    public void setDistributor(Distributor distributor) {
        this.distributor = distributor;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Boolean getAccounted() {
        return accounted;
    }

    public void setAccounted(Boolean accounted) {
        this.accounted = accounted;
    }

    public BigDecimal getDealerAmount() {
        return dealerAmount;
    }

    public void setDealerAmount(BigDecimal dealerAmount) {
        this.dealerAmount = dealerAmount;
    }

    public String getInvoiceNumber(){
        String result = "";

        if (getMovement() != null){
            result = getMovement().getNumber().toString();
        }

        return result;
    }

    public String isValidInvoice(){
        String result = "NO";
        if (getMovement() != null){
            if (getMovement().getCodigoEstado() != null)
                if (getMovement().getCodigoEstado().equals("908"))
                    result = "SI";
        }
        return result;
    }

    public String getInvoiceStatusSIN(){
        String result = "";
        if (getMovement() != null){
            if (getMovement().getCuf() != null){
                result = getMovement().getDescri();
                if (result.equals("ANULACION CONFIRMADA"))
                    result = "ANULADA";
            }
        }
        return result;
    }

    public Boolean getFlagct() {
        return flagct;
    }

    public void setFlagct(Boolean flagct) {
        this.flagct = flagct;
    }
}
