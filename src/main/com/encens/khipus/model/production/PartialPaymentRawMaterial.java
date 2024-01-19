package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;


@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "PartialPaymentRawMaterial.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pagoparcialacopiomp",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "pagoparcialacopiomp")
public class PartialPaymentRawMaterial implements Serializable, BaseModel {

    @Id
    @Column(name = "idpagoparcialacopiomp", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PartialPaymentRawMaterial.tableGenerator")
    private Long id;

    @Column(name = "descripcion", nullable = true)
    private String description;

    @Column(name = "monto", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpagoacopiomp", nullable = false)
    private RawMaterialPayment rawMaterialPayment;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


}
