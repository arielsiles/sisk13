package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Detalle (linea) del Vale de Despacho de Productos Terminados.
 * Tabla: inv_valedespacho_det.
 * Cada linea referencia un ProductItem (cod_art + no_cia) y captura cantidad,
 * costo unitario (snapshot al aprobar) y monto.
 */
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_valedespacho_det", schema = Constants.FINANCES_SCHEMA)
public class WarehouseVoucherDispatchDetail implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalledespacho", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idvaledespacho", nullable = false)
    private WarehouseVoucherDispatch dispatch;

    @Column(name = "no_cia_art", nullable = false, length = 2)
    @NotNull
    @Length(max = 2)
    private String productItemCompanyNumber;

    @Column(name = "cod_art", nullable = false, length = 6)
    @NotNull
    @Length(max = 6)
    private String productItemCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia_art", referencedColumnName = "no_cia",
                    nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art",
                    nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    @Column(name = "cod_med", nullable = false, length = 6)
    @NotNull
    @Length(max = 6)
    private String measureUnitCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia_art", referencedColumnName = "no_cia",
                    nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_med", referencedColumnName = "cod_med",
                    nullable = false, insertable = false, updatable = false)
    })
    private MeasureUnit measureUnit;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 6)
    @NotNull
    private BigDecimal quantity;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 6)
    @NotNull
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "monto", nullable = false, precision = 14, scale = 6)
    @NotNull
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "cantidad_bolsas")
    private Integer bagsCount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipoenvase")
    private InventoryPackaging packaging;

    @Column(name = "observacion", length = 250)
    @Length(max = 250)
    private String observation;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WarehouseVoucherDispatch getDispatch() {
        return dispatch;
    }

    public void setDispatch(WarehouseVoucherDispatch dispatch) {
        this.dispatch = dispatch;
    }

    public String getProductItemCompanyNumber() {
        return productItemCompanyNumber;
    }

    public void setProductItemCompanyNumber(String productItemCompanyNumber) {
        this.productItemCompanyNumber = productItemCompanyNumber;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
        if (productItem != null && productItem.getId() != null) {
            this.productItemCompanyNumber = productItem.getId().getCompanyNumber();
            this.productItemCode = productItem.getId().getProductItemCode();
        }
    }

    public String getMeasureUnitCode() {
        return measureUnitCode;
    }

    public void setMeasureUnitCode(String measureUnitCode) {
        this.measureUnitCode = measureUnitCode;
    }

    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
        if (measureUnit != null && measureUnit.getId() != null) {
            this.measureUnitCode = measureUnit.getId().getMeasureUnitCode();
        }
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getBagsCount() {
        return bagsCount;
    }

    public void setBagsCount(Integer bagsCount) {
        this.bagsCount = bagsCount;
    }

    public InventoryPackaging getPackaging() {
        return packaging;
    }

    public void setPackaging(InventoryPackaging packaging) {
        this.packaging = packaging;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
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
}
