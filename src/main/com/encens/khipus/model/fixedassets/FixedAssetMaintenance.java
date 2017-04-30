package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This entity stands for a maintenance for fixed assets.
 *
 * @author
 * @version 2.25
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetMaintenance.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rol",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "mantenimiento")
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
public class FixedAssetMaintenance implements BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetMaintenance.tableGenerator")
    @Column(name = "idmant")
    private Long id;

    @Column(name = "fechaentrega", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date deliveryDate;

    @Column(name = "fecharecepcion", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date receiptDate;

    @Column(name = "fechaestimadarecepcion", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date estimatedReceiptDate;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "iddescripentrega", nullable = false)
    private Text deliveryDescription;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "iddescriprecepcion", nullable = true)
    private Text receiptDescription;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private FixedAssetMaintenanceState state;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idestadorecepcion", nullable = true)
    private FixedAssetMaintenanceReceiptState receiptState;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idmoneda")
    private Currency currency;

    @Column(name = "monto", precision = 16, scale = 6)
    private BigDecimal amount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
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

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public Date getEstimatedReceiptDate() {
        return estimatedReceiptDate;
    }

    public void setEstimatedReceiptDate(Date estimatedReceiptDate) {
        this.estimatedReceiptDate = estimatedReceiptDate;
    }

    public Text getDeliveryDescription() {
        return deliveryDescription;
    }

    public void setDeliveryDescription(Text deliveryDescription) {
        this.deliveryDescription = deliveryDescription;
    }

    public Text getReceiptDescription() {
        return receiptDescription;
    }

    public void setReceiptDescription(Text receiptDescription) {
        this.receiptDescription = receiptDescription;
    }

    public FixedAssetMaintenanceState getState() {
        return state;
    }

    public void setState(FixedAssetMaintenanceState state) {
        this.state = state;
    }

    public FixedAssetMaintenanceReceiptState getReceiptState() {
        return receiptState;
    }

    public void setReceiptState(FixedAssetMaintenanceReceiptState receiptState) {
        this.receiptState = receiptState;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
}
