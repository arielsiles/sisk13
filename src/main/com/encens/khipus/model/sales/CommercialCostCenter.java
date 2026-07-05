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
 * Centro de costo del area comercial (independiente de la contabilidad legacy
 * {@code cg_cencos}). Catalogo configurable usado por linea en la Orden de Venta
 * (columna COST CENTER, ej. "CC-01").
 *
 * @author
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = "CommercialCostCenter.findByCode",
                query = "select cc from CommercialCostCenter cc where cc.code =:code"),
        @NamedQuery(name = "CommercialCostCenter.findActive",
                query = "select cc from CommercialCostCenter cc where cc.active =:active order by cc.code")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "CommercialCostCenter.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "centrocosto_comercial",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "centrocosto_comercial",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "codigo"}))
public class CommercialCostCenter implements BaseModel {

    @Id
    @Column(name = "idcentrocosto_comercial", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CommercialCostCenter.tableGenerator")
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    @NotNull
    @Length(max = 20)
    private String code;

    @Column(name = "descripcion", nullable = false, length = 150)
    @NotNull
    @Length(max = 150)
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
        return code + " - " + description;
    }
}
