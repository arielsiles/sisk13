package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Gestion;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "PeriodIndirectCost_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "PERIODOCOSTOINDIRECTO",
        allocationSize = 10)

@Entity
@Table(name = "PERIODOCOSTOINDIRECTO")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class PeriodIndirectCost implements BaseModel {

    @Id
    @Column(name = "IDPERIODOCOSTOINDIRECTO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PeriodIndirectCost_Generator")
    private Long id;

    @Column(name = "MES", nullable = true)
    private Integer month;

    @Column(name = "DIA", nullable = true)
    private Integer day;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE},optional = false)
    @JoinColumn(name = "IDGESTION")
    private Gestion gestion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "periodIndirectCost", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<IndirectCosts> indirectCostses = new ArrayList<IndirectCosts>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public List<IndirectCosts> getIndirectCostses() {
        return indirectCostses;
    }

    public void setIndirectCostses(List<IndirectCosts> indirectCostses) {
        this.indirectCostses = indirectCostses;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
