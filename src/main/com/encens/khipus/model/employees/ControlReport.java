package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for controlReport
 *
 * @author
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ControlReport.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "reportecontrol",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "ControlReport.findAll", query = "select o from ControlReport o ")
        }

)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "reportecontrol")
public class ControlReport implements BaseModel {

    @Id
    @Column(name = "idreportecontrol", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ControlReport.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idbandahorariac", nullable = false, updatable = false, insertable = true)
    private HoraryBandContract horaryBandContract;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanillagenerada", nullable = false, updatable = false, insertable = true)
    private GeneratedPayroll generatedPayroll;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha")
    private Date date;

    @Temporal(TemporalType.TIME)
    @Column(name = "marcinicio")
    private Date initMark;

    @Temporal(TemporalType.TIME)
    @Column(name = "marcfin")
    private Date endMark;

    @Column(name = "mindescuento")
    private Integer minutesDiscount;

    @Column(name = "importedescuento", precision = 13, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "marcaciones")
    @Lob
    private String marks;

    @Column(name = "numerofaltabandas")
    private Integer numberBandAbsences;

    @Column(name = "faltabanda")
    private Integer bandAbsence;

    @Column(name = "descuentofaltabanda", precision = 13, scale = 2)
    private BigDecimal bandAbsenceDiscount;

    @Column(name = "sueldoporbanda", precision = 13, scale = 2)
    private BigDecimal perBandSalary;

    @Column(name = "sueldoporfin", precision = 13, scale = 2)
    private BigDecimal perMinuteSalary;

    @Column(name = "importeminutostrabajo", precision = 13, scale = 2)
    private BigDecimal performanceMinuteAmount;

    @Column(name = "minutostrabajados")
    private Integer performanceMinutes;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;


    /*0 without control,    1 whith control,    2 horary change(without control)*/
    @Column(name = "tipocontrol")
    private Integer controlType;

    public Integer getControlType() {
        return controlType;
    }

    public void setControlType(Integer controlType) {
        this.controlType = controlType;
    }


    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberBandAbsences() {
        return numberBandAbsences;
    }

    public void setNumberBandAbsences(Integer numberBandAbsences) {
        this.numberBandAbsences = numberBandAbsences;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public HoraryBandContract getHoraryBandContract() {
        return horaryBandContract;
    }

    public void setHoraryBandContract(HoraryBandContract horaryBandContract) {
        this.horaryBandContract = horaryBandContract;
    }

    public Date getInitMark() {
        return initMark;
    }

    public void setInitMark(Date initMark) {
        this.initMark = initMark;
    }

    public Date getEndMark() {
        return endMark;
    }

    public void setEndMark(Date endMark) {
        this.endMark = endMark;
    }

    public Integer getMinutesDiscount() {
        return minutesDiscount;
    }

    public void setMinutesDiscount(Integer minutesDiscount) {
        this.minutesDiscount = minutesDiscount;
    }


    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public Integer getBandAbsence() {
        return bandAbsence;
    }

    public void setBandAbsence(Integer bandAbsence) {
        this.bandAbsence = bandAbsence;
    }

    public Integer getPerformanceMinutes() {
        return performanceMinutes;
    }

    public void setPerformanceMinutes(Integer performanceMinutes) {
        this.performanceMinutes = performanceMinutes;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getBandAbsenceDiscount() {
        return bandAbsenceDiscount;
    }

    public void setBandAbsenceDiscount(BigDecimal bandAbsenceDiscount) {
        this.bandAbsenceDiscount = bandAbsenceDiscount;
    }

    public BigDecimal getPerBandSalary() {
        return perBandSalary;
    }

    public void setPerBandSalary(BigDecimal perBandSalary) {
        this.perBandSalary = perBandSalary;
    }

    public BigDecimal getPerMinuteSalary() {
        return perMinuteSalary;
    }

    public void setPerMinuteSalary(BigDecimal perMinuteSalary) {
        this.perMinuteSalary = perMinuteSalary;
    }

    public BigDecimal getPerformanceMinuteAmount() {
        return performanceMinuteAmount;
    }

    public void setPerformanceMinuteAmount(BigDecimal performanceMinuteAmount) {
        this.performanceMinuteAmount = performanceMinuteAmount;
    }
}