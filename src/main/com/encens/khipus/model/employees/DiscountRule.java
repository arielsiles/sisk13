package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@NamedQueries({
        @NamedQuery(name = "DiscountRule.findActiveByGestionAndBusinessUnitAndJobCategory",
                query = "select distinct discountRule from DiscountRule discountRule " +
                        "left join fetch discountRule.discountRuleRangeList discountRuleRange " +
                        "where discountRule.gestion=:gestion " +
                        "and discountRule.businessUnit =:businessUnit " +
                        "and discountRule.jobCategory =:jobCategory " +
                        "and discountRule.discountRuleType=:discountRuleType " +
                        "and discountRule.active=:active "),
        @NamedQuery(name = "DiscountRule.findBusinessUnitGlobalActiveDiscountRuleByGestion",
                query = "select distinct discountRule from DiscountRule discountRule " +
                        "left join fetch discountRule.discountRuleRangeList discountRuleRange " +
                        "where discountRule.gestion=:gestion " +
                        "and discountRule.businessUnit =:businessUnit  " +
                        "and discountRule.jobCategory is null " +
                        "and discountRule.discountRuleType=:discountRuleType " +
                        "and discountRule.active=:active "),
        @NamedQuery(name = "DiscountRule.findGlobalActiveDiscountRuleByGestion",
                query = "select distinct discountRule from DiscountRule discountRule " +
                        "left join fetch discountRule.discountRuleRangeList discountRuleRange " +
                        "where discountRule.gestion=:gestion " +
                        "and discountRule.businessUnit is null " +
                        "and discountRule.jobCategory is null " +
                        "and discountRule.discountRuleType=:discountRuleType " +
                        "and discountRule.active=:active "),
        @NamedQuery(name = "DiscountRule.findActiveNationalSolidaryAfpDiscountRule",
                query = "select distinct discountRule from DiscountRule discountRule " +
                        "left join fetch discountRule.discountRuleRangeList discountRuleRange " +
                        "where discountRule.discountRuleType=:discountRuleType " +
                        "and discountRule.active=:active ")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "DiscountRule.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "REGLADESCUENTO",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "REGLADESCUENTO", schema = Constants.KHIPUS_SCHEMA)
public class DiscountRule implements BaseModel {

    @Id
    @Column(name = "IDREGLADESCUENTO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DiscountRule.tableGenerator")
    private Long id;

    @Column(name = "ACTIVO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean active = Boolean.FALSE;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    @NotEmpty
    @Length(max = 100)
    private String name;

    @Column(name = "DESCRIPCION", nullable = false, length = 1000)
    @NotEmpty
    @Length(max = 1000)
    private String description;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCATEGORIAPUESTO", nullable = true)
    private JobCategory jobCategory;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDGESTION", nullable = true)
    private Gestion gestion;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", nullable = true)
    private BusinessUnit businessUnit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDMONEDA", nullable = true)
    private Currency currency;

    @Column(name = "TIPODESCUENTO", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private DiscountRuleType discountRuleType;

    @Column(name = "TIPOINTERVALO", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private IntervalType intervalType;

    @Column(name = "TIPOUNIDADDESCUENTO", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private DiscountUnitType discountUnitType;

    @Column(name = "TIPORANGO", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private DiscountRuleRangeType discountRuleRangeType;

    @OneToMany(mappedBy = "discountRule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DiscountRuleRange> discountRuleRangeList = new ArrayList<DiscountRuleRange>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    //todo should use Finances currency type
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public DiscountUnitType getDiscountUnitType() {
        return discountUnitType;
    }

    public void setDiscountUnitType(DiscountUnitType discountUnitType) {
        this.discountUnitType = discountUnitType;
    }

    public DiscountRuleRangeType getDiscountRuleRangeType() {
        return discountRuleRangeType;
    }

    public void setDiscountRuleRangeType(DiscountRuleRangeType discountRuleRangeType) {
        this.discountRuleRangeType = discountRuleRangeType;
    }

    public List<DiscountRuleRange> getDiscountRuleRangeList() {
        return discountRuleRangeList;
    }

    public void setDiscountRuleRangeList(List<DiscountRuleRange> discountRuleRangeList) {
        this.discountRuleRangeList = discountRuleRangeList;
    }

    public DiscountRuleType getDiscountRuleType() {
        return discountRuleType;
    }

    public void setDiscountRuleType(DiscountRuleType discountRuleType) {
        this.discountRuleType = discountRuleType;
    }

    public IntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(IntervalType intervalType) {
        this.intervalType = intervalType;
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
