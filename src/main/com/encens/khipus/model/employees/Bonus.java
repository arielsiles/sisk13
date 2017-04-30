package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Bonus.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "Bono",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "Bonus.load",
                query = "select bonus from Bonus bonus" +
                        " left join fetch bonus.smnRate smnRate" +
                        " left join fetch bonus.description description" +
                        " where bonus.id=:id"),
        @NamedQuery(name = "Bonus.findByActiveAndBonusType",
                query = "select bonus from Bonus bonus where bonus.active = :active and bonus.bonusType = :bonusType")
})

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TIPO", discriminatorType = DiscriminatorType.STRING, length = 20)
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BONO")
public class Bonus implements BaseModel {

    @Id
    @Column(name = "IDBONO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Bonus.tableGenerator")
    private Long id;

    @Length(max = 255)
    @Column(name = "NOMBRE", length = 255, nullable = false)
    @NotNull
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTASASMN")
    private SMNRate smnRate;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "IDDESCRIPCION")
    private Text description;

    @Column(name = "MONTO", precision = 13, scale = 2)
    private BigDecimal amount;

    @Column(name = "ACTIVO")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPOBONO", length = 30, nullable = false)
    @NotNull
    private BonusType bonusType;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

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

    public SMNRate getSmnRate() {
        return smnRate;
    }

    public void setSmnRate(SMNRate smnRate) {
        this.smnRate = smnRate;
    }

    public Text getDescription() {
        return description;
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public void setBonusType(BonusType bonusType) {
        this.bonusType = bonusType;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getFullName() {
        return (null != getDescription()) ? getName() + " - " + getDescription().getValue() : getName();
    }
}
