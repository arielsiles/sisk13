package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
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


@Entity
@Table(name = "personacliente",schema = Constants.CASHBOX_SCHEMA)
public class Client implements Serializable, BaseModel {

    @Id
    @Column(name = "IDPERSONACLIENTE")
    private Long id;

    @Column(name = "NRO_DOC")
    private String nroDoc;

    @Column(name = "AP")
    private String ap = "";

    @Column(name = "AM")
    private String am = "";

    @Column(name = "NOM")
    private String name = "";

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
    private String direccion;

    @Column(name = "NIT")
    private String nit;

    @Column(name = "RAZONSOCIAL")
    private String razonsocial = "";

    @Column(name = "PORCENTAJECOMISION")
    private Double porcentajeComision = 0.0;

    @Column(name = "CODIGOCLIENTE")
    private String codigo;

    @JoinColumn(name = "IDTERRITORIOTRABAJO", referencedColumnName = "IDTERRITORIOTRABAJO")
    @ManyToOne
    private Territoriotrabajo territoriotrabajo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNroDoc() {
        return nroDoc;
    }

    public void setNroDoc(String nroDoc) {
        this.nroDoc = nroDoc;
    }

    public String getAp() {
        return ap;
    }

    public void setAp(String ap) {
        this.ap = ap;
    }

    public String getAm() {
        return am;
    }

    public void setAm(String am) {
        this.am = am;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }


    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getRazonsocial() {
        return razonsocial;
    }

    public void setRazonsocial(String razonsocial) {
        this.razonsocial = razonsocial;
    }

    public Double getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(Double descuento) {
        this.porcentajeComision = descuento;
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
        if(StringUtils.isEmpty(razonsocial))
            return name +" "+ap+" "+am;
        else
            return razonsocial;
    }

    public String getFullName(){
        return getName() + " " + getAp() + " " + getAm();
    }

    public Territoriotrabajo getTerritoriotrabajo() {
        return territoriotrabajo;
    }

    public void setTerritoriotrabajo(Territoriotrabajo territoriotrabajo) {
        this.territoriotrabajo = territoriotrabajo;
    }
}
