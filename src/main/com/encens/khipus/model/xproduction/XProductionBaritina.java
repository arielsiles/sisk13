package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Datos especificos de produccion BARITINA (1 a 1 con {@link XProduction}).
 *
 * Espeja el patron de {@link XProductionUlexita}: tabla satelite que solo aplica
 * a ordenes cuya linea es de tipo {@link ProductionLineType#BARITINA}. Almacena
 * la cantidad de materia prima usada y los totales propios del proceso de
 * molienda de baritina. La distribucion por zonas productivas se modela aparte
 * en {@link XProductionBaritinaZona}.
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "XProductionBaritina.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_produccion_baritina",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "xpr_produccion_baritina",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idproduccion"}))
public class XProductionBaritina implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XProductionBaritina.tableGenerator")
    @Column(name = "idproduccion_baritina", nullable = false)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproduccion", nullable = false, unique = true)
    private XProduction production;

    /** Uso de materia prima Baritina (TN). Base para el calculo por zona. */
    @Column(name = "uso_mp_tn", precision = 14, scale = 4)
    private BigDecimal usoMpTn;

    /** Baritina producto terminado (TN). */
    @Column(name = "pt_tn", precision = 14, scale = 4)
    private BigDecimal ptTn;

    /** Turnos producidos en la jornada. */
    @Column(name = "turnos")
    private Integer turnos;

    @Column(name = "observacion", length = 500)
    private String observacion;

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

    public BigDecimal getUsoMpTn() {
        return usoMpTn;
    }

    public void setUsoMpTn(BigDecimal usoMpTn) {
        this.usoMpTn = usoMpTn;
    }

    public BigDecimal getPtTn() {
        return ptTn;
    }

    public void setPtTn(BigDecimal ptTn) {
        this.ptTn = ptTn;
    }

    public Integer getTurnos() {
        return turnos;
    }

    public void setTurnos(Integer turnos) {
        this.turnos = turnos;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
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
