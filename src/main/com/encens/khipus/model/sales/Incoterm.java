package com.encens.khipus.model.sales;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Termino comercial internacional (Incoterm - Camara de Comercio Internacional).
 * <p/>
 * Catalogo configurable, sembrado con los 11 Incoterms 2020 (EXW, FCA, CPT, CIP,
 * DAP, DPU, DDP, FAS, FOB, CFR, CIF). En la Orden de Venta se referencia el termino
 * y se captura aparte el "lugar designado" (ej. "Santa Cruz, Bolivia").
 *
 * @author
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = "Incoterm.findByCode",
                query = "select i from Incoterm i where i.code =:code"),
        @NamedQuery(name = "Incoterm.findActive",
                query = "select i from Incoterm i where i.active =:active order by i.code")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "Incoterm.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "incoterm",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "incoterm",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "codigo"}))
public class Incoterm implements BaseModel {

    @Id
    @Column(name = "idincoterm", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Incoterm.tableGenerator")
    private Long id;

    @Column(name = "codigo", nullable = false, length = 10)
    @NotNull
    @Length(max = 10)
    private String code;

    @Column(name = "nombre", nullable = false, length = 150)
    @NotNull
    @Length(max = 150)
    private String name;

    @Column(name = "descripcion", length = 500)
    @Length(max = 500)
    private String description;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active = Boolean.TRUE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public String getFullName() {
        return code + " - " + name;
    }
}
