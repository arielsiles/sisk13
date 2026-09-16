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

    /**
     * La banda horaria que origino la fila. <b>Puede ser nula</b>: con el motor nuevo no hay
     * bandas, la jornada sale de las cuatro capas de resolucion. Se conserva para las planillas
     * viejas y para las empresas que siguen con el modelo de bandas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idbandahorariac", updatable = false, insertable = true)
    private HoraryBandContract horaryBandContract;

    /**
     * El contrato que pago ese dia. Sin banda no habia forma de saber de quien era la fila: el
     * empleado se alcanzaba navegando banda -> puesto -> contrato -> empleado, y con la banda en
     * nulo la fila quedaba huerfana.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", updatable = false, insertable = true)
    private com.encens.khipus.model.finances.Contract contract;

    /** Lo que la jornada exigia ese dia. Es contra esto que se midio el atraso. */
    @Column(name = "horainicioprog")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduledStart;

    @Column(name = "horafinprog")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduledEnd;

    /** De cual de las cuatro capas salio la jornada: excepcion, cronograma, horario fijo. */
    @Column(name = "origenjornada", length = 20)
    private String journeySource;

    /**
     * Por que se perdio el dia: AUSENCIA, REGISTRO -falta una marca- o SINGOCE. Vacio si no se
     * perdio. Es lo que permite explicar por que a uno se le descuenta el doble y a otro no.
     */
    @Column(name = "clasefalta", length = 20)
    private String absenceClass;

    /** Cuanto dia se perdio: 0, 0.5 o 1. Sin duplicar: el x2 es del calculo, no del hecho. */
    @Column(name = "diasfalta", precision = 4, scale = 2)
    private BigDecimal absenceDays;

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

    public com.encens.khipus.model.finances.Contract getContract() {
        return contract;
    }

    public void setContract(com.encens.khipus.model.finances.Contract contract) {
        this.contract = contract;
    }

    public Date getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(Date scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public Date getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(Date scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public String getJourneySource() {
        return journeySource;
    }

    public void setJourneySource(String journeySource) {
        this.journeySource = journeySource;
    }

    public String getAbsenceClass() {
        return absenceClass;
    }

    public void setAbsenceClass(String absenceClass) {
        this.absenceClass = absenceClass;
    }

    public BigDecimal getAbsenceDays() {
        return absenceDays;
    }

    /**
     * Las claves i18n de las dos columnas que guardan un codigo.
     * <p/>
     * Se arman aca y no en la pantalla porque el prefijo y el codigo tienen que viajar juntos: si
     * la pantalla los concatenara, cambiar un codigo obligaria a acordarse de la pantalla.
     */
    public String getAbsenceClassKey() {
        return null == absenceClass ? null : "ControlReport.absenceClass." + absenceClass;
    }

    /**
     * El horario que se le exigia ese dia, venga del motor que venga.
     * <p/>
     * El motor de jornadas lo guarda en la fila; el de bandas lo tiene en la banda. La pantalla
     * pregunta una sola cosa y no tiene que saber cual de los dos escribio la fila.
     */
    public Date getShownStart() {
        if (null != scheduledStart) {
            return scheduledStart;
        }
        return (null == horaryBandContract || null == horaryBandContract.getHoraryBand())
                ? null : horaryBandContract.getHoraryBand().getInitHour();
    }

    public Date getShownEnd() {
        if (null != scheduledEnd) {
            return scheduledEnd;
        }
        return (null == horaryBandContract || null == horaryBandContract.getHoraryBand())
                ? null : horaryBandContract.getHoraryBand().getEndHour();
    }

    /** La del origen sale del propio enum, para no mantener dos juegos de claves. */
    public String getJourneySourceKey() {
        if (null == journeySource) {
            return null;
        }
        try {
            return com.encens.khipus.service.employees.JourneyResolverService.JourneySource
                    .valueOf(journeySource).getResourceKey();
        } catch (IllegalArgumentException e) {
            /* Un valor viejo o desconocido no tiene que romper la pantalla del reporte. */
            return null;
        }
    }

    public void setAbsenceDays(BigDecimal absenceDays) {
        this.absenceDays = absenceDays;
    }
}