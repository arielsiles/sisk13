package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
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
                                "where c.fechaEntrega between :startDate and :endDate " +
                                "and c.estado = 'CONTABILIZADO' " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderTypeId = 1 "),

                @NamedQuery(name = "CustomerOrder.findByDatesForCostsLac",
                        query = "select c from CustomerOrder c " +
                                "where c.fechaEntrega between :startDate and :endDate " +
                                "and c.estado <> 'ANULADO' " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderTypeId in (1,5) " +
                                "and c.userId <> 5 "), /** breque y comercial **/

                @NamedQuery(name = "CustomerOrder.findByDatesForCostsVet",
                        query = "select c from CustomerOrder c " +
                                "where c.fechaEntrega between :startDate and :endDate " +
                                "and c.estado <> 'ANULADO' " +
                                "and c.cvFlag = 0 " +
                                "and c.customerOrderTypeId in (1,6) " +
                                "and c.userId = 5 ") /** cisc **/
        }
)

@Entity
@Table(name = "pedidos")
public class CustomerOrder implements BaseModel  {

    //todo:revisar por q el id no es correlativo
    @Id
    @Column(name = "IDPEDIDOS")
    private Long idpedidos;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "FECHA_PEDIDO")
    @Temporal(TemporalType.DATE)
    private Date fechaPedido;

    @Basic(optional = false)
    @Column(name = "FECHA_ENTREGA")
    @Temporal(TemporalType.DATE)

    private Date fechaEntrega;
    @Column(name = "FECHA_A_PAGAR")
    @Temporal(TemporalType.DATE)

    private Date fechaAPagar;

    @Column(name = "OBSERVACION")
    private String observacion;

    @Column(name = "FACTURA")
    private String factura;

    @Column(name = "PORCENTAJECOMISION")
    private Double porcentajeComision = 0.0;

    @Column(name = "PORCENTAJEGARANTIA")
    private Double porcentajeGarantia = 0.0;

    @Column(name = "VALORCOMISION")
    private Double valorComision = 0.0;

    @Column(name = "VALORGARANTIA")
    private Double valorGarantia = 0.0;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "CON_REPOSICION",columnDefinition = "INT(1)")
    private Boolean conReposicion = false;

    @Column(name = "TOTAL")
    private Double total = 0.0;
    //cantidad * precio de venta

    @Basic(optional = false)
    @Column(name = "TOTALIMPORTE")
    private Double totalimporte = 0.0;

    @Column(name = "flagstock", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean stockFlag = Boolean.FALSE;

    @Column(name = "CV", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean cvFlag = Boolean.FALSE;

    @Column(name = "IDTIPOPEDIDO")
    private Long customerOrderTypeId;

    @Column(name = "IDUSUARIO")
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "customerOrder")
    private Collection<ArticleOrder> articulosPedidos ;

    @Basic
    @Column(name="codigo",columnDefinition="bigint(20)")
    private Long codigo;

    @JoinColumn(name = "IDCLIENTE", referencedColumnName = "IDPERSONACLIENTE")
    @ManyToOne(optional = false)
    private Client cliente;

    public Long getIdpedidos() {
        return idpedidos;
    }

    public void setIdpedidos(Long idpedidos) {
        this.idpedidos = idpedidos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(Date fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Date fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Date getFechaAPagar() {
        return fechaAPagar;
    }

    public void setFechaAPagar(Date fechaAPagar) {
        this.fechaAPagar = fechaAPagar;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public Double getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(Double porcenDescuento) {
        this.porcentajeComision = porcenDescuento;
    }

    public Double getPorcentajeGarantia() {
        return porcentajeGarantia;
    }

    public void setPorcentajeGarantia(Double porcenRetencion) {
        this.porcentajeGarantia = porcenRetencion;
    }

    public String getEstado() {
        if(estado == null)
        {
            estado = "ACTIVO";
        }
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idpedidos != null ? idpedidos.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "com.encens.khipus.model.Pedidos[ idpedidos=" + idpedidos + " ]";
    }

    public Double getValorComision() {
        return valorComision;
    }

    public void setValorComision(Double valorDescuento) {
        this.valorComision = valorDescuento;
    }

    public Double getValorGarantia() {
        return valorGarantia;
    }

    public void setValorGarantia(Double valorRetencion) {
        this.valorGarantia = valorRetencion;
    }

    public void setTotalimporte(Double totalimporte) {
        this.totalimporte = totalimporte;
    }

    public Boolean getConReposicion() {
        return conReposicion;
    }

    public void setConReposicion(Boolean conReposicion) {
        this.conReposicion = conReposicion;
    }

    @Override
    public Object getId() {
        return null;
    }

    public Collection<ArticleOrder> getArticulosPedidos() {
        return articulosPedidos;
    }

    public void setArticulosPedidos(Collection<ArticleOrder> articulosPedidos) {
        this.articulosPedidos = articulosPedidos;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public Client getCliente() {
        return cliente;
    }

    public void setCliente(Client cliente) {
        this.cliente = cliente;
    }

    public Double getTotalimporte() {
        return totalimporte;
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

    public Long getCustomerOrderTypeId() {
        return customerOrderTypeId;
    }

    public void setCustomerOrderTypeId(Long customerOrderTypeId) {
        this.customerOrderTypeId = customerOrderTypeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
