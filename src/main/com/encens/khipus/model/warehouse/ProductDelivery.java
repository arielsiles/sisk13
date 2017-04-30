package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.4
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ProductDelivery.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "entregaarticulo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "entregaarticulo", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class ProductDelivery implements BaseModel {
    @Id
    @Column(name = "IDENTARTICULO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductDelivery.tableGenerator")
    private Long id;

    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "NO_FACT", nullable = false, length = 10)
    @Length(max = 10)
    private String invoiceNumber;

    @Column(name = "NO_TRANS", nullable = true, length = 10)
    @Length(max = 10)
    private String transactionNumber;

    @Version
    @Column(name = "version")
    private long version;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = true, updatable = false, insertable = false),
            @JoinColumn(name = "NO_TRANS", nullable = true, updatable = false, insertable = false)
    })
    private WarehouseVoucher warehouseVoucher;

    @OneToMany(mappedBy = "productDelivery", fetch = FetchType.LAZY)
    private List<SoldProduct> soldProductList = new ArrayList<SoldProduct>(0);

    @Transient
    private String fullNameClient = "";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public WarehouseVoucher getWarehouseVoucher() {
        return warehouseVoucher;
    }

    public void setWarehouseVoucher(WarehouseVoucher warehouseVoucher) {
        this.warehouseVoucher = warehouseVoucher;
        if (null != warehouseVoucher) {
            setTransactionNumber(warehouseVoucher.getId().getTransactionNumber());
        } else {
            setTransactionNumber(null);
        }
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<SoldProduct> getSoldProductList() {
        return soldProductList;
    }

    public void setSoldProductList(List<SoldProduct> soldProductList) {
        this.soldProductList = soldProductList;
    }

    public String getFullNameClient() {
        if(this.getSoldProductList().size() > 0)
        {
            fullNameClient = this.getSoldProductList().get(0).getNames() + " " + this.getSoldProductList().get(0).getFirstName() + " " + this.getSoldProductList().get(0).getSecondName();
        }
        return fullNameClient;
    }

    public void setFullNameClient(String fullNameClient) {
        this.fullNameClient = fullNameClient;
    }
}
