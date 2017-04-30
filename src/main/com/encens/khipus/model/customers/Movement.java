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

@TableGenerator(name = "Movement_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "ILVA_MOVIMIENTOS",
        allocationSize = 10)

@Entity
@Table(name = "ILVA_MOVIMIENTOS",schema = Constants.CASHBOX_SCHEMA)
//@Filter(name = "companyFilter")
//@EntityListeners(CompanyListener.class)
public class Movement implements BaseModel {

    /*@EmbeddedId
    private CustomerOrderPK id = new CustomerOrderPK();*/
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Movement_Generator")
    private Long id;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHA")
    private Date date;

    @Column(name = "GLOSA", columnDefinition = "VARCHAR2(100 BYTE)")
    private String gloss;

    @Column(name = "NRORECIBO", columnDefinition = "VARCHAR2(30 BYTE)")
    private String numberVoucher;

    @Column(name = "ESTADO", columnDefinition = "VARCHAR2(1 BYTE)")
    private String type;

    @Column(name = "OBSERVACION", columnDefinition = "VARCHAR2(300 BYTE)")
    private String observation;

    @Column(name = "CASOESPECIAL", columnDefinition = "VARCHAR2(1 BYTE)")
    private String caseEspecial;

    @Column(name = "MONTOTESO", columnDefinition = "DECIMAL(10,2)")
    private Double mountTeso;

    @Column(name = "MONTOCUST", columnDefinition = "DECIMAL(10,2)")
    private Double mountCust;

    @Column(name = "CUEN_ID")
    private Long accountID;

    @Column(name = "USUA_ID")
    private Long usrID;

    @Column(name = "EST_COD", columnDefinition = "VARCHAR2(15 BYTE)")
    private String estCod;

    @Column(name = "MONEDA", columnDefinition = "VARCHAR2(1 BYTE)")
    private String coin;

    @Column(name = "PI_ID", columnDefinition = "VARCHAR2(20 BYTE)")
    private Long piID;

    @Column(name = "EN_UN_RECIBO", columnDefinition = "VARCHAR2(1 BYTE)")
    private String inVoucher;

    @Column(name = "NROPREIMPRESO", columnDefinition = "VARCHAR2(30 BYTE)")
    private String numberPrePrint;

    @Column(name = "MOTIVO", columnDefinition = "VARCHAR2(300 BYTE)")
    private String motive;

    @Column(name = "NROFACTURA", columnDefinition = "VARCHAR2(12 BYTE)")
    private Long numberInvoice;

    @Column(name = "CODIGOCONTROL", columnDefinition = "VARCHAR2(256 BYTE)")
    private String codControl;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "DOSI_ID")
    private Dosage dosage;

    @Column(name = "NIT", columnDefinition = "VARCHAR2(30 BYTE)")
    private String nit;

    @Column(name = "CANT")
    private Integer mount;

    @Column(name = "DESCUENTO", columnDefinition = "DECIMAL(10,0)")
    private Double discount;

    @Column(name = "SOLIDARIDAD", columnDefinition = "VARCHAR2(1 BYTE)")
    private String solidarity;

    @Column(name = "MONTODESC", columnDefinition = "DECIMAL(10,2)")
    private Double mountDiscount;

    @Column(name = "DESCREAL", columnDefinition = "DECIMAL(10,2)")
    private Double descreal;

    @Column(name = "MONTONETO", columnDefinition = "DECIMAL(10,2)")
    private Double mountNeto;

    @Column(name = "TIPO_PAGO", columnDefinition = "VARCHAR2(10 BYTE)")
    private String typePay;

    @Column(name = "UNIDAD_ACAD_ADM", columnDefinition = "VARCHAR2(10 BYTE)")
    private String unitAcadAdm;

    @Column(name = "PLAN_ESTUDIO", columnDefinition = "VARCHAR2(30 BYTE)")
    private String studyPlan;

    @Column(name = "TIPO_CAMBIO", columnDefinition = "DECIMAL(10,2)")
    private Double typeChange;

    @Column(name = "TOTAL_FACTURA", columnDefinition = "DECIMAL(10,2)")
    private Double totalInvoice;

    @Column(name = "NRO_CREDITOS", columnDefinition = "DECIMAL(22,0)")
    private Double numberCredit;

    @Column(name = "DESC_PEDIDO", columnDefinition = "VARCHAR2(500 BYTE)")
    private String descrOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "PEDIDO", columnDefinition = "VARCHAR2(50 BYTE)")
    private CustomerOrder customerOrderMovement;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHA_REGISTRO")
    private Date dateRecord;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public String getNumberVoucher() {
        return numberVoucher;
    }

    public void setNumberVoucher(String numberVoucher) {
        this.numberVoucher = numberVoucher;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getCaseEspecial() {
        return caseEspecial;
    }

    public void setCaseEspecial(String caseEspecial) {
        this.caseEspecial = caseEspecial;
    }

    public Double getMountTeso() {
        return mountTeso;
    }

    public void setMountTeso(Double mountTeso) {
        this.mountTeso = mountTeso;
    }

    public Double getMountCust() {
        return mountCust;
    }

    public void setMountCust(Double mountCust) {
        this.mountCust = mountCust;
    }

    public Long getAccountID() {
        return accountID;
    }

    public void setAccountID(Long accountID) {
        this.accountID = accountID;
    }

    public Long getUsrID() {
        return usrID;
    }

    public void setUsrID(Long usrID) {
        this.usrID = usrID;
    }

    public String getEstCod() {
        return estCod;
    }

    public void setEstCod(String estCod) {
        this.estCod = estCod;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public void setPiID(Long piID) {
        this.piID = piID;
    }

    public String getInVoucher() {
        return inVoucher;
    }

    public void setInVoucher(String inVoucher) {
        this.inVoucher = inVoucher;
    }

    public String getNumberPrePrint() {
        return numberPrePrint;
    }

    public void setNumberPrePrint(String numberPrePrint) {
        this.numberPrePrint = numberPrePrint;
    }

    public String getMotive() {
        return motive;
    }

    public void setMotive(String motive) {
        this.motive = motive;
    }

    public Long getNumberInvoice() {
        return numberInvoice;
    }

    public void setNumberInvoice(Long numberInvoice) {
        this.numberInvoice = numberInvoice;
    }

    public String getCodControl() {
        return codControl;
    }

    public void setCodControl(String codControl) {
        this.codControl = codControl;
    }

    public Dosage getDosage() {
        return dosage;
    }

    public void setDosage(Dosage dosage) {
        this.dosage = dosage;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public Integer getMount() {
        return mount;
    }

    public void setMount(Integer mount) {
        this.mount = mount;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getSolidarity() {
        return solidarity;
    }

    public void setSolidarity(String solidarity) {
        this.solidarity = solidarity;
    }

    public Double getMountDiscount() {
        return mountDiscount;
    }

    public void setMountDiscount(Double mountDiscount) {
        this.mountDiscount = mountDiscount;
    }

    public Double getDescreal() {
        return descreal;
    }

    public void setDescreal(Double descreal) {
        this.descreal = descreal;
    }

    public Double getMountNeto() {
        return mountNeto;
    }

    public void setMountNeto(Double mountNeto) {
        this.mountNeto = mountNeto;
    }

    public String getTypePay() {
        return typePay;
    }

    public void setTypePay(String typePay) {
        this.typePay = typePay;
    }

    public String getUnitAcadAdm() {
        return unitAcadAdm;
    }

    public void setUnitAcadAdm(String unitAcadAdm) {
        this.unitAcadAdm = unitAcadAdm;
    }

    public String getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(String studyPlan) {
        this.studyPlan = studyPlan;
    }

    public Double getTypeChange() {
        return typeChange;
    }

    public void setTypeChange(Double typeChange) {
        this.typeChange = typeChange;
    }

    public Double getTotalInvoice() {
        return totalInvoice;
    }

    public void setTotalInvoice(Double totalInvoice) {
        this.totalInvoice = totalInvoice;
    }

    public Double getNumberCredit() {
        return numberCredit;
    }

    public void setNumberCredit(Double numberCredit) {
        this.numberCredit = numberCredit;
    }

    public String getDescrOrder() {
        return descrOrder;
    }

    public void setDescrOrder(String descrOrder) {
        this.descrOrder = descrOrder;
    }

    public CustomerOrder getCustomerOrderMovement() {
        return customerOrderMovement;
    }

    public void setCustomerOrderMovement(CustomerOrder customerOrderMovement) {
        this.customerOrderMovement = customerOrderMovement;
    }

    public Date getDateRecord() {
        return dateRecord;
    }

    public void setDateRecord(Date dateRecord) {
        this.dateRecord = dateRecord;
    }
}
