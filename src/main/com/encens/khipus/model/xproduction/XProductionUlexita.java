package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "XProductionUlexita.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_produccion_ulexita",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "xpr_produccion_ulexita",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idproduccion"}))
public class XProductionUlexita implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XProductionUlexita.tableGenerator")
    @Column(name = "idproduccion_ulexita", nullable = false)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproduccion", nullable = false, unique = true)
    private XProduction production;

    @Column(name = "ley_mp_bentonita", precision = 14, scale = 4)
    private BigDecimal leyMpBentonita;

    @Column(name = "ley_pt", precision = 14, scale = 4)
    private BigDecimal leyPt;

    @Column(name = "producto_granulado_tn", precision = 14, scale = 4)
    private BigDecimal productoGranuladoTn;

    @Column(name = "consumo_reproceso_tn", precision = 14, scale = 4)
    private BigDecimal consumoReprocesoTn;

    @Column(name = "reproceso_final_tn", precision = 14, scale = 4)
    private BigDecimal reprocesoFinalTn;

    @Column(name = "diluyente_total_tn", precision = 14, scale = 4)
    private BigDecimal diluyenteTotalTn;

    @Column(name = "bentonita_pct", precision = 8, scale = 4)
    private BigDecimal bentonitaPct;

    @Column(name = "consumo_mp_calc_snap", precision = 14, scale = 4)
    private BigDecimal consumoMpCalcSnap;

    // ----- Snapshots agregados en v6.0.77 -----------------------------------

    @Column(name = "merma_factor_snap", precision = 10, scale = 4)
    private BigDecimal mermaFactorSnap;

    @Column(name = "diluyente_total_snap", precision = 14, scale = 4)
    private BigDecimal diluyenteTotalSnap;

    @Column(name = "bentonita_pct_snap", precision = 8, scale = 4)
    private BigDecimal bentonitaPctSnap;

    @Column(name = "caolin_pct_snap", precision = 8, scale = 4)
    private BigDecimal caolinPctSnap;

    @Column(name = "pt_a_snap", precision = 14, scale = 4)
    private BigDecimal ptASnap;

    @Column(name = "pt_b_snap", precision = 14, scale = 4)
    private BigDecimal ptBSnap;

    @Column(name = "pt_total_bueno_snap", precision = 14, scale = 4)
    private BigDecimal ptTotalBuenoSnap;

    @Column(name = "kpa_snap", precision = 14, scale = 6)
    private BigDecimal kpaSnap;

    @Column(name = "kpm_bentonita_snap", precision = 14, scale = 6)
    private BigDecimal kpmBentonitaSnap;

    @Column(name = "kpm_merma_snap", precision = 14, scale = 6)
    private BigDecimal kpmMermaSnap;

    @Column(name = "ley_mp_recalc_snap", precision = 14, scale = 4)
    private BigDecimal leyMpRecalcSnap;

    @Column(name = "merma_snap", precision = 14, scale = 4)
    private BigDecimal mermaSnap;

    @Column(name = "merma_pct_snap", precision = 8, scale = 4)
    private BigDecimal mermaPctSnap;

    @Column(name = "snap_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date snapAt;

    @Column(name = "snap_by", length = 4)
    private String snapBy;

    // ------------------------------------------------------------------------

    @Column(name = "observacion_lab", length = 500)
    private String observacionLab;

    /** Articulo (cod_art) donde se acumula el Reproceso final (TN) al aprobar. Se copia
     *  desde la config de la linea al guardar, para que la orden conserve con que articulo
     *  se haran los movimientos de inventario aunque luego cambie la linea. */
    @Column(name = "cod_art_reproc_final", length = 20)
    private String codArtReprocFinal;

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

    public BigDecimal getLeyMpBentonita() {
        return leyMpBentonita;
    }

    public void setLeyMpBentonita(BigDecimal leyMpBentonita) {
        this.leyMpBentonita = leyMpBentonita;
    }

    public BigDecimal getLeyPt() {
        return leyPt;
    }

    public void setLeyPt(BigDecimal leyPt) {
        this.leyPt = leyPt;
    }

    public BigDecimal getProductoGranuladoTn() {
        return productoGranuladoTn;
    }

    public void setProductoGranuladoTn(BigDecimal productoGranuladoTn) {
        this.productoGranuladoTn = productoGranuladoTn;
    }

    public BigDecimal getConsumoReprocesoTn() {
        return consumoReprocesoTn;
    }

    public void setConsumoReprocesoTn(BigDecimal consumoReprocesoTn) {
        this.consumoReprocesoTn = consumoReprocesoTn;
    }

    public BigDecimal getReprocesoFinalTn() {
        return reprocesoFinalTn;
    }

    public void setReprocesoFinalTn(BigDecimal reprocesoFinalTn) {
        this.reprocesoFinalTn = reprocesoFinalTn;
    }

    public BigDecimal getDiluyenteTotalTn() {
        return diluyenteTotalTn;
    }

    public void setDiluyenteTotalTn(BigDecimal diluyenteTotalTn) {
        this.diluyenteTotalTn = diluyenteTotalTn;
    }

    public BigDecimal getBentonitaPct() {
        return bentonitaPct;
    }

    public void setBentonitaPct(BigDecimal bentonitaPct) {
        this.bentonitaPct = bentonitaPct;
    }

    public BigDecimal getConsumoMpCalcSnap() {
        return consumoMpCalcSnap;
    }

    public void setConsumoMpCalcSnap(BigDecimal consumoMpCalcSnap) {
        this.consumoMpCalcSnap = consumoMpCalcSnap;
    }

    public String getObservacionLab() {
        return observacionLab;
    }

    public void setObservacionLab(String observacionLab) {
        this.observacionLab = observacionLab;
    }

    public String getCodArtReprocFinal() {
        return codArtReprocFinal;
    }

    public void setCodArtReprocFinal(String codArtReprocFinal) {
        this.codArtReprocFinal = codArtReprocFinal;
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

    // ----- Getters/setters de snapshots v6.0.77 -----------------------------

    public BigDecimal getMermaFactorSnap() { return mermaFactorSnap; }
    public void setMermaFactorSnap(BigDecimal v) { this.mermaFactorSnap = v; }

    public BigDecimal getDiluyenteTotalSnap() { return diluyenteTotalSnap; }
    public void setDiluyenteTotalSnap(BigDecimal v) { this.diluyenteTotalSnap = v; }

    public BigDecimal getBentonitaPctSnap() { return bentonitaPctSnap; }
    public void setBentonitaPctSnap(BigDecimal v) { this.bentonitaPctSnap = v; }

    public BigDecimal getCaolinPctSnap() { return caolinPctSnap; }
    public void setCaolinPctSnap(BigDecimal v) { this.caolinPctSnap = v; }

    public BigDecimal getPtASnap() { return ptASnap; }
    public void setPtASnap(BigDecimal v) { this.ptASnap = v; }

    public BigDecimal getPtBSnap() { return ptBSnap; }
    public void setPtBSnap(BigDecimal v) { this.ptBSnap = v; }

    public BigDecimal getPtTotalBuenoSnap() { return ptTotalBuenoSnap; }
    public void setPtTotalBuenoSnap(BigDecimal v) { this.ptTotalBuenoSnap = v; }

    public BigDecimal getKpaSnap() { return kpaSnap; }
    public void setKpaSnap(BigDecimal v) { this.kpaSnap = v; }

    public BigDecimal getKpmBentonitaSnap() { return kpmBentonitaSnap; }
    public void setKpmBentonitaSnap(BigDecimal v) { this.kpmBentonitaSnap = v; }

    public BigDecimal getKpmMermaSnap() { return kpmMermaSnap; }
    public void setKpmMermaSnap(BigDecimal v) { this.kpmMermaSnap = v; }

    public BigDecimal getLeyMpRecalcSnap() { return leyMpRecalcSnap; }
    public void setLeyMpRecalcSnap(BigDecimal v) { this.leyMpRecalcSnap = v; }

    public BigDecimal getMermaSnap() { return mermaSnap; }
    public void setMermaSnap(BigDecimal v) { this.mermaSnap = v; }

    public BigDecimal getMermaPctSnap() { return mermaPctSnap; }
    public void setMermaPctSnap(BigDecimal v) { this.mermaPctSnap = v; }

    public Date getSnapAt() { return snapAt; }
    public void setSnapAt(Date v) { this.snapAt = v; }

    public String getSnapBy() { return snapBy; }
    public void setSnapBy(String v) { this.snapBy = v; }

    /** True si la orden tiene snapshots persistidos (snapshot completado al menos una vez). */
    public boolean hasSnapshots() {
        return snapAt != null;
    }
}
