package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for Customer categories
 *
 * @author:
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CustomerCategory.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "categoriacliente",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "categoriacliente", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class CustomerCategory implements BaseModel {

    @Id
    @Column(name = "idcategoriacliente", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CustomerCategory.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private CustomerCategoryType type;


    @OneToMany(mappedBy = "customerCategory", fetch = FetchType.LAZY)
    private List<PriceItem> priceItemList = new ArrayList<PriceItem>(0);

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


    public CustomerCategoryType getType() {
        return type;
    }

    public void setType(CustomerCategoryType type) {
        this.type = type;
    }

    /*public CashBoxType getCashBoxType() {
        return cashBoxType;
    }

    public void setCashBoxType(CashBoxType cashBoxType) {
        this.cashBoxType = cashBoxType;
    }*/

    public List<PriceItem> getPriceItemList() {
        return priceItemList;
    }

    public void setPriceItemList(List<PriceItem> priceItemList) {
        this.priceItemList = priceItemList;
    }
}
