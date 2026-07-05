package com.encens.khipus.model.sales;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Nota predefinida configurable para el recuadro final de la Orden de Venta.
 * <p/>
 * Es texto libre sin relacion; en la orden se seleccionan una o mas notas de este
 * catalogo. Se pueden agregar/editar en cualquier momento.
 *
 * @author
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = "SalesOrderNote.findActive",
                query = "select n from SalesOrderNote n where n.active =:active order by n.sortOrder, n.id")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "SalesOrderNote.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "ordenventa_nota",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "ordenventa_nota")
public class SalesOrderNote implements BaseModel {

    @Id
    @Column(name = "idordenventa_nota", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalesOrderNote.tableGenerator")
    private Long id;

    @Column(name = "texto", nullable = false, length = 1000)
    @NotNull
    @Length(max = 1000)
    private String text;

    @Column(name = "orden")
    private Integer sortOrder = 0;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    /** Vista corta del texto para listas (primeros 80 caracteres). */
    public String getShortText() {
        if (text == null) {
            return "";
        }
        return text.length() > 80 ? text.substring(0, 80) + "..." : text;
    }
}
