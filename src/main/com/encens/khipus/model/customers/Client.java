package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@NamedQueries(
        {
                @NamedQuery(name = "Client.findAll", query = "select c from Client c")
        }
)

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "Client.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "personacliente",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "personacliente")
public class Client implements BaseModel {

    @Id
    @Column(name = "IDPERSONACLIENTE", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Client.tableGenerator")
    private Long id;

    @Column(name = "NRO_DOC")
    private String idNumber;

    @Column(name = "COMP")
    private String complement;

    @Column(name = "AP")
    private String lastName;

    @Column(name = "AM")
    private String maidenName;

    @Column(name = "NOM")
    private String name;

    @Column(name = "SEXO")
    private String sexo;

    @Column(name = "EST_CIVIL")
    private String estCivil;

    @Column(name = "FECHA_NAC")
    @Temporal(TemporalType.DATE)
    private Date fechaNac;

    @Column(name = "CEM_COD")
    private String cemCod;

    @Column(name = "OCU_COD")
    private String ocuCod;

    @Column(name = "TDO_COD")
    private String tdoCod;

    @Column(name = "SIS_COD")
    private String sisCod;

    @Column(name = "DIRECCION")
    private String address;

    @Column(name = "TELEFONO")
    private Integer phone;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "NIT")
    private String nitNumber;

    @Column(name = "RAZONSOCIAL")
    private String businessName;

    @Column(name = "PORCENTAJECOMISION")
    private Double commission;

    @Column(name = "PORCENTAJEGARANTIA")
    private Double guarantee;

    @Column(name = "CODIGOCLIENTE")
    private String codigo;

    @Column(name = "TIPO_PERSONA")
    private String personType;

    @Column(name = "ESPERSONA", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean personFlag = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipocliente", referencedColumnName = "idtipocliente", nullable = false)
    private ClientType clientType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipodocsin", referencedColumnName = "idtipodocumento")
    private DocumentType invoiceDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmetodopagosin", referencedColumnName = "idmetodopago")
    private PaymentMethodSin paymentMethodSin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriacliente", referencedColumnName = "idcategoriacliente", nullable = true)
    private CustomerCategory customerCategory;

    @JoinColumn(name = "IDTERRITORIOTRABAJO", referencedColumnName = "IDTERRITORIOTRABAJO")
    @ManyToOne
    private Territoriotrabajo territoriotrabajo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String nroDoc) {
        this.idNumber = nroDoc;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String ap) {
        this.lastName = ap;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String am) {
        this.maidenName = am;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSexo() {
        if(this.sexo == null)
            this.sexo = "MASCULINO";
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEstCivil() {
        return estCivil;
    }

    public void setEstCivil(String estCivil) {
        this.estCivil = estCivil;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getCemCod() {
        return cemCod;
    }

    public void setCemCod(String cemCod) {
        this.cemCod = cemCod;
    }

    public String getOcuCod() {
        return ocuCod;
    }

    public void setOcuCod(String ocuCod) {
        this.ocuCod = ocuCod;
    }

    public String getTdoCod() {
        return tdoCod;
    }

    public void setTdoCod(String tdoCod) {
        this.tdoCod = tdoCod;
    }

    public String getSisCod() {
        return sisCod;
    }

    public void setSisCod(String sisCod) {
        this.sisCod = sisCod;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String direccion) {
        this.address = direccion;
    }


    public String getNitNumber() {
        return nitNumber;
    }

    public void setNitNumber(String nit) {
        this.nitNumber = nit;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String razonsocial) {
        this.businessName = razonsocial;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double descuento) {
        this.commission = descuento;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombreCompleto(){
        if(this.id == null)
            return "";
        if(StringUtils.isEmpty(businessName))
            return name +" "+ lastName +" "+ maidenName;
        else
            return businessName;
    }

    public String getFullName(){
        String result = getName() + " ";
        result = result + (getLastName() != null ? getLastName() + " " : "") + (getMaidenName() != null ? getMaidenName() : "");
        return result;
    }

    public Territoriotrabajo getTerritoriotrabajo() {
        return territoriotrabajo;
    }

    public void setTerritoriotrabajo(Territoriotrabajo territoriotrabajo) {
        this.territoriotrabajo = territoriotrabajo;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public Boolean getPersonFlag() {
        return personFlag;
    }

    public void setPersonFlag(Boolean personFlag) {
        this.personFlag = personFlag;
    }

    public CustomerCategory getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(CustomerCategory customerCategory) {
        this.customerCategory = customerCategory;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public Double getGuarantee() {
        return guarantee;
    }

    public void setGuarantee(Double guarantee) {
        this.guarantee = guarantee;
    }

    public DocumentType getInvoiceDocumentType() {
        return invoiceDocumentType;
    }

    public void setInvoiceDocumentType(DocumentType invoiceDocumentType) {
        this.invoiceDocumentType = invoiceDocumentType;
    }

    public PaymentMethodSin getPaymentMethodSin() {
        return paymentMethodSin;
    }

    public void setPaymentMethodSin(PaymentMethodSin paymentMethodSin) {
        this.paymentMethodSin = paymentMethodSin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }
}
