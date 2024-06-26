package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "WarehouseDocumentType.findByType", query = "select w from WarehouseDocumentType w" +
                " where w.id.companyNumber=:companyNumber and w.warehouseVoucherType=:warehouseVoucherType" +
                " order by w.id.documentCode"),
        @NamedQuery(name  = "WarehouseDocumentType.findWarehouseDocumentType", query ="select w from WarehouseDocumentType w where w.warehouseVoucherType=:warehouseVoucherType ")
})
@Entity
@Table(name = "inv_tipodocs", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class WarehouseDocumentType implements BaseModel {
    @EmbeddedId
    private DocumentTypePK id = new DocumentTypePK();

    @Column(name = "descri", nullable = true, length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "tipo_vale", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private WarehouseVoucherType warehouseVoucherType;

    @Column(name = "restriccioncampo", length = 100)
    @Enumerated(EnumType.STRING)
    private WarehouseDocumentTypeFieldRestriction fieldRestriction;

    @Column(name = "ctracuentamn", length = 20)
    @Length(max = 20)
    private String contraAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", insertable = false, updatable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "ctracuentamn", insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount contraAccount;

    @Column(name = "estado", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private DocumentTypeState state;

    @Column(name = "cod_doc", nullable = false, insertable = false, updatable = false)
    private String documentCode;

    @Column(name = "desc_def", length = 100)
    @Length(max = 100)
    private String defaultDescription;

    @Column(name = "ctacosto", length = 20)
    @Length(max = 20)
    private String costAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", insertable = false, updatable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "ctacosto", insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount costAccount;

    @Version
    @Column(name = "version")
    private long version;

    public DocumentTypePK getId() {
        return id;
    }

    public void setId(DocumentTypePK id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WarehouseVoucherType getWarehouseVoucherType() {
        return warehouseVoucherType;
    }

    public void setWarehouseVoucherType(WarehouseVoucherType warehouseVoucherType) {
        this.warehouseVoucherType = warehouseVoucherType;
    }

    public WarehouseDocumentTypeFieldRestriction getFieldRestriction() {
        return fieldRestriction;
    }

    public void setFieldRestriction(WarehouseDocumentTypeFieldRestriction fieldRestriction) {
        this.fieldRestriction = fieldRestriction;
    }

    public String getContraAccountCode() {
        return contraAccountCode;
    }

    public void setContraAccountCode(String contraAccountCode) {
        this.contraAccountCode = contraAccountCode;
    }

    public CashAccount getContraAccount() {
        return contraAccount;
    }

    public void setContraAccount(CashAccount contraAccount) {
        this.contraAccount = contraAccount;
        setContraAccountCode(contraAccount != null ? contraAccount.getAccountCode() : null);
    }

    public DocumentTypeState getState() {
        return state;
    }

    public void setState(DocumentTypeState state) {
        this.state = state;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getFullName() {
        return documentCode + " - " + name;
    }

    public Boolean isInputType() {
        return WarehouseVoucherType.E.equals(getWarehouseVoucherType());
    }

    public Boolean isOutputType() {
        return WarehouseVoucherType.S.equals(getWarehouseVoucherType());
    }

    public Boolean isContraAccountDefinedByDefault() {
        return WarehouseDocumentTypeFieldRestriction.CONTRA_ACCOUNT_DEFINED_BY_DEFAULT.equals(getFieldRestriction());
    }

    public Boolean isContraAccountDefinedByUser() {
        return WarehouseDocumentTypeFieldRestriction.CONTRA_ACCOUNT_DEFINED_BY_USER.equals(getFieldRestriction());
    }

    public String getCostAccountCode() {
        return costAccountCode;
    }

    public void setCostAccountCode(String costAccountCode) {
        this.costAccountCode = costAccountCode;
    }

    public CashAccount getCostAccount() {
        return costAccount;
    }

    public void setCostAccount(CashAccount costAccount) {
        this.costAccount = costAccount;
    }
}
