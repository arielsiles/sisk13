package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
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

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "RegisterMarkAction.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rhmarcado",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "RHMark.findAll", query = "select o from RHMark o "),
                @NamedQuery(name = "RHMark.findRHMarkByMarkCode", query = "select o from RHMark o where o.marRefCard=:markCode "),
                @NamedQuery(name = "RHMark.findRHMarkByMarkCodeByInitDateByEndDate", query = "select o from RHMark o " +
                        "where o.marRefCard =:markCode and o.marDate >=:initDate and o.marDate <=:endDate"),
                @NamedQuery(name = "RHMark.findRHMarkByInitDateAndEndDate", query = "select o from RHMark o " +
                        "where o.marDate >=:initDate and o.marDate <=:endDate"),
                @NamedQuery(name = "RHMark.findRHMarkDateForPayrollGeneration", query = "select o.marDate,o.marTime from RHMark o " +
                        "where o.marRefCard =:markCode and o.marDate >=:initDate and o.marDate <=:endDate")
        }

)
@Entity
@Name("rhmark")
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "rhmarcado")
public class RHMark implements BaseModel, Comparable {

    @Id
    @Column(name = "idrhmarcado", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RegisterMarkAction.tableGenerator")
    private Long id;

    @OneToMany(mappedBy = "rHMark", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<MarkReport> markReportList = new ArrayList<MarkReport>(0);

    @Column(name = "marperid", nullable = false)
    private Integer marPerId;

    @Column(name = "marfecha", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date marDate = new Date();

    @Transient
    private Date startMarDate = new Date();

    @Transient
    private Date endMarDate = new Date();

    @Column(name = "marhora", nullable = false, updatable = false)
    @Temporal(TemporalType.TIME)
    private Date marTime = new Date();

    @Column(name = "marreftarjeta", nullable = false, length = 200)
    private String marRefCard;

    @Column(name = "marippc", nullable = false, length = 200)
    private String marIpPc;

    @Column(name = "marestado", nullable = true, length = 200)
    private String marState;

    @Column(name = "sede", nullable = false, length = 200)
    private String seat;

    @Column(name = "control", nullable = true)
    private Integer control;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "descripcion", nullable = true)
    @Lob
    private String description;

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

    public String getMarRefCard() {
        return marRefCard;
    }

    public void setMarRefCard(String marRefCard) {
        this.marRefCard = marRefCard;
    }

    public String getMarIpPc() {
        return marIpPc;
    }

    public void setMarIpPc(String marIpPc) {
        this.marIpPc = marIpPc;
    }

    public String getMarState() {
        return marState;
    }

    public void setMarState(String marState) {
        this.marState = marState;
    }


    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    // makes this object comparable in order to sort any list of this kind

    public int compareTo(Object o) {
        RHMark rhMark = (RHMark) o;
        if (this.marDate.compareTo(rhMark.marDate) == 0) {
            return this.marTime.compareTo(rhMark.marTime);
        } else {
            return this.marDate.compareTo(rhMark.marDate);
        }
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public List<MarkReport> getMarkReportList() {
        return markReportList;
    }

    public void setMarkReportList(List<MarkReport> markReportList) {
        this.markReportList = markReportList;
    }

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