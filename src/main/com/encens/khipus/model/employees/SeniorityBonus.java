package com.encens.khipus.model.employees;

import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@NamedQueries({
        @NamedQuery(name = "SeniorityBonus.findActiveBonus",
                query = "select seniorityBonus from SeniorityBonus seniorityBonus where seniorityBonus.active =:active")
})

@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BONOANTIGUEDAD")
@DiscriminatorValue("BONO_ANTIGUEDAD")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "IDBONOANTIGUEDAD", referencedColumnName = "IDBONO")
})
public class SeniorityBonus extends Bonus {

    @Column(name = "FECHAINICIO", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "FECHAFIN", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "IDBONOANTIGUEDAD", referencedColumnName = "IDBONOANTIGUEDAD", nullable = false)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<SeniorityBonusDetail> details = new ArrayList<SeniorityBonusDetail>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<SeniorityBonusDetail> getDetails() {
        return details;
    }

    public void setDetails(List<SeniorityBonusDetail> details) {
        this.details = details;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
