package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
@NamedQueries({
        @NamedQuery(name = "DiscountRuleRange.findByDiscountRule",
                query = "select discountRuleRange from DiscountRuleRange discountRuleRange" +
                        " left join discountRuleRange.discountRule discountRule" +
                        " where discountRule.id =:discountRuleId "),
        @NamedQuery(name = "DiscountRuleRange.findByDiscountRuleOrderBySequence",
                query = "select discountRuleRange from DiscountRuleRange discountRuleRange" +
                        " left join discountRuleRange.discountRule discountRule" +
                        " where discountRule.id =:discountRuleId " +
                        " order by discountRuleRange.sequence desc "),
        @NamedQuery(name = "DiscountRuleRange.findByRangeOverlap",
                query = "select discountRuleRange from DiscountRuleRange discountRuleRange" +
                        " left join discountRuleRange.discountRule discountRule" +
                        " where discountRuleRange.id <> :discountRuleRangeId " +
                        " and discountRule.id =:discountRuleId " +
                        " and (discountRuleRange.endRange >= :initRange or  discountRuleRange.endRange IS NULL)")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "DiscountRuleRange.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rangoregladescuento",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "rangoregladescuento", schema = Constants.KHIPUS_SCHEMA)
public class DiscountRuleRange implements BaseModel {

    @Id
    @Column(name = "idrangoregladescuento", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DiscountRuleRange.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotEmpty
    @Length(max = 100)
    private String name;

    @Column(name = "monto", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(name = "rangoinicial")
    private Integer initRange;

    @Column(name = "rangofinal", nullable = true)
    private Integer endRange;

    @Column(name = "secuencia", nullable = false)
    @NotNull
    private Long sequence;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idregladescuento", nullable = false)
    private DiscountRule discountRule;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getInitRange() {
        return initRange;
    }

    public void setInitRange(Integer initRange) {
        this.initRange = initRange;
    }

    public Integer getEndRange() {
        return endRange;
    }

    public void setEndRange(Integer endRange) {
        this.endRange = endRange;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public DiscountRule getDiscountRule() {
        return discountRule;
    }

    public void setDiscountRule(DiscountRule discountRule) {
        this.discountRule = discountRule;
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
