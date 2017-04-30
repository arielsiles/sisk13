package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * This entity represents how the fixed assets were returned
 *
 * @author
 * @version 2.25
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetMaintenanceReceiptState.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "estadorecepcion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "estadorecepcion", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class FixedAssetMaintenanceReceiptState {
    @Id
    @Column(name = "idestadorecepcion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetMaintenanceReceiptState.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "tipo", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private FixedAssetMaintenanceReceiptType receiptType;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FixedAssetMaintenanceReceiptType getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(FixedAssetMaintenanceReceiptType receiptType) {
        this.receiptType = receiptType;
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

    public Boolean isSuccessType() {
        return FixedAssetMaintenanceReceiptType.SUCCESS.equals(getReceiptType());
    }
}
