package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

/**
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "TaxPayrollGenerated.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "planillafiscalgenerada",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "TaxPayrollGenerated.findByTypeAndEvaluationState",
                query = "select element from TaxPayrollGenerated element where element.configurationTaxPayroll=:configurationTaxPayroll and element.type =:type and element.evaluationState =:evaluationState"),
        @NamedQuery(name = "TaxPayrollGenerated.countByConfigurationTaxPayroll",
                query = "select count(element) from TaxPayrollGenerated element where element.configurationTaxPayroll=:configurationTaxPayroll")
})


@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "PLANILLAFISCALGENERADA", schema = Constants.KHIPUS_SCHEMA)
public class TaxPayrollGenerated implements BaseModel {
    @Id
    @Column(name = "IDPLANILLAFISCALGENERADA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TaxPayrollGenerated.tableGenerator")
    private Long id;

    @Column(name = "ESTADOVALIDACION", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaxPayrollEvaluationState evaluationState;

    @Column(name = "FECHAGENERACION", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date generationDate;

    @Column(name = "TIPO", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaxPayrollGeneratedType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCONFPLANILLAFISCAL", nullable = false, updatable = false, insertable = true)
    private ConfigurationTaxPayroll configurationTaxPayroll;

    @Version
    @Column(name = "VERSION")
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaxPayrollEvaluationState getEvaluationState() {
        return evaluationState;
    }

    public void setEvaluationState(TaxPayrollEvaluationState evaluationState) {
        this.evaluationState = evaluationState;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public TaxPayrollGeneratedType getType() {
        return type;
    }

    public void setType(TaxPayrollGeneratedType type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ConfigurationTaxPayroll getConfigurationTaxPayroll() {
        return configurationTaxPayroll;
    }

    public void setConfigurationTaxPayroll(ConfigurationTaxPayroll configurationTaxPayroll) {
        this.configurationTaxPayroll = configurationTaxPayroll;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
