package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.xproduction.ProductionGroup;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Cabecera del Vale de Despacho de Productos Terminados.
 * Tabla: inv_valedespacho.
 * <p>
 * Cada despacho captura los datos comerciales (cliente, transportadora,
 * vendedor), logisticos (conductor y vehiculo via catalogo, lugares),
 * de pesaje (tara/bruto/neto, boleta balanza), de carguio (numero de bolsas,
 * horario) y de inventario (almacen de productos terminados, productos a
 * egresar).
 * <p>
 * Al aprobarse (estado APROBADO), se genera un {@link WarehouseVoucher} de
 * egreso vinculado por (no_cia_vale, no_trans_vale) que dispara el descuento
 * de stock y el asiento contable. El numero de orden de entrega
 * ({@link #deliveryOrderNumber}) se asigna en ese momento desde la secuencia
 * "DISPATCH_ORDER_NUMBER".
 */
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_valedespacho", schema = Constants.FINANCES_SCHEMA)
public class WarehouseVoucherDispatch implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idvaledespacho", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    @NotNull
    private DispatchState state = DispatchState.BORRADOR;

    @Column(name = "no_orden_entrega")
    private Long deliveryOrderNumber;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_despacho", nullable = false)
    @NotNull
    private Date dispatchDate;

    /* -------- Vendedor de entrega (JobContract) -------- */

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontratopuestovend")
    private JobContract deliverySeller;

    /* -------- Datos comerciales -------- */

    @Column(name = "codigo_lote_venta", nullable = false, length = 80)
    @NotNull
    @Length(max = 80)
    private String salesLotCode;

    @Column(name = "numero_factura", length = 50)
    @Length(max = 50)
    private String invoiceNumber;

    /* -------- Transportadora (Provider, PK compuesta no_cia + cod_prov) -------- */

    @Column(name = "cod_prov", length = 6)
    @Length(max = 6)
    private String transportCompanyCode;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia",
                    insertable = false, updatable = false),
            @JoinColumn(name = "cod_prov", referencedColumnName = "cod_prov",
                    insertable = false, updatable = false)
    })
    private Provider transportCompany;

    /* -------- Cliente -------- */

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcliente")
    private Client client;

    /* -------- Turno de produccion -------- */

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idturno")
    private ProductionGroup productionTurn;

    /* -------- Lugares origen y destino (catalogo DispatchPlace) -------- */

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idlugar_origen")
    private DispatchPlace originPlace;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idlugar_destino")
    private DispatchPlace destinationPlace;

    /* -------- Conductor / vehiculo (FK a catalogos inv_conductor / inv_vehiculo) -------- */

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idconductor")
    private Driver driver;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idvehiculo")
    private Vehicle vehicle;

    /* -------- Carguio -------- */

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "hora_inicio", nullable = false)
    @NotNull
    private Date loadingStartTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "hora_fin", nullable = false)
    @NotNull
    private Date loadingEndTime;

    @Column(name = "no_camion", nullable = false)
    @NotNull
    private Integer truckDispatchNumber;

    /* -------- Pesaje balanza -------- */

    @Column(name = "no_boleta_balanza", nullable = false, length = 30)
    @NotNull
    @Length(max = 30)
    private String weighingTicketNumber;

    @Column(name = "peso_tara_kg", nullable = false, precision = 12, scale = 3)
    @NotNull
    private BigDecimal tareWeightKg;

    @Column(name = "peso_bruto_kg", nullable = false, precision = 12, scale = 3)
    @NotNull
    private BigDecimal grossWeightKg;

    @Column(name = "peso_neto_kg", nullable = false, precision = 12, scale = 3)
    @NotNull
    private BigDecimal netWeightKg;

    /* -------- Almacen / responsable / unidad / centro de costo -------- */

    @Column(name = "no_cia", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "cod_alm", length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia",
                    insertable = false, updatable = false),
            @JoinColumn(name = "cod_alm", referencedColumnName = "cod_alm",
                    insertable = false, updatable = false)
    })
    private Warehouse warehouse;

    @Column(name = "cod_cc", length = 8)
    @Length(max = 8)
    private String costCenterCode;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia",
                    insertable = false, updatable = false),
            @JoinColumn(name = "cod_cc", referencedColumnName = "cod_cc",
                    insertable = false, updatable = false)
    })
    private CostCenter costCenter;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idresponsable")
    private Employee responsible;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio")
    private BusinessUnit executorUnit;

    /* -------- Enlace al WarehouseVoucher generado al aprobar -------- */

    @Column(name = "no_cia_vale", length = 2)
    @Length(max = 2)
    private String warehouseVoucherCompanyNumber;

    @Column(name = "no_trans_vale", length = 10)
    @Length(max = 10)
    private String warehouseVoucherTransactionNumber;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia_vale", referencedColumnName = "no_cia",
                    insertable = false, updatable = false),
            @JoinColumn(name = "no_trans_vale", referencedColumnName = "no_trans",
                    insertable = false, updatable = false)
    })
    private WarehouseVoucher warehouseVoucher;

    /* -------- Hoja de Ruta -------- */

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idruta")
    private DispatchRoute route;

    @Column(name = "vigencia_dias")
    private Integer validityDays;

    @Column(name = "vigencia_max_dias")
    private Integer maximumValidityDays = Integer.valueOf(1);

    /* -------- Observacion / auditoria -------- */

    @Column(name = "observacion", length = 1000)
    @Length(max = 1000)
    private String observation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_anul")
    private Date annulDate;

    @Column(name = "usuario_anul", length = 4)
    @Length(max = 4)
    private String annulUser;

    @Column(name = "motivo_anul", length = 250)
    @Length(max = 250)
    private String annulReason;

    @Column(name = "createdby", length = 4)
    @Length(max = 4)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createddate")
    private Date createdDate;

    @Column(name = "updatedby", length = 4)
    @Length(max = 4)
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updateddate")
    private Date updatedDate;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    /* -------- Detalle de productos -------- */

    @OneToMany(mappedBy = "dispatch", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<WarehouseVoucherDispatchDetail> details = new ArrayList<WarehouseVoucherDispatchDetail>();

    /* ================== Getters / Setters ================== */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DispatchState getState() {
        return state;
    }

    public void setState(DispatchState state) {
        this.state = state;
    }

    public Long getDeliveryOrderNumber() {
        return deliveryOrderNumber;
    }

    public void setDeliveryOrderNumber(Long deliveryOrderNumber) {
        this.deliveryOrderNumber = deliveryOrderNumber;
    }

    public Date getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(Date dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public JobContract getDeliverySeller() {
        return deliverySeller;
    }

    public void setDeliverySeller(JobContract deliverySeller) {
        this.deliverySeller = deliverySeller;
    }

    public String getSalesLotCode() {
        return salesLotCode;
    }

    public void setSalesLotCode(String salesLotCode) {
        this.salesLotCode = salesLotCode;
    }

    /**
     * Cantidad total de bolsas del despacho, derivado de la suma de
     * bagsCount de cada detalle. Reemplaza el campo persistente eliminado.
     */
    @Transient
    public Integer getBagCount() {
        int total = 0;
        if (details != null) {
            for (WarehouseVoucherDispatchDetail d : details) {
                if (d.getBagsCount() != null) {
                    total += d.getBagsCount();
                }
            }
        }
        return total;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getTransportCompanyCode() {
        return transportCompanyCode;
    }

    public void setTransportCompanyCode(String transportCompanyCode) {
        this.transportCompanyCode = transportCompanyCode;
    }

    public Provider getTransportCompany() {
        return transportCompany;
    }

    public void setTransportCompany(Provider transportCompany) {
        this.transportCompany = transportCompany;
        if (transportCompany != null && transportCompany.getId() != null) {
            this.transportCompanyCode = transportCompany.getId().getProviderCode();
            if (this.companyNumber == null) {
                this.companyNumber = transportCompany.getId().getCompanyNumber();
            }
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ProductionGroup getProductionTurn() {
        return productionTurn;
    }

    public void setProductionTurn(ProductionGroup productionTurn) {
        this.productionTurn = productionTurn;
    }

    public DispatchPlace getOriginPlace() {
        return originPlace;
    }

    public void setOriginPlace(DispatchPlace originPlace) {
        this.originPlace = originPlace;
    }

    public DispatchPlace getDestinationPlace() {
        return destinationPlace;
    }

    public void setDestinationPlace(DispatchPlace destinationPlace) {
        this.destinationPlace = destinationPlace;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Date getLoadingStartTime() {
        return loadingStartTime;
    }

    public void setLoadingStartTime(Date loadingStartTime) {
        this.loadingStartTime = loadingStartTime;
    }

    public Date getLoadingEndTime() {
        return loadingEndTime;
    }

    public void setLoadingEndTime(Date loadingEndTime) {
        this.loadingEndTime = loadingEndTime;
    }

    public Integer getTruckDispatchNumber() {
        return truckDispatchNumber;
    }

    public void setTruckDispatchNumber(Integer truckDispatchNumber) {
        this.truckDispatchNumber = truckDispatchNumber;
    }

    public String getWeighingTicketNumber() {
        return weighingTicketNumber;
    }

    public void setWeighingTicketNumber(String weighingTicketNumber) {
        this.weighingTicketNumber = weighingTicketNumber;
    }

    public BigDecimal getTareWeightKg() {
        return tareWeightKg;
    }

    public void setTareWeightKg(BigDecimal tareWeightKg) {
        this.tareWeightKg = tareWeightKg;
    }

    public BigDecimal getGrossWeightKg() {
        return grossWeightKg;
    }

    public void setGrossWeightKg(BigDecimal grossWeightKg) {
        this.grossWeightKg = grossWeightKg;
    }

    public BigDecimal getNetWeightKg() {
        return netWeightKg;
    }

    public void setNetWeightKg(BigDecimal netWeightKg) {
        this.netWeightKg = netWeightKg;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        if (warehouse != null && warehouse.getId() != null) {
            this.warehouseCode = warehouse.getId().getWarehouseCode();
            if (this.companyNumber == null) {
                this.companyNumber = warehouse.getId().getCompanyNumber();
            }
        }
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        if (costCenter != null && costCenter.getId() != null) {
            this.costCenterCode = costCenter.getId().getCode();
            if (this.companyNumber == null) {
                this.companyNumber = costCenter.getId().getCompanyNumber();
            }
        }
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public String getWarehouseVoucherCompanyNumber() {
        return warehouseVoucherCompanyNumber;
    }

    public void setWarehouseVoucherCompanyNumber(String warehouseVoucherCompanyNumber) {
        this.warehouseVoucherCompanyNumber = warehouseVoucherCompanyNumber;
    }

    public String getWarehouseVoucherTransactionNumber() {
        return warehouseVoucherTransactionNumber;
    }

    public void setWarehouseVoucherTransactionNumber(String warehouseVoucherTransactionNumber) {
        this.warehouseVoucherTransactionNumber = warehouseVoucherTransactionNumber;
    }

    public WarehouseVoucher getWarehouseVoucher() {
        return warehouseVoucher;
    }

    public void setWarehouseVoucher(WarehouseVoucher warehouseVoucher) {
        this.warehouseVoucher = warehouseVoucher;
        if (warehouseVoucher != null && warehouseVoucher.getId() != null) {
            this.warehouseVoucherCompanyNumber = warehouseVoucher.getId().getCompanyNumber();
            this.warehouseVoucherTransactionNumber = warehouseVoucher.getId().getTransactionNumber();
        }
    }

    public DispatchRoute getRoute() {
        return route;
    }

    public void setRoute(DispatchRoute route) {
        this.route = route;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public Integer getMaximumValidityDays() {
        return maximumValidityDays;
    }

    public void setMaximumValidityDays(Integer maximumValidityDays) {
        this.maximumValidityDays = maximumValidityDays;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Date getAnnulDate() {
        return annulDate;
    }

    public void setAnnulDate(Date annulDate) {
        this.annulDate = annulDate;
    }

    public String getAnnulUser() {
        return annulUser;
    }

    public void setAnnulUser(String annulUser) {
        this.annulUser = annulUser;
    }

    public String getAnnulReason() {
        return annulReason;
    }

    public void setAnnulReason(String annulReason) {
        this.annulReason = annulReason;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<WarehouseVoucherDispatchDetail> getDetails() {
        return details;
    }

    public void setDetails(List<WarehouseVoucherDispatchDetail> details) {
        this.details = details;
    }

    /* ================== Helpers de estado ================== */

    public boolean isDraft() {
        return state == DispatchState.BORRADOR;
    }

    public boolean isApproved() {
        return state == DispatchState.APROBADO;
    }

    public boolean isFinalized() {
        return state == DispatchState.FINALIZADO;
    }

    public boolean isAnnulled() {
        return state == DispatchState.ANULADO;
    }

    /** True si el despacho ya genero envases (APROBADO o FINALIZADO). */
    public boolean hasEnvelopes() {
        return state == DispatchState.APROBADO || state == DispatchState.FINALIZADO;
    }
}
