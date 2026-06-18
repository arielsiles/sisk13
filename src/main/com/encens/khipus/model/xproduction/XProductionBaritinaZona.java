package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Distribucion del uso de materia prima Baritina por zona productiva
 * (N filas por orden de produccion).
 *
 * Reutiliza el maestro existente {@link ProductiveZone} (acopio de materia
 * prima). Cada fila registra el porcentaje de uso de una zona y la cantidad
 * calculada = uso_mp_tn * porcentaje / 100. La suma de porcentajes de una
 * orden debe ser 100%.
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "XProductionBaritinaZona.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_produccion_baritina_zona",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "xpr_produccion_baritina_zona")
public class XProductionBaritinaZona implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XProductionBaritinaZona.tableGenerator")
    @Column(name = "idproduccion_baritina_zona", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproduccion", nullable = false)
    private XProduction production;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", nullable = false)
    private ProductiveZone productiveZone;

    /** Porcentaje de uso de la zona (0..100). */
    @Column(name = "porcentaje", precision = 8, scale = 4)
    private BigDecimal porcentaje;

    /** Cantidad usada de la zona (TN) = uso_mp_tn * porcentaje / 100. */
    @Column(name = "cantidad_tn", precision = 14, scale = 4)
    private BigDecimal cantidadTn;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public XProduction getProduction() {
        return production;
    }

    public void setProduction(XProduction production) {
        this.production = production;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal getCantidadTn() {
        return cantidadTn;
    }

    public void setCantidadTn(BigDecimal cantidadTn) {
        this.cantidadTn = cantidadTn;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
