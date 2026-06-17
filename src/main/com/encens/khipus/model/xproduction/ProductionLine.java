package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ProductionLine.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_linea",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "xpr_linea", uniqueConstraints = @UniqueConstraint(columnNames = {"idlinea"}))
public class ProductionLine implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductionLine.tableGenerator")
    @Column(name = "idlinea", nullable = false)
    private Long id;

    @Column(name = "codigo")
    private String code;

    @Column(name = "nombre", nullable = false, length = 255)
    private String name;

    @Column(name = "report_template_code", length = 20)
    private String reportTemplateCode;

    @Column(name = "cod_art_mp_principal", length = 20)
    private String codArtMpPrincipal;

    @Column(name = "cod_art_pt_a", length = 20)
    private String codArtPtA;

    @Column(name = "cod_art_pt_b", length = 20)
    private String codArtPtB;

    @Column(name = "cod_art_diluy_bent", length = 20)
    private String codArtDiluyBent;

    @Column(name = "cod_art_diluy_caolin", length = 20)
    private String codArtDiluyCaolin;

    @Column(name = "merma_factor")
    private BigDecimal mermaFactor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReportTemplateCode() {
        return reportTemplateCode;
    }

    public void setReportTemplateCode(String reportTemplateCode) {
        this.reportTemplateCode = reportTemplateCode;
    }

    public String getCodArtMpPrincipal() {
        return codArtMpPrincipal;
    }

    public void setCodArtMpPrincipal(String codArtMpPrincipal) {
        this.codArtMpPrincipal = codArtMpPrincipal;
    }

    public String getCodArtPtA() {
        return codArtPtA;
    }

    public void setCodArtPtA(String codArtPtA) {
        this.codArtPtA = codArtPtA;
    }

    public String getCodArtPtB() {
        return codArtPtB;
    }

    public void setCodArtPtB(String codArtPtB) {
        this.codArtPtB = codArtPtB;
    }

    public String getCodArtDiluyBent() {
        return codArtDiluyBent;
    }

    public void setCodArtDiluyBent(String codArtDiluyBent) {
        this.codArtDiluyBent = codArtDiluyBent;
    }

    public String getCodArtDiluyCaolin() {
        return codArtDiluyCaolin;
    }

    public void setCodArtDiluyCaolin(String codArtDiluyCaolin) {
        this.codArtDiluyCaolin = codArtDiluyCaolin;
    }

    public BigDecimal getMermaFactor() {
        return mermaFactor;
    }

    public void setMermaFactor(BigDecimal mermaFactor) {
        this.mermaFactor = mermaFactor;
    }

    /**
     * Tipo de linea resuelto desde report_template_code. {@code null} = linea GENERAL.
     */
    public ProductionLineType getLineType() {
        return ProductionLineType.fromCode(reportTemplateCode);
    }

    public boolean isUlexitaTemplate() {
        return ProductionLineType.ULEXITA == getLineType();
    }

    public boolean isBaritinaTemplate() {
        return ProductionLineType.BARITINA == getLineType();
    }
}
