package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for RegisterMarkAction
 *
 * @author
 */

@NamedQueries(
        {
                @NamedQuery(name = "RH_Mark.findAll", query = "select o from RH_Mark o "),
                @NamedQuery(name = "RH_Mark.findRHMarkByMarkCode", query = "select o from RH_Mark o where o.marPerId=:markCode "),
                @NamedQuery(name = "RH_Mark.findRHMarkByMarkCodeByInitDateByEndDate", query = "select o from RH_Mark o " +
                        "where o.marPerId =:markCode and o.marDate >=:initDate and o.marDate <=:endDate"),
                @NamedQuery(name = "RH_Mark.findRHMarkByInitDateAndEndDate", query = "select o from RH_Mark o " +
                        "where o.marDate >=:initDate and o.marDate <=:endDate"),
                @NamedQuery(name = "RH_Mark.findRHMarkDateForPayrollGeneration", query = "select o.marDate,o.marTime from RH_Mark o " +
                        "where o.marPerId =:markCode and o.marDate >=:initDate and o.marDate <=:endDate")
        }

)
@Entity
@Name("rh_mark")
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "rh_marcado")
public class RH_Mark implements BaseModel, Comparable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "idrhmarcado", nullable = false)
    private Long id;

    /*@OneToMany(mappedBy = "rHMark", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<MarkReport> markReportList = new ArrayList<MarkReport>(0);*/

    @Column(name = "control", nullable = true)
    private Integer control;

    @Column(name = "descripcion", nullable = true)
    @Lob
    private String description;

    @Column(name = "marfecha", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date marDate = new Date();

    @Column(name = "marperid", nullable = false)
    private Integer marPerId;

    /*@Column(name = "marreftarjeta", nullable = false, length = 200)
    private String marRefCard;*/

    @Column(name = "marhora", nullable = false, updatable = false)
    @Temporal(TemporalType.TIME)
    private Date marTime = new Date();

    @Transient
    private Date startMarDate = new Date();

    @Transient
    private Date endMarDate = new Date();


    @Column(name = "marippc", nullable = false, length = 200)
    private String marIpPc;

    /*@Column(name = "sede", nullable = false, length = 200)
    private String seat;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;*/


    public Integer getControl() {
        return control;
    }

    public void setControl(Integer control) {
        this.control = control;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMarPerId() {
        return marPerId;
    }

    public void setMarPerId(Integer marPerId) {
        this.marPerId = marPerId;
    }

    public Date getMarDate() {
        this.marDate = new Date();
        return  this.marDate;
    }

    public void setMarDate(Date marDate) {
        this.marDate = marDate;
    }

    public Date getMarTime() {
       /* Date date = new Date();
        if (null == marTime) {
            marTime = new Date();
            return marTime;
        }*/
        this.marTime = new Date();
        return marTime;
    }

    public void setMarTime(Date marTime) {
        this.marTime = marTime;
    }

    /*public String getMarRefCard() {
        return marRefCard;
    }

    public void setMarRefCard(String marRefCard) {
        this.marRefCard = marRefCard;
    }*/

    public String getMarIpPc() {
        return marIpPc;
    }

    public void setMarIpPc(String marIpPc) {
        this.marIpPc = marIpPc;
    }

    /*public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }*/

    // makes this object comparable in order to sort any list of this kind

    public int compareTo(Object o) {
        RH_Mark rhMark = (RH_Mark) o;
        if (this.marDate.compareTo(rhMark.marDate) == 0) {
            return this.marTime.compareTo(rhMark.marTime);
        } else {
            return this.marDate.compareTo(rhMark.marDate);
        }
    }

    /*public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }*/

    /*public List<MarkReport> getMarkReportList() {
        return markReportList;
    }

    public void setMarkReportList(List<MarkReport> markReportList) {
        this.markReportList = markReportList;
    }*/

    public Date getStartMarDate() {
        return startMarDate;
    }

    public void setStartMarDate(Date startMarDate) {
        this.startMarDate = startMarDate;
    }

    public Date getEndMarDate() {
        return endMarDate;
    }

    public void setEndMarDate(Date endMarDate) {
        this.endMarDate = endMarDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}