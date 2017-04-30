package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for MarkReport
 *
 * @author
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "MarkReport.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "reportemarcacion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)


@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "reportemarcacion")
public class MarkReport implements BaseModel {

    @Id
    @Column(name = "idreportemarcacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MarkReport.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipomarcacion", nullable = false, updatable = false, insertable = true)
    private MarkType markType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idrhmarcado", nullable = false, updatable = false, insertable = true)
    private RHMark rHMark;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idbandahorariacontrato", nullable = false, updatable = false, insertable = true)
    private HoraryBandContract horaryBandContract;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarkType getMarkType() {
        return markType;
    }

    public void setMarkType(MarkType markType) {
        this.markType = markType;
    }

    public RHMark getRHMark() {
        return rHMark;
    }

    public void setRHMark(RHMark rHMark) {
        this.rHMark = rHMark;
    }

    public HoraryBandContract getHoraryBandContract() {
        return horaryBandContract;
    }

    public void setHoraryBandContract(HoraryBandContract horaryBandContract) {
        this.horaryBandContract = horaryBandContract;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}