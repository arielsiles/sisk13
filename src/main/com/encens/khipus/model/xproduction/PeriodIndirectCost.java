package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.xproduction.IndirectCosts;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

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
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "periodocostoindirecto",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "periodocostoindirecto")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class PeriodIndirectCost implements BaseModel {

    @Id
    @Column(name = "idperiodocostoindirecto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PeriodIndirectCost_Generator")
    private Long id;

    @Column(name = "mes", nullable = true)
    private Integer month;

    @Column(name = "procesado", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean processed = Boolean.FALSE;

    @Column(name = "contab")
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean accounting = Boolean.FALSE;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE},optional = false)
    @JoinColumn(name = "idgestion")
    private Gestion gestion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "periodIndirectCost", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<com.encens.khipus.model.xproduction.IndirectCosts> indirectCostList = new ArrayList<com.encens.khipus.model.xproduction.IndirectCosts>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
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

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public List<com.encens.khipus.model.xproduction.IndirectCosts> getIndirectCostList() {
        return indirectCostList;
    }

    public void setIndirectCostList(List<IndirectCosts> indirectCostses) {
        this.indirectCostList = indirectCostses;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean disttributionFlag) {
        this.processed = disttributionFlag;
    }

    public Boolean getAccounting() {
        return accounting;
    }

    public void setAccounting(Boolean accounting) {
        this.accounting = accounting;
    }
}
