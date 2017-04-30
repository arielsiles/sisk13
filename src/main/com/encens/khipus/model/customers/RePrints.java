package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "RePrints_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "ILVA_REIMPRESIONES",
        allocationSize = 10)

@Entity
@Table(name = "ILVA_REIMPRESIONES",schema = Constants.CASHBOX_SCHEMA)
public class RePrints implements BaseModel {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RePrints_Generator")
    private Long id;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHA_REIMP")
    private Date dateRePrint;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHA_EMISION")
    private Date dateEmission;

    @Column(name = "USUA_ID_REIMP")
    private Long idUsrRePint;

    @Column(name = "USUA_ID_EMISION")
    private Long idUsrEmission;

    @Column(name = "EST_COD",columnDefinition = "VARCHAR2(15 BYTE)")
    private String stateCod;

    @Column(name = "PI_ID",columnDefinition = "VARCHAR2(20 BYTE)")
    private String piID;

    @Column(name = "NROFACTURA",columnDefinition = "VARCHAR2(12 BYTE)")
    private Long numberInvoice;

    @Column(name = "NIT",columnDefinition = "VARCHAR2(30 BYTE)")
    private String nit;

    @Column(name = "UNIDAD_ACAD_ADM",columnDefinition = "VARCHAR2(10 BYTE)")
    private String unitAcadAdm;

    @Column(name = "PLAN_ESTUDIO",columnDefinition = "VARCHAR2(30 BYTE)")
    private String planStudy;

    @Column(name = "NUMERO_REIMP")
    private Integer numberReImprent;

    @Column(name = "ESTADO",columnDefinition = "VARCHAR2(5 BYTE)")
    private String state;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "DOSI_ID")
    private Dosage dosage;

    @Column(name = "GLOSA",columnDefinition = "VARCHAR2(200 BYTE)")
    private String gloss;

    @Column(name = "MOTIVO",columnDefinition = "VARCHAR2(200 BYTE)")
    private String reason;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "PEDIDO", columnDefinition = "VARCHAR2(15 BYTE)")
    private CustomerOrder customerOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateRePrint() {
        return dateRePrint;
    }

    public void setDateRePrint(Date dateRePrint) {
        this.dateRePrint = dateRePrint;
    }

    public Date getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(Date dateEmission) {
        this.dateEmission = dateEmission;
    }

    public Long getIdUsrRePint() {
        return idUsrRePint;
    }

    public void setIdUsrRePint(Long idUsrRePint) {
        this.idUsrRePint = idUsrRePint;
    }

    public Long getIdUsrEmission() {
        return idUsrEmission;
    }

    public void setIdUsrEmission(Long idUsrEmission) {
        this.idUsrEmission = idUsrEmission;
    }

    public String getStateCod() {
        return stateCod;
    }

    public void setStateCod(String stateCod) {
        this.stateCod = stateCod;
    }

    public String getPiID() {
        return piID;
    }

    public void setPiID(String piID) {
        this.piID = piID;
    }

    public Long getNumberInvoice() {
        return numberInvoice;
    }

    public void setNumberInvoice(Long numberInvoice) {
        this.numberInvoice = numberInvoice;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getUnitAcadAdm() {
        return unitAcadAdm;
    }

    public void setUnitAcadAdm(String unitAcadAdm) {
        this.unitAcadAdm = unitAcadAdm;
    }

    public String getPlanStudy() {
        return planStudy;
    }

    public void setPlanStudy(String planStudy) {
        this.planStudy = planStudy;
    }

    public Integer getNumberReImprent() {
        return numberReImprent;
    }

    public void setNumberReImprent(Integer numberReImprent) {
        this.numberReImprent = numberReImprent;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Dosage getDosage() {
        return dosage;
    }

    public void setDosage(Dosage dosage) {
        this.dosage = dosage;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }
}
