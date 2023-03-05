package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
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
                @NamedQuery(name = "Client.findAll", query = "select c from Client c"),
                @NamedQuery(name = "Client.findByIdNumber", query = "select c from Client c where c.idNumber =:idNumber")
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
    @Column(name = "idpersonacliente", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Client.tableGenerator")
    private Long id;

    @Column(name = "nro_doc")
    private String idNumber;

    @Column(name = "comp")
    private String complement;

    @Column(name = "ap")
    private String lastName;

    @Column(name = "am")
    private String maidenName;

    @Column(name = "nom")
    private String name;

    @Column(name = "sexo")
    private String sexo;

    @Column(name = "est_civil")
    private String estCivil;

    @Column(name = "fecha_nac")
    @Temporal(TemporalType.DATE)
    private Date fechaNac;

    @Column(name = "cem_cod")
    private String cemCod;

    @Column(name = "ocu_cod")
    private String ocuCod;

    @Column(name = "tdo_cod")
    private String tdoCod;

    @Column(name = "sis_cod")
    private String sisCod;

    @Column(name = "direccion")
    private String address;

    @Column(name = "telefono")
    private Integer phone;

    @Column(name = "email")
    private String email;

    @Column(name = "nit")
    private String nitNumber;

    @Column(name = "razonsocial")
    private String businessName;

    @Column(name = "codmetodopagosin")
    private Integer paymentMethodTypeCode;

    @Column(name = "descuento")
    private BigDecimal additionalDiscount = BigDecimal.ZERO;

    @Column(name = "descuentoprod")
    private BigDecimal productDiscount  = BigDecimal.ZERO;;

    @Column(name = "porcentajecomision")
    private Double commission;

    @Column(name = "porcentajegarantia")
    private Double guarantee;

    @Column(name = "codigocliente")
    private String codigo;

    @Column(name = "tipo_persona")
    private String personType;

    @Column(name = "espersona", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean personFlag = Boolean.TRUE;

    @Column(name = "ctaregula")
    private String regularizeAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipocliente", referencedColumnName = "idtipocliente", nullable = false)
    private ClientType clientType;

    @ManyToOne
    @JoinColumn(name = "idtipodocsin", referencedColumnName = "idtipodocumento")
    private DocumentType invoiceDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriacliente", referencedColumnName = "idcategoriacliente", nullable = true)
    private CustomerCategory customerCategory;

    @JoinColumn(name = "idterritoriotrabajo", referencedColumnName = "idterritoriotrabajo")
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

    public String getNitNumberComplement() {

        String nitCiCexPasOd = "";
        if (getInvoiceDocumentType() != null) {
            if (getInvoiceDocumentType().getSinCode() == 1)
                nitCiCexPasOd = "CI-";
            if (getInvoiceDocumentType().getSinCode() == 2)
                nitCiCexPasOd = "CEX-";
            if (getInvoiceDocumentType().getSinCode() == 3)
                nitCiCexPasOd = "PAS-";
            if (getInvoiceDocumentType().getSinCode() == 4)
                nitCiCexPasOd = "OD-";
            if (getInvoiceDocumentType().getSinCode() == 5)
                nitCiCexPasOd = "NIT-";
        }

        return nitCiCexPasOd + nitNumber + ((getComplement() != null) ? " " + getComplement() : "");
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

    public boolean validNitNumber(){
        Long number = new Long(getNitNumber());
        if (number <= 0)
            return false;
        else
            return true;
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

    public BigDecimal getAdditionalDiscount() {
        return additionalDiscount;
    }

    public void setAdditionalDiscount(BigDecimal additionalDiscount) {
        this.additionalDiscount = additionalDiscount;
    }

    public BigDecimal getProductDiscount() {
        return productDiscount;
    }

    public void setProductDiscount(BigDecimal productDiscount) {
        this.productDiscount = productDiscount;
    }

    public Integer getPaymentMethodTypeCode() {
        return paymentMethodTypeCode;
    }

    public void setPaymentMethodTypeCode(Integer paymentMethodTypeCode) {
        this.paymentMethodTypeCode = paymentMethodTypeCode;
    }

    public String getRegularizeAccount() {
        return regularizeAccount;
    }

    public void setRegularizeAccount(String regularizeAccount) {
        this.regularizeAccount = regularizeAccount;
    }

}
