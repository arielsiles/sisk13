package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Holds information about which MarkState are related to HoraryBandState
 *
 * @author
 * @version 3.0
 */
@NamedQueries({
        @NamedQuery(name = "MarkStateHoraryBandState.countByMarkStateAndNotMarkStateHoraryBandState",
                query = "select count(markStateHoraryBandState) from MarkStateHoraryBandState markStateHoraryBandState " +
                        "where markStateHoraryBandState.markState=:markState " +
                        "and markStateHoraryBandState<>:markStateHoraryBandState "),
        @NamedQuery(name = "MarkStateHoraryBandState.findByMarkStateAndHoraryBandState",
                query = "select markStateHoraryBandState from MarkStateHoraryBandState markStateHoraryBandState " +
                        "where markStateHoraryBandState.markState=:markState " +
                        "and markStateHoraryBandState.horaryBandState.id=:horaryBandStateId ")
}
)
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "MarkStateHoraryBandState.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "estadomarestadobh")
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "estadomarestadobh")
public class MarkStateHoraryBandState implements BaseModel {

    @Id
    @Column(name = "idestadomarestadobh", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MarkStateHoraryBandState.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idestadobandahoraria", referencedColumnName = "idestadobandahoraria", nullable = false)
    @NotNull
    private HoraryBandState horaryBandState;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idestadomarcado", referencedColumnName = "idestadomarcado", nullable = false)
    @NotNull
    private MarkState markState;

    @Column(name = "estado", length = 20)
    @Enumerated(EnumType.STRING)
    private MarkStateType type;

    @Column(name = "mindescuento")
    private Integer minutesDiscount;

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

    public HoraryBandState getHoraryBandState() {
        return horaryBandState;
    }

    public void setHoraryBandState(HoraryBandState horaryBandState) {
        this.horaryBandState = horaryBandState;
    }

    public MarkState getMarkState() {
        return markState;
    }

    public void setMarkState(MarkState markState) {
        this.markState = markState;
    }

    public MarkStateType getType() {
        return type;
    }

    public void setType(MarkStateType type) {
        this.type = type;
    }

    public Integer getMinutesDiscount() {
        return minutesDiscount;
    }

    public void setMinutesDiscount(Integer minutesDiscount) {
        this.minutesDiscount = minutesDiscount;
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